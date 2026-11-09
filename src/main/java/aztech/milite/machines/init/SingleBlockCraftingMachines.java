package aztech.milite.machines.init;

import aztech.milite.MI;
import aztech.milite.MIFluids;
import aztech.milite.compat.rei.machines.MachineCategoryParams;
import aztech.milite.compat.rei.machines.ReiMachineRecipes;
import aztech.milite.compat.rei.machines.SteamMode;
import aztech.milite.inventory.ConfigurableFluidStack;
import aztech.milite.inventory.ConfigurableItemStack;
import aztech.milite.inventory.SlotPositions;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.blockentities.ElectricCraftingMachineBlockEntity;
import aztech.milite.machines.blockentities.SteamCraftingMachineBlockEntity;
import aztech.milite.machines.components.MachineInventoryComponent;
import aztech.milite.machines.components.OverclockComponent;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.guicomponents.EnergyBar;
import aztech.milite.machines.guicomponents.ProgressBar;
import aztech.milite.machines.recipe.MachineRecipeType;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registration of all single block crafting machines.
 */
public final class SingleBlockCraftingMachines {
    /**
     * The full ladder for machines whose lowest variant is the steel one.
     */
    private static final List<MachineLevel> LEVELS_STEEL_TO_IRIDIUM = List.of(MachineLevel.STEEL, MachineLevel.ELECTRIC,
            MachineLevel.ALUMINUM, MachineLevel.STAINLESS_STEEL, MachineLevel.TITANIUM, MachineLevel.IRIDIUM);

    private static final EnergyBar.Params DEFAULT_ENERGY_BAR = new EnergyBar.Params(18, 34);

    /**
     * The full ladder for machines that have no steam variant.
     */
    private static final List<MachineLevel> LEVELS_ALL = List.of(MachineLevel.ELECTRIC, MachineLevel.ALUMINUM,
            MachineLevel.STAINLESS_STEEL, MachineLevel.TITANIUM, MachineLevel.IRIDIUM);
    private static final List<MachineLevel> LEVELS_ALUMINUM_TO_IRIDIUM = List.of(MachineLevel.ALUMINUM,
            MachineLevel.STAINLESS_STEEL, MachineLevel.TITANIUM, MachineLevel.IRIDIUM);
    /**
     * The full ladder including the two steam machines at the bottom.
     */
    private static final List<MachineLevel> LEVELS_BRONZE_TO_IRIDIUM = List.of(MachineLevel.BRONZE, MachineLevel.STEEL,
            MachineLevel.ELECTRIC, MachineLevel.ALUMINUM, MachineLevel.STAINLESS_STEEL, MachineLevel.TITANIUM,
            MachineLevel.IRIDIUM);

    private SingleBlockCraftingMachines() {
    }

    /**
     * The id prefix of a machine at {@code level}. Every level carries its own prefix, so the id of a variant always
     * says which level it is.
     */
    private static String prefixOf(MachineLevel level) {
        return level.getSerializedName() + "_";
    }

    private static boolean isSteam(MachineLevel level) {
        return level == MachineLevel.BRONZE || level == MachineLevel.STEEL;
    }

