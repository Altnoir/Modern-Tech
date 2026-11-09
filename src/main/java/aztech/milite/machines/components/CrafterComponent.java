package aztech.milite.machines.components;

import static aztech.milite.util.Simulation.ACT;
import static aztech.milite.util.Simulation.SIMULATE;

import aztech.milite.api.machine.component.CrafterAccess;
import aztech.milite.api.machine.component.InventoryAccess;
import aztech.milite.compat.almostunified.AlmostUnifiedFacade;
import aztech.milite.inventory.AbstractConfigurableStack;
import aztech.milite.inventory.ConfigurableFluidStack;
import aztech.milite.inventory.ConfigurableItemStack;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.MachineComponent;
import aztech.milite.machines.recipe.condition.MachineProcessCondition;
import aztech.milite.machines.recipe.MachineRecipe;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.MI;
import aztech.milite.MIAdvancementTriggers;
import aztech.milite.stats.PlayerStatistics;
import aztech.milite.stats.PlayerStatisticsData;
import aztech.milite.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.milite.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.milite.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.milite.util.Simulation;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CrafterComponent implements MachineComponent.ServerOnly, CrafterAccess {
    private final MachineProcessCondition.Context conditionContext;

    public CrafterComponent(MachineBlockEntity blockEntity, Inventory inventory, Behavior behavior) {
        this.inventory = inventory;
        this.behavior = behavior;
        this.conditionContext = () -> blockEntity;
    }

    public interface Inventory extends InventoryAccess {
        List<ConfigurableItemStack> getItemInputs();

        List<ConfigurableItemStack> getItemOutputs();

        List<ConfigurableFluidStack> getFluidInputs();

        List<ConfigurableFluidStack> getFluidOutputs();

        int hash();
    }

    /**
     * The machine's speed index, including bonuses, before taking the recipe level into account.
     */
    public int getSpeedMultiplier() {
        return getSpeedMultiplier(MachineLevel.BRONZE);
    }

    private int getSpeedMultiplier(MachineLevel recipeLevel) {
        long speed = Math.clamp((long) behavior.getMachineLevel().speedMultiplierFrom(recipeLevel) * behavior.getSpeedBonus(),
                1, Integer.MAX_VALUE);
        if (behavior.getMachineLevel().canCraft(MachineLevel.ELECTRIC)) {
            int units = Math.min(behavior.getOverclockerUnits(), 31);
            for (int i = 0; i < units; i++) {
                speed *= 2;
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, speed));
    }

    private final Inventory inventory;
    private final Behavior behavior;

    @Nullable
    private RecipeHolder<MachineRecipe> activeRecipe = null;
    @Nullable
    private ResourceLocation delayedActiveRecipe;

    private boolean matchesMultipleRecipes;

    /** The total FE a craft of the active recipe costs, which does not depend on how fast the machine is. */
    private long recipeFe;
    /** The FE per tick the active recipe wants at full speed, which a starved machine gets only part of. */
    private long recipeFePerTick;
    private long usedFe;
    /** The FE that went into the recipe during the last tick, which is the requested amount unless the machine is starved. */
    private long lastConsumedFe;


    private int lastInvHash = 0;
    private int lastForcedTick = 0;

    @Override
    public float getProgress() {
        return recipeFe == 0 ? 0 : (float) usedFe / recipeFe;
    }

    @Override
    public boolean hasActiveRecipe() {
        return activeRecipe != null;
    }

    @Override
    public boolean matchesMultipleRecipes() {
        return matchesMultipleRecipes;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    /**
     * The FE per tick the active recipe needs to run at the speed of this machine.
     */
    public long getBaseRecipeFe() {
        Preconditions.checkArgument(hasActiveRecipe());
        return recipeFePerTick;
    }

    /**
     * The FE the active recipe actually ate during the last tick. It is lower than {@link #getBaseRecipeFe()} while the
     * machine is starved, which is what makes the recipe take longer than its speed allows.
     */
    @Override
    public long getCurrentRecipeFe() {
        Preconditions.checkArgument(hasActiveRecipe());
        return lastConsumedFe;
    }

    /**
     * Perform a crafter tick, and return whether the crafter is active after the
     * tick.
     */
    public boolean tickRecipe() {
        if (behavior.getCrafterWorld().isClientSide()) {
            throw new IllegalStateException("May not call client side.");
        }
        boolean isActive;
        boolean isEnabled = behavior.isEnabled();

        loadDelayedActiveRecipe();

        // START RECIPE IF NECESSARY
        // usedFe == 0 means that no recipe is currently started
        boolean recipeStarted = false;
        if (usedFe == 0 && isEnabled) {
            if (behavior.consumeFe(1, SIMULATE) == 1) {
                boolean matchesMultipleRecipesBefore = matchesMultipleRecipes;
                recipeStarted = updateActiveRecipe();
                if (matchesMultipleRecipes && !matchesMultipleRecipesBefore) {
                    behavior.getStatsOrDummy().triggerAdvancement(behavior.getCrafterWorld(),
                            MIAdvancementTriggers.OVERLAPPING_RECIPES);
                }
            }
        }

        if (activeRecipe != null) {
            lastForcedTick = 0;
            var recipe = activeRecipe.value();
            recipeFePerTick = recipe.getFePerTick(getSpeedMultiplier(recipe.level));
        }

        // PROCESS RECIPE TICK
        long fe = 0;
        if (activeRecipe != null && isEnabled) {
            if (usedFe > 0 || recipeStarted) {
                // A starved machine eats what its buffer holds and keeps its progress until the power comes back.
                long request = Math.min(recipeFePerTick, recipeFe - usedFe);
                fe = !behavior.banRecipe(activeRecipe.value()) && activeRecipe.value().conditionsMatch(conditionContext)
                        ? behavior.consumeFe(request, ACT)
                        : 0;
                lastConsumedFe = fe;
                isActive = fe > 0;
                usedFe += fe;

                if (usedFe >= recipeFe) {
                    putItemOutputs(activeRecipe.value(), false, false);
                    putFluidOutputs(activeRecipe.value(), false, false);
                    clearLocks();
                    usedFe = 0;
                    behavior.onCraft();
                }
            } else {
                isActive = false;
            }
        } else {
            isActive = false;
        }

        // If the recipe is done, allow starting another one right away
        clearActiveRecipeIfPossible();

        return isActive;
    }

    public interface Behavior {
        default boolean isEnabled() {
            return true;
        }

        long consumeFe(long max, Simulation simulation);

        default boolean banRecipe(MachineRecipe recipe) {
            return !getMachineLevel().canCraft(recipe.level);
        }

        /**
         * The recipe type of this machine, which is the one its viewer category and its recipe list are built from.
         */
        MachineRecipeType recipeType();

        /**
         * Every recipe type this machine runs. A machine that took over the work of an older one lists both, and the
         * recipes of the second are found exactly like its own: by item input when it looks for something to start, and
         * by id when it restores a craft that a reload interrupted.
         */
        default List<MachineRecipeType> recipeTypes() {
            return List.of(recipeType());
        }

        /**
         * The level of this machine. A recipe may only run when its own level is not above this one.
         */
        MachineLevel getMachineLevel();

        /**
         * An extra speed factor on top of {@link MachineLevel#speedMultiplier()}, for machines that can speed themselves
         * up beyond what their level does (the gunpowder overclock of the steam multiblocks, for instance).
         *
         * <p>
         * How much power the machine is supplied with decides whether it actually reaches this speed: a machine never
         * eats more than its buffer holds, so a starved one simply sits in the middle of its progress bar.
         */
        default int getSpeedBonus() {
            return 1;
        }

        default int getOverclockerUnits() {
            return 0;
        }

        default void onCraft() {}

        // can't use getWorld() or the remapping will fail
        ServerLevel getCrafterWorld();

        default int getMaxFluidOutputs() {
            return Integer.MAX_VALUE;
        }

        @Nullable
        UUID getOwnerUuid();

        default PlayerStatistics getStatsOrDummy() {
            var uuid = getOwnerUuid();
            if (uuid == null) {
                return PlayerStatistics.DUMMY;
            } else {
                return PlayerStatisticsData.get(getCrafterWorld().getServer()).get(uuid);
            }
        }

        /**
         * If {@code true}, {@link #takeFluidInputs} will only allow each fluid stack to be used
         * for up to one fluid input. This is used for the fusion reactor.
         */
        default boolean oneFluidInputPerStack() {
            return false;
        }
    }

    private void clearActiveRecipeIfPossible() {
        if (usedFe == 0) {
            activeRecipe = null;
        }
    }

    /**
     * Attempt to re-lock hatches to continue the active recipe.
     *
     * @return True if there is no current recipe or if the hatches could be locked
     *         for it, false otherwise.
     */
    public boolean tryContinueRecipe() {
        loadDelayedActiveRecipe();

        if (activeRecipe != null) {
            if (putItemOutputs(activeRecipe.value(), true, false) && putFluidOutputs(activeRecipe.value(), true, false)) {
                // Relock stacks
                putItemOutputs(activeRecipe.value(), true, true);
                putFluidOutputs(activeRecipe.value(), true, true);
            } else {
                return false;
            }
        }

        return true;
    }

    private void loadDelayedActiveRecipe() {
        if (delayedActiveRecipe != null) {
            activeRecipe = findRecipe(delayedActiveRecipe);
            delayedActiveRecipe = null;
            if (activeRecipe == null) { // If a recipe got removed, the saved progress is meaningless and must go
                usedFe = 0;
            }
        }
    }

    /**
     * A recipe id names a recipe across the whole recipe manager, so trying the types of the machine in turn finds the
     * same recipe the machine had picked before it was unloaded.
     */
    @Nullable
    private RecipeHolder<MachineRecipe> findRecipe(ResourceLocation recipeId) {
        for (MachineRecipeType recipeType : behavior.recipeTypes()) {
            var recipe = recipeType.getRecipe(behavior.getCrafterWorld(), recipeId);
            if (recipe != null) {
                return recipe;
            }
        }
        return null;
    }

    private static boolean areAllSlotsLocked(List<? extends AbstractConfigurableStack> slots) {
        for (var output : slots) {
            if (!output.canPlayerLock()) {
                return true;
            }
            if (!output.isPlayerLocked()) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllOutputSlotsLocked() {
        return areAllSlotsLocked(inventory.getItemOutputs()) &&
                areAllSlotsLocked(inventory.getFluidOutputs());
    }

    private boolean shouldUpdateActiveRecipe() {
        int currentHash = inventory.hash();
        if (currentHash == lastInvHash) {
            if (lastForcedTick == 0) {
                lastForcedTick = 100;
            } else {
                --lastForcedTick;
                return false;
            }
        } else {
            lastInvHash = currentHash;
        }
        return true;
    }

    private boolean updateActiveRecipe() {
        if (!shouldUpdateActiveRecipe()) {
            return false;
        }
        // Only then can we run the iteration over the recipes
        var outputsLocked = areAllOutputSlotsLocked();
        RecipeHolder<MachineRecipe> newActiveRecipe = null;
        for (RecipeHolder<MachineRecipe> recipe : getRecipes()) {
            if (behavior.banRecipe(recipe.value()))
                continue;
            if (canStartRecipe(recipe.value())) {
                if (newActiveRecipe != null) {
                    if (overlaps(newActiveRecipe.value(), recipe.value())) {
                        matchesMultipleRecipes = true;
                        return false;
                    }
                    continue;
                }
                newActiveRecipe = recipe;
                if (outputsLocked) {
                    break;
                }
            }
        }
        matchesMultipleRecipes = false;
        if (newActiveRecipe != null && tryStartRecipe(newActiveRecipe.value())) {
            activeRecipe = newActiveRecipe;
            recipeFe = newActiveRecipe.value().getTotalFe();
            usedFe = 0;
            return true;
        }
        return false;
    }

    private boolean overlaps(MachineRecipe first, MachineRecipe second) {
        // When we find which ingredient matches, check if the second recipe has an ingredient that also matches this stack
        // We must do an overlap check this way because ingredients are not able to be easily compared against each other aside from equality
        for (var stack : inventory.getFluidInputs()) {
            for (var input : first.fluidInputs) {
                if (input.fluid().test(stack.toStack())) {
                    for (var otherInput : second.fluidInputs) {
                        if (otherInput.fluid().test(stack.toStack())) {
                            return true;
                        }
                    }
                }
            }
        }
        for (var stack : inventory.getItemInputs()) {
            for (var input : first.itemInputs) {
                if (input.matches(stack.toStack())) {
                    for (var otherInput : second.itemInputs) {
                        if (otherInput.matches(stack.toStack())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Iterable<RecipeHolder<MachineRecipe>> getRecipes() {
        if (usedFe > 0) {
            return Collections.singletonList(activeRecipe);
        } else {
            return getRecipes(behavior.getCrafterWorld(), behavior.recipeTypes(), inventory.getItemInputs());
        }
    }

    public static Collection<RecipeHolder<MachineRecipe>> getRecipes(ServerLevel level, MachineRecipeType recipeType, List<ConfigurableItemStack> itemInputs) {
        Set<RecipeHolder<MachineRecipe>> recipes = new HashSet<>(recipeType.getFluidOnlyRecipes(level));
        for (ConfigurableItemStack stack : itemInputs) {
            if (!stack.isEmpty()) {
                recipes.addAll(recipeType.getMatchingRecipes(level, stack.getResource().getItem()));
            }
        }
        return recipes;
    }

    /**
     * The recipes of every type the machine runs. A recipe belongs to exactly one type, so a machine that runs several
     * never sees the same recipe twice.
     */
    public static Collection<RecipeHolder<MachineRecipe>> getRecipes(ServerLevel level, List<MachineRecipeType> recipeTypes,
            List<ConfigurableItemStack> itemInputs) {
        Set<RecipeHolder<MachineRecipe>> recipes = new HashSet<>();
        for (MachineRecipeType recipeType : recipeTypes) {
            recipes.addAll(getRecipes(level, recipeType, itemInputs));
        }
        return recipes;
    }

    private boolean canStartRecipe(MachineRecipe recipe) {
        return takeItemInputs(recipe, true) && takeFluidInputs(recipe, true) && putItemOutputs(recipe, true, false)
                && putFluidOutputs(recipe, true, false) && recipe.conditionsMatch(conditionContext);
    }

    /**
     * Try to start a recipe. Return true if success, false otherwise. If false,
     * nothing was changed.
     */
    private boolean tryStartRecipe(MachineRecipe recipe) {
        if (canStartRecipe(recipe)) {
            takeItemInputs(recipe, false);
            takeFluidInputs(recipe, false);
            putItemOutputs(recipe, true, true);
            putFluidOutputs(recipe, true, true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Writes the craft that is in progress, so that a machine that is unloaded in the middle of a recipe resumes it
     * where it stopped instead of starting over.
     */
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("usedFe", this.usedFe);
        tag.putLong("recipeFe", this.recipeFe);
        tag.putLong("recipeFePerTick", this.recipeFePerTick);
        if (activeRecipe != null) {
            tag.putString("activeRecipe", this.activeRecipe.id().toString());
        } else if (delayedActiveRecipe != null) {
            tag.putString("activeRecipe", this.delayedActiveRecipe.toString());
        }
    }

    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        this.usedFe = tag.getLong("usedFe");
        this.recipeFe = tag.getLong("recipeFe");
        this.recipeFePerTick = tag.getLong("recipeFePerTick");
        String recipeId = tag.getString("activeRecipe");
        this.delayedActiveRecipe = recipeId.isEmpty() ? null : ResourceLocation.tryParse(recipeId);
        if (delayedActiveRecipe == null && usedFe > 0) {
            usedFe = 0;
            MI.LOGGER.error("Had to set the usedFe of CrafterComponent to 0, but that should never happen!");
        }
    }

    public static boolean doInputsMatch(List<ConfigurableItemStack> itemInputs, List<ConfigurableFluidStack> fluidInputs, MachineRecipe recipe) {
        return takeItemInputs(itemInputs, PlayerStatistics.DUMMY, recipe, true) &&
                takeFluidInputs(fluidInputs, PlayerStatistics.DUMMY, recipe, false, true);
    }

    protected boolean takeItemInputs(MachineRecipe recipe, boolean simulate) {
        return takeItemInputs(inventory.getItemInputs(), behavior.getStatsOrDummy(), recipe, simulate);
    }

    protected static boolean takeItemInputs(List<ConfigurableItemStack> baseList, PlayerStatistics stats, MachineRecipe recipe, boolean simulate) {
        List<ConfigurableItemStack> stacks = simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

        boolean ok = true;
        for (MachineRecipe.ItemInput input : recipe.itemInputs) {
            if (!simulate && input.reusable()) {
                continue;
            }
            if (!simulate && input.probability() < 1) { // if we are not simulating, there is a chance we don't need to take this output
                if (ThreadLocalRandom.current().nextFloat() >= input.probability()) {
                    continue;
                }
            }
            int remainingAmount = input.amount();
            for (ConfigurableItemStack stack : stacks) {
                if (stack.getAmount() > 0 && stack.getResource().test(input.ingredient())) {
                    int taken = Math.min((int) stack.getAmount(), remainingAmount);
                    if (taken > 0 && !simulate) {
                        stats.addUsedItems(stack.getResource().getItem(), taken);
                    }
                    stack.decrement(taken);
                    remainingAmount -= taken;
                    if (remainingAmount == 0)
                        break;
                }
            }
            if (remainingAmount > 0)
                ok = false;
        }

        return ok;
    }

    protected boolean takeFluidInputs(MachineRecipe recipe, boolean simulate) {
        return takeFluidInputs(inventory.getFluidInputs(), behavior.getStatsOrDummy(), recipe, behavior.oneFluidInputPerStack(), simulate);
    }

    protected static boolean takeFluidInputs(List<ConfigurableFluidStack> baseList, PlayerStatistics stats, MachineRecipe recipe, boolean oneInputPerStack, boolean simulate) {
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;
        boolean[] usedStacks = oneInputPerStack ? new boolean[stacks.size()] : null;

        boolean ok = true;
        for (MachineRecipe.FluidInput input : recipe.fluidInputs) {
            if (!simulate && input.probability() < 1) { // if we are not simulating, there is a chance we don't need to take this output
                if (ThreadLocalRandom.current().nextFloat() >= input.probability()) {
                    continue;
                }
            }
            long remainingAmount = input.amount();
            for (int istack = 0; istack < stacks.size(); ++istack) {
                if (oneInputPerStack && usedStacks[istack]) {
                    continue;
                }
                ConfigurableFluidStack stack = stacks.get(istack);
                if (fluidIngredientMatch(stack.getResource(), input.fluid())) {
                    long taken = Math.min(remainingAmount, stack.getAmount());
                    if (taken > 0) {
                        if (oneInputPerStack) {
                            usedStacks[istack] = true;
                        }
                        if (!simulate) {
                            stats.addUsedFluids(stack.getResource().getFluid(), taken);
                        }
                        stack.decrement(taken);
                        remainingAmount -= taken;
                        if (remainingAmount == 0)
                            break;
                    }
                }
            }
            if (remainingAmount > 0)
                ok = false;
        }
        return ok;
    }

    private static boolean fluidIngredientMatch(FluidVariant resource, FluidIngredient ingredient) {
        if (ingredient.isSimple()) {
            for (var stack : ingredient.getStacks()) {
                if (resource.equals(FluidVariant.of(stack.getFluid()))) {
                    return true;
                }
            }
            return false;
        } else {
            return ingredient.test(resource.toStack(1));
        }
    }

    protected boolean putItemOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        List<ConfigurableItemStack> baseList = inventory.getItemOutputs();
        List<ConfigurableItemStack> stacks = simulate ? ConfigurableItemStack.copyList(baseList) : baseList;

        List<Integer> locksToToggle = new ArrayList<>();
        List<Item> lockItems = new ArrayList<>();

        boolean ok = true;
        for (MachineRecipe.ItemOutput output : recipe.itemOutputs) {
            if (output.probability() < 1) {
                if (simulate)
                    continue; // don't check output space for probabilistic recipes
                float randFloat = ThreadLocalRandom.current().nextFloat();
                if (randFloat > output.probability())
                    continue;
            }
            int remainingAmount = output.amount();
            // Try to insert in non-empty stacks or locked first, then also allow insertion
            // in empty stacks.
            for (int loopRun = 0; loopRun < 2; loopRun++) {
                int stackId = 0;
                for (ConfigurableItemStack stack : stacks) {
                    stackId++;
                    ItemVariant key = stack.getResource();
                    if (stack.isResourceAllowedByLock(output.variant()) &&
                            (key.equals(output.variant()) || key.isBlank())) {
                        // If simulating or chanced output or is the first try, respect the adjusted capacity.
                        // If putting the output on the second try, don't respect the adjusted capacity in case it was
                        // reduced during the processing.
                        int remainingCapacity = simulate || output.probability() < 1 || loopRun == 0
                                ? (int) stack.getRemainingCapacityFor(output.variant())
                                : output.variant().getMaxStackSize() - (int) stack.getAmount();
                        int ins = Math.min(remainingAmount, remainingCapacity);
                        if (ins > 0) {
                            if (key.isBlank()) {
                                if (stack.isMachineLocked() || stack.isPlayerLocked() || loopRun == 1) {
                                    stack.setAmount(ins);
                                    stack.setKey(output.variant());
                                } else {
                                    ins = 0;
                                }
                            } else {
                                stack.increment(ins);
                            }
                        }
                        remainingAmount -= ins;
                        // ins changed inside of previous if, need to check again!
                        if (ins > 0) {
                            locksToToggle.add(stackId - 1);
                            lockItems.add(output.variant().getItem());
                            if (!simulate) {
                                behavior.getStatsOrDummy().addProducedItems(behavior.getCrafterWorld(), output.variant().getItem(), ins);
                            }
                        }
                        if (remainingAmount == 0)
                            break;
                    }
                }
            }
            if (remainingAmount > 0)
                ok = false;
        }

        if (toggleLock) {
            for (int i = 0; i < locksToToggle.size(); i++) {
                baseList.get(locksToToggle.get(i)).enableMachineLock(lockItems.get(i));
            }
        }
        return ok;
    }

    protected boolean putFluidOutputs(MachineRecipe recipe, boolean simulate, boolean toggleLock) {
        List<ConfigurableFluidStack> baseList = inventory.getFluidOutputs();
        List<ConfigurableFluidStack> stacks = simulate ? ConfigurableFluidStack.copyList(baseList) : baseList;

        List<Integer> locksToToggle = new ArrayList<>();
        List<Fluid> lockFluids = new ArrayList<>();

        boolean ok = true;
        for (int i = 0; i < Math.min(recipe.fluidOutputs.size(), behavior.getMaxFluidOutputs()); ++i) {
            MachineRecipe.FluidOutput output = recipe.fluidOutputs.get(i);
            if (output.probability() < 1) {
                if (simulate)
                    continue; // don't check output space for probabilistic recipes
                float randFloat = ThreadLocalRandom.current().nextFloat();
                if (randFloat > output.probability())
                    continue;
            }
            // First, try to find a slot that contains the fluid. If we couldn't find one,
            // we insert in any stack
            outer:
            for (int tries = 0; tries < 2; ++tries) {
                for (int j = 0; j < stacks.size(); j++) {
                    ConfigurableFluidStack stack = stacks.get(j);
                    FluidVariant outputKey = FluidVariant.of(output.fluid());
                    if (stack.isResourceAllowedByLock(outputKey)
                            && ((tries == 1 && stack.isResourceBlank()) || stack.getResource().equals(outputKey))) {
                        long inserted = Math.min(output.amount(), stack.getRemainingSpace());
                        if (inserted > 0) {
                            stack.setKey(outputKey);
                            stack.increment(inserted);
                            locksToToggle.add(j);
                            lockFluids.add(output.fluid());
                            if (!simulate) {
                                behavior.getStatsOrDummy().addProducedFluids(output.fluid(), inserted);
                            }
                        }
                        if (inserted < output.amount()) {
                            ok = false;
                        }
                        break outer;
                    }
                }
                if (tries == 1) {
                    ok = false;
                }
            }
        }

        if (toggleLock) {
            for (int i = 0; i < locksToToggle.size(); i++) {
                baseList.get(locksToToggle.get(i)).enableMachineLock(lockFluids.get(i));
            }
        }
        return ok;
    }

    protected void clearLocks() {
        for (ConfigurableItemStack stack : inventory.getItemOutputs()) {
            if (stack.isMachineLocked())
                stack.disableMachineLock();
        }
        for (ConfigurableFluidStack stack : inventory.getFluidOutputs()) {
            if (stack.isMachineLocked())
                stack.disableMachineLock();
        }
    }

    private static <T, K extends TransferVariant<T>, S extends AbstractConfigurableStack<T, K>> void handleLocking(
            List<S> stacks,
            Predicate<T> matchesRecipe,
            long requiredAmount,
            Supplier<@Nullable T> lockTarget) {
        for (S stack : stacks) {
            if (stack.getLockedInstance() != null && matchesRecipe.apply(stack.getLockedInstance())) {
                requiredAmount -= stack.getTotalCapacityFor(stack.getLockedInstance());
                if (requiredAmount <= 0) {
                    // We have all we need already
                    return;
                }
            }
        }
        var newLockedInstance = lockTarget.get();
        if (newLockedInstance == null) {
            return;
        }

        AbstractConfigurableStack.playerLockNoOverride(newLockedInstance, requiredAmount, stacks);
    }

    public void lockRecipe(ResourceLocation recipeId, net.minecraft.world.entity.player.Inventory inventory) {
        Optional<RecipeHolder<MachineRecipe>> optionalMachineRecipe = behavior.recipeTypes().stream()
                .flatMap(recipeType -> recipeType.getRecipesWithCache(behavior.getCrafterWorld()).stream())
                .filter(recipe -> recipe.id().equals(recipeId)).findFirst();
        if (optionalMachineRecipe.isEmpty())
            return;
        var recipe = optionalMachineRecipe.get();
        // ITEM INPUTS
        for (MachineRecipe.ItemInput input : recipe.value().itemInputs) {
            handleLocking(
                    this.inventory.getItemInputs(),
                    item -> input.matches(new ItemStack(item)),
                    input.amount(),
                    () -> {
                        // Find the first match in the player inventory (useful for logs for example)
                        for (int i = 0; i < inventory.getContainerSize(); i++) {
                            ItemStack playerStack = inventory.getItem(i);
                            if (!playerStack.isEmpty() && input.matches(new ItemStack(playerStack.getItem()))) {
                                return playerStack.getItem();
                            }
                        }
                        List<Item> inputItems = input.getInputItems();
                        // Find the preferred item with Almost Unified if possible
                        if (!inputItems.isEmpty()) {
                            var targetItem = AlmostUnifiedFacade.INSTANCE.getTargetItem(inputItems.getFirst());
                            if (targetItem != null) {
                                return targetItem;
                            }
                        }
                        // Find the first match that is an item from MI or vanilla (useful for ingots for example)
                        for (Item item : inputItems) {
                            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                            if (id.getNamespace().equals(MI.ID) || id.getNamespace().equals("minecraft")) {
                                return item;
                            }
                        }
                        if (!inputItems.isEmpty()) {
                            return inputItems.get(0);
                        }
                        return null;
                    });
        }
        // ITEM OUTPUTS
        for (MachineRecipe.ItemOutput output : recipe.value().itemOutputs) {
            handleLocking(
                    this.inventory.getItemOutputs(),
                    item -> output.variant().isOf(item),
                    output.amount(),
                    output.variant()::getItem);
        }

        // FLUID INPUTS
        for (MachineRecipe.FluidInput input : recipe.value().fluidInputs) {
            handleLocking(
                    this.inventory.getFluidInputs(),
                    fluid -> input.fluid().test(new FluidStack(fluid, 1)),
                    input.amount(),
                    () -> {
                        // Find the first match in the player inventory
                        for (int i = 0; i < inventory.getContainerSize(); i++) {
                            var playerStack = FluidUtil.getFluidContained(inventory.getItem(i)).orElse(FluidStack.EMPTY);
                            if (!playerStack.isEmpty() && input.fluid().test(new FluidStack(playerStack.getFluid(), 1))) {
                                return playerStack.getFluid();
                            }
                        }
                        List<Fluid> inputFluids = input.getInputFluids();
                        // Find the first match that is an item from MI or vanilla
                        for (Fluid fluid : inputFluids) {
                            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                            if (id.getNamespace().equals(MI.ID) || id.getNamespace().equals("minecraft")) {
                                return fluid;
                            }
                        }
                        if (!inputFluids.isEmpty()) {
                            return inputFluids.get(0);
                        }
                        return null;
                    });
        }
        // FLUID OUTPUTS
        for (MachineRecipe.FluidOutput output : recipe.value().fluidOutputs) {
            handleLocking(
                    this.inventory.getFluidOutputs(),
                    fluid -> output.fluid() == fluid,
                    output.amount(),
                    output::fluid);
        }

        // LOCK ITEMS
        lockAll(this.inventory.getItemInputs());
        lockAll(this.inventory.getItemOutputs());
        // LOCK FLUIDS
        lockAll(this.inventory.getFluidInputs());
        lockAll(this.inventory.getFluidOutputs());
    }

    private static void lockAll(List<? extends AbstractConfigurableStack<?, ?>> stacks) {
        for (var stack : stacks) {
            if (stack.isEmpty() && stack.getLockedInstance() == null) {
                stack.togglePlayerLock();
            }
        }
    }
}