    public static void init() {
        // @formatter:off
        registerMachineTiers(
                "Assembler", "assembler", MIMachineRecipeTypes.ASSEMBLER, 12, 3, 2, 0, guiParams -> guiParams.backgroundHeight(186),
                new ProgressBar.Params(120, 45, "circuit"), new EnergyBar.Params(14, 44),
                items -> items.addSlots(38, 27, 4, 3).addSlots(152, 27, 1, 3), fluids -> fluids.addSlots(113, 27, 2, 1),
                true, true, false,
                LEVELS_ALL, 16
        );
        registerMachineTiers(
                "Centrifuge","centrifuge", MIMachineRecipeTypes.CENTRIFUGE, 1, 4, 1, 4, guiParams -> {},
                new ProgressBar.Params(65, 33, "centrifuge"), DEFAULT_ENERGY_BAR,
                items -> items.addSlot(42, 27).addSlots(93, 27, 2, 2), fluids -> fluids.addSlot(42, 45).addSlots(131, 27, 2, 2),
                true, true, true,
                LEVELS_ALUMINUM_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Chemical Reactor", "chemical_reactor", MIMachineRecipeTypes.CHEMICAL_REACTOR, 3, 3, 3, 3, guiParams -> {},
                new ProgressBar.Params(88, 35, "triple_arrow"), new EnergyBar.Params(12, 35),
                items -> items.addSlots(30, 27, 3, 1).addSlots(116, 27, 3, 1), fluids -> fluids.addSlots(30, 47, 3, 1).addSlots(116, 47, 3, 1),
                true, false, false,
                LEVELS_ALUMINUM_TO_IRIDIUM, 24
        );
        registerMachineTiers(
                "Compressor", "compressor", MIMachineRecipeTypes.COMPRESSOR, 1, 1, 0, 0, guiParams -> {},
                new ProgressBar.Params(77, 34, "compress"), new EnergyBar.Params(18, 30),
                items -> items.addSlot(56, 35).addSlot(102, 35), fluids -> {},
                true, true, true,
                LEVELS_BRONZE_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Cutting Machine", "cutting_machine", MIMachineRecipeTypes.CUTTING_MACHINE, 1, 1, 0, 0, guiParams -> {},
                new ProgressBar.Params(77, 31, "slice"), new EnergyBar.Params(15, 34),
                items -> items.addSlot(56, 35).addSlot(102, 35), fluids -> {},
                true, false, false,
                LEVELS_BRONZE_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Distillery", "distillery", MIMachineRecipeTypes.DISTILLERY, 0, 0, 1, 1, guiParams -> {},
                new ProgressBar.Params(77, 33, "arrow"), new EnergyBar.Params(18, 30),
                items -> {}, fluids -> fluids.addSlot(56, 35).addSlot(102, 35),
                true, false, false,
                LEVELS_ALUMINUM_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Electrolyzer", "electrolyzer", MIMachineRecipeTypes.ELECTROLYZER, 1, 4, 1, 4, guiParams -> {},
                new ProgressBar.Params(66, 35, "arrow"), DEFAULT_ENERGY_BAR,
                items -> items.addSlot(42, 27).addSlots(93, 27, 2, 2), fluids -> fluids.addSlot(42, 47).addSlots(131, 27, 2, 2),
                true, false, true,
                LEVELS_ALUMINUM_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Furnace", "furnace", MIMachineRecipeTypes.FURNACE, 1, 1, 0, 0, guiParams -> {},
                new ProgressBar.Params(77, 33, "arrow"), new EnergyBar.Params(18, 30),
                items -> items.addSlot(56, 35).addSlot(102, 35), fluids -> {},
                true, false, false,
                LEVELS_BRONZE_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Lithography Machine", "lithography", MIMachineRecipeTypes.LITHOGRAPHY, 2, 1, 0, 0, guiParams -> {},
                new ProgressBar.Params(78, 34, "circuit"), DEFAULT_ENERGY_BAR,
                items -> items.addSlot(44, 27).addSlot(44, 45).addSlot(116, 36), fluids -> {},
                true, false, false,
                List.of(MachineLevel.STAINLESS_STEEL, MachineLevel.TITANIUM, MachineLevel.IRIDIUM), 0
        );
        registerMachineTiers(
                "Macerator", "macerator", MIMachineRecipeTypes.MACERATOR, 1, 4, 0, 0, guiParams -> {},
                new ProgressBar.Params(77, 33, "macerate"), DEFAULT_ENERGY_BAR,
                items -> items.addSlot(56, 35).addSlots(102, 27, 2, 2), fluids -> {},
                true, true, false,
                LEVELS_BRONZE_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Mixer", "mixer", MIMachineRecipeTypes.MIXER, 4, 2, 2, 2, guiParams -> {},
                new ProgressBar.Params(103, 33, "arrow"), new EnergyBar.Params(15, 34),
                items -> items.addSlots(62, 27, 2, 2).addSlots(129, 27, 1, 2), fluids -> fluids.addSlots(42, 27, 1, 2).addSlots(149, 27, 1, 2),
                true, true, true,
                LEVELS_BRONZE_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Packer", "packer", MIMachineRecipeTypes.PACKER, 3, 1, 0, 0, guiParams -> guiParams.backgroundHeight(178),
                new ProgressBar.Params(77, 33, "arrow"), new EnergyBar.Params(18, 30),
                items -> items.addSlots(56, 18, 1, 3).addSlot(102, 36), fluids -> {},
                true, false, false,
                LEVELS_STEEL_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Polarizer", "polarizer", MIMachineRecipeTypes.POLARIZER, 2, 1, 0, 0, guiParams -> {},
                new ProgressBar.Params(77, 30, "magnet"), new EnergyBar.Params(18, 30),
                items -> items.addSlots(56, 23, 1, 2).addSlot(102, 32), fluids -> {},
                true, true, false,
                LEVELS_ALUMINUM_TO_IRIDIUM, 16
        );
        registerMachineTiers(
                "Wiremill", "wiremill", MIMachineRecipeTypes.WIREMILL, 1, 1, 0, 0, guiParams -> {},
                new ProgressBar.Params(77, 34, "wiremill"), new EnergyBar.Params(18, 30),
                items -> items.addSlot(56, 35).addSlot(102, 35), fluids -> {},
                true, true, false,
                LEVELS_STEEL_TO_IRIDIUM, 16
        );

        registerMachineTiers(
                "Unpacker", "unpacker", MIMachineRecipeTypes.UNPACKER, 1, 2, 0, 0, guiParams -> {},
                new ProgressBar.Params(77, 33, "arrow"), new EnergyBar.Params(18, 30),
                items -> items.addSlots(56, 36, 1, 1).addSlots(102, 27, 1, 2), fluids -> {},
                true, false, false,
                LEVELS_STEEL_TO_IRIDIUM, 16
        );
        // @formatter:on
    }

    public static void registerMachineTiers(String englishName, String machine, MachineRecipeType type, int itemInputCount, int itemOutputCount,
                                            int fluidInputCount,
                                            int fluidOutputCount, Consumer<MachineGuiParameters.Builder> guiParams, ProgressBar.Params progressBarParams,
                                            EnergyBar.Params energyBarParams, Consumer<SlotPositions.Builder> itemPositions,
                                            Consumer<SlotPositions.Builder> fluidPositions, boolean frontOverlay, boolean topOverlay, boolean sideOverlay, List<MachineLevel> levels,
                                            int ioBucketCapacity) {
        registerMachineTiers(englishName, machine, type, itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount,
                guiParams, progressBarParams, progressBarParams, energyBarParams, itemPositions, fluidPositions, frontOverlay,
                topOverlay, sideOverlay, levels, ioBucketCapacity, new Config());
    }

    /**
     * @param guiProgressBarParams    where the progress bar is drawn inside the machine's own screen
     * @param viewerProgressBarParams where the progress bar is drawn in the recipe viewer category. Only pass a value
     *                                that differs from {@code guiProgressBarParams} when the machine screen was
     *                                re-laid-out but the recipe viewer should keep its original appearance.
     */
    public static void registerMachineTiers(String englishName, String machine, MachineRecipeType type, int itemInputCount, int itemOutputCount,
                                            int fluidInputCount,
                                            int fluidOutputCount, Consumer<MachineGuiParameters.Builder> guiParams, ProgressBar.Params guiProgressBarParams,
                                            ProgressBar.Params viewerProgressBarParams,
                                            EnergyBar.Params energyBarParams, Consumer<SlotPositions.Builder> itemPositions,
                                            Consumer<SlotPositions.Builder> fluidPositions, boolean frontOverlay, boolean topOverlay, boolean sideOverlay, List<MachineLevel> levels,
                                            int ioBucketCapacity) {
        registerMachineTiers(englishName, machine, type, itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount,
                guiParams, guiProgressBarParams, viewerProgressBarParams, energyBarParams, itemPositions, fluidPositions,
                frontOverlay, topOverlay, sideOverlay, levels, ioBucketCapacity, new Config());
    }

    public static void registerMachineTiers(String englishName, String machine, MachineRecipeType type, int itemInputCount, int itemOutputCount,
                                            int fluidInputCount,
                                            int fluidOutputCount, Consumer<MachineGuiParameters.Builder> guiParams, ProgressBar.Params guiProgressBarParams,
                                            ProgressBar.Params viewerProgressBarParams,
                                            EnergyBar.Params energyBarParams, Consumer<SlotPositions.Builder> itemPositions,
                                            Consumer<SlotPositions.Builder> fluidPositions, boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
                                            List<MachineLevel> levels,
                                            int ioBucketCapacity, Config extraConfig) {
        for (MachineLevel level : levels) {
            if (isSteam(level)) {
                SlotPositions items = new SlotPositions.Builder().buildWithConsumer(itemPositions);
                SlotPositions fluids = new SlotPositions.Builder().addSlot(12, 35).buildWithConsumer(fluidPositions);
                String prefix = prefixOf(level);
                int steamBuckets = level == MachineLevel.BRONZE ? 2 : 4;
                String id = prefix + machine;
                MachineGuiParameters.Builder guiParamsBuilder = new MachineGuiParameters.Builder(id, true);
                guiParams.accept(guiParamsBuilder);
                MachineGuiParameters builtGuiParams = guiParamsBuilder.build();

                MachineRegistrationHelper.registerMachine(level.getEnglishPrefix() + englishName, id,
                        bet -> new SteamCraftingMachineBlockEntity(bet, type,
                                buildComponent(itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount, items, fluids, steamBuckets,
                                        ioBucketCapacity),
                                builtGuiParams, guiProgressBarParams, level, extraConfig.steamOverclockCatalysts),
                        bet -> {
                            if (itemInputCount + itemOutputCount > 0) {
                                MachineBlockEntity.registerItemApi(bet);
                            }
                            MachineBlockEntity.registerFluidApi(bet);
                        });
                MachineRegistrationHelper.addMachineModel(level, id, machine, frontOverlay, topOverlay, sideOverlay);
            } else {
                SlotPositions items = new SlotPositions.Builder().buildWithConsumer(itemPositions);
                SlotPositions fluids = new SlotPositions.Builder().buildWithConsumer(fluidPositions);

                String id = prefixOf(level) + machine;
                MachineGuiParameters.Builder guiParamsBuilder = new MachineGuiParameters.Builder(id, true);
                guiParams.accept(guiParamsBuilder);
                MachineGuiParameters builtGuiParams = guiParamsBuilder.build();

                String fullEnglishName = levels.size() == 1 ? englishName : level.getEnglishPrefix() + englishName;

                MachineRegistrationHelper.registerMachine(fullEnglishName, id,
                        bet -> new ElectricCraftingMachineBlockEntity(bet, type,
                                buildComponent(itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount, items, fluids, 0,
                                        ioBucketCapacity),
                                builtGuiParams,
                                energyBarParams, guiProgressBarParams, level),
                        bet -> {
                            ElectricCraftingMachineBlockEntity.registerEnergyApi(bet);
                            if (itemInputCount + itemOutputCount > 0) {
                                MachineBlockEntity.registerItemApi(bet);
                            }
                            if (fluidInputCount + fluidOutputCount > 0) {
                                MachineBlockEntity.registerFluidApi(bet);
                            }
                        });
                MachineRegistrationHelper.addMachineModel(level, id, machine, frontOverlay, topOverlay, sideOverlay);
            }
        }

        SlotPositions items = new SlotPositions.Builder().buildWithConsumer(itemPositions);
        SlotPositions fluids = new SlotPositions.Builder().buildWithConsumer(fluidPositions);
        registerReiTiers(englishName, machine, type,
                new MachineCategoryParams(null, null, items.sublist(0, itemInputCount),
                        items.sublist(itemInputCount, itemInputCount + itemOutputCount),
                        fluids.sublist(0, fluidInputCount), fluids.sublist(fluidInputCount, fluidInputCount + fluidOutputCount), viewerProgressBarParams,
                        null, null, false, SteamMode.BOTH),
                levels, guiProgressBarParams);
    }

    /**
     * Registers the single recipe viewer category of a machine family.
     *
     * <p>
     * One machine family is one category, no matter how many levels it exists at, and the category keeps the plain
     * machine name. Which level a recipe needs is shown on the recipe itself, so there is no reason to split the family
     * into one category per level: that only showed the same recipes several times over.
     *
     * @param clickAreaProgressBarParams where the clickable area of a machine screen is placed. This must follow the
     *                                   progress bar that the screen actually draws, so that hovering over the visible
     *                                   bar opens the recipe.
     */
    private static void registerReiTiers(String englishName, String machine, MachineRecipeType recipeType, MachineCategoryParams categoryParams,
                                         List<MachineLevel> levels, ProgressBar.Params clickAreaProgressBarParams) {
        MachineLevel lowest = levels.get(0);
        // The category is registered under the id of the lowest variant, which is also the variant the ids used before
        // the other levels existed. Every variant of the family is then an alternative way to reach it.
        ResourceLocation categoryId = MI.id(prefixOf(lowest) + machine);
        MachineCategoryParams category = new MachineCategoryParams(englishName, categoryId,
                categoryParams.itemInputs, categoryParams.itemOutputs, categoryParams.fluidInputs, categoryParams.fluidOutputs,
                categoryParams.progressBarParams, recipeType, recipe -> true, false,
                isSteam(lowest) ? SteamMode.BOTH : SteamMode.ELECTRIC_ONLY);
        ReiMachineRecipes.registerCategory(categoryId, category);

        for (MachineLevel level : levels) {
            String itemId = prefixOf(level) + machine;
            MachineRegistrationHelper.recordRecipeMachine(recipeType, itemId, level, 1);
            ReiMachineRecipes.registerWorkstation(categoryId, MI.id(itemId));
            ReiMachineRecipes.registerMachineClickArea(MI.id(itemId), clickAreaProgressBarParams.toRectangle());
            ReiMachineRecipes.registerRecipeCategoryForMachine(MI.id(itemId), category.category);
        }
    }

    /**
     * @param steamBuckets Number of steam buckets in the steam input slot, or 0 for
     *                     no steam input slot
     */
    private static MachineInventoryComponent buildComponent(int itemInputCount, int itemOutputCount, int fluidInputCount, int fluidOutputCount,
                                                            SlotPositions itemPositions, SlotPositions fluidPositions, int steamBuckets, int ioBucketCapacity) {
        List<ConfigurableItemStack> itemInputStacks = new ArrayList<>();
        for (int i = 0; i < itemInputCount; ++i) {
            itemInputStacks.add(ConfigurableItemStack.standardInputSlot());
        }
        List<ConfigurableItemStack> itemOutputStacks = new ArrayList<>();
        for (int i = 0; i < itemOutputCount; ++i) {
            itemOutputStacks.add(ConfigurableItemStack.standardOutputSlot());
        }
        List<ConfigurableFluidStack> fluidInputStacks = new ArrayList<>();
        if (steamBuckets > 0) {
            fluidInputStacks.add(ConfigurableFluidStack.lockedInputSlot(1000L * steamBuckets, MIFluids.STEAM.asFluid()));
        }
        for (int i = 0; i < fluidInputCount; ++i) {
            fluidInputStacks.add(ConfigurableFluidStack.standardInputSlot(1000L * ioBucketCapacity));
        }
        List<ConfigurableFluidStack> fluidOutputStacks = new ArrayList<>();
        for (int i = 0; i < fluidOutputCount; ++i) {
            fluidOutputStacks.add(ConfigurableFluidStack.standardOutputSlot(1000L * ioBucketCapacity));
        }

        return new MachineInventoryComponent(itemInputStacks, itemOutputStacks, fluidInputStacks, fluidOutputStacks, itemPositions, fluidPositions);
    }

    public static class Config {
        public List<OverclockComponent.Catalyst> steamOverclockCatalysts = OverclockComponent.getDefaultCatalysts();
    }
}
