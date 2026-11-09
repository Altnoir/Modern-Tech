package aztech.milite.machines.components;

import aztech.milite.api.energy.EnergyTier;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.MachineComponent;
import aztech.milite.machines.models.MachineCasing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class CasingComponent implements MachineComponent, DropableComponent {
    private EnergyTier currentTier = EnergyTier.ELECTRIC;

    @Nullable
    private final UpdatedCallback callback;

    private ItemStack casingStack = ItemStack.EMPTY;
    public CasingComponent(@Nullable UpdatedCallback callback, EnergyTier initialTier) {
        this(callback);
        this.currentTier = initialTier;
    }
    private int speedMultiplier = 1;

    public CasingComponent(@Nullable UpdatedCallback callback) {
        this.callback = callback;
    }

    public CasingComponent() {
        this((UpdatedCallback) null);
    }

    public CasingComponent(EnergyTier initialTier) {
        this(null, initialTier);
    }

    /**
     * @param speedMultiplier how many times faster than the reference level the machine runs. It scales the FE buffer,
     *                        because a machine that eats {@code n} times as much per tick needs {@code n} times as
     *                        much energy waiting for it to keep running at full speed between two insertions.
     */
    public CasingComponent(EnergyTier initialTier, int speedMultiplier) {
        this(null, initialTier);
        this.speedMultiplier = Math.max(1, speedMultiplier);
    }

    @Nullable
    public static EnergyTier getCasingTier(Item item) {
        var itemKey = BuiltInRegistries.ITEM.getKey(item);
        for (var tier : EnergyTier.allTiers()) {
            if (tier.itemKey != null && tier.itemKey.equals(itemKey)) {
                return tier;
            }
        }
        return null;
    }

    /**
     * Sets the current casing stack and update {@link #currentTier} accordingly.
     */
    private void setCasingStack(ItemStack stack) {
        casingStack = stack;

        EnergyTier storedTier = getCasingTier(stack.getItem());
        if (storedTier != null) {
            currentTier = storedTier;
        }
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("casing", casingStack.saveOptional(registries));
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        ItemStack stack = ItemStack.parseOptional(registries, tag.getCompound("casing"));
        if (!stack.isEmpty()) {
            setCasingStack(stack);
        }
    }

    @Override
    public void writeClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("casing", currentTier.name);
    }

    @Override
    public void readClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        currentTier = EnergyTier.getTier(tag.getString("casing"));
    }

    public void dropCasing(Level world, BlockPos pos) {
        Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), casingStack);
    }

    public ItemInteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.getCount() >= 1) {
            var previousTier = currentTier;
            var newTier = getCasingTier(stackInHand.getItem());
            if (newTier != null && newTier != currentTier) {
                if (currentTier != EnergyTier.ELECTRIC) {
                    dropCasing(be.getLevel(), be.getBlockPos());
                }
                setCasingStack(stackInHand.copyWithCount(1));
                stackInHand.consume(1, player);
                be.setChanged();
                if (!be.getLevel().isClientSide()) {
                    be.sync();
                }
                be.getLevel().updateNeighborsAt(be.getBlockPos(), Blocks.AIR);
                playCasingPlaceSound(be);
                if (callback != null) {
                    callback.onUpdated(previousTier, currentTier);
                }
                return ItemInteractionResult.sidedSuccess(be.getLevel().isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void playCasingPlaceSound(MachineBlockEntity be) {
        var blockKey = currentTier.itemKey;
        if (blockKey == null) {
            return;
        }

        BuiltInRegistries.BLOCK.getOptional(blockKey).ifPresent(block -> {
            var casingState = block.defaultBlockState();
            var group = casingState.getSoundType();
            var sound = group.getBreakSound();
            be.getLevel().playSound(null, be.getBlockPos(), sound, SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 4.0F, group.getPitch() * 0.8F);
        });
    }

    /**
     * @return The FE buffer capacity granted by the current casing. It is proportional to the tier of the
     * casing and the machine's level multiplier, capped by the largest registered energy tier.
     */
    public long getFeCapacity() {
        return currentTier.getMachineBufferCapacity(speedMultiplier);
    }

    @Override
    public ItemStack getDrop() {
        return casingStack;
    }

    public void setCasingServer(MachineBlockEntity be, ItemStack casing) {
        var previousTier = currentTier;
        setCasingStack(casing);
        be.setChanged();
        be.sync();
        be.getLevel().updateNeighborsAt(be.getBlockPos(), Blocks.AIR);
        playCasingPlaceSound(be);
        if (callback != null && previousTier != currentTier) {
            callback.onUpdated(previousTier, currentTier);
        }
    }

    public @Nullable MachineCasing getCasing() {
        return casingStack.isEmpty() ? null : currentTier.casing;
    }

    @FunctionalInterface
    public interface UpdatedCallback {
        void onUpdated(EnergyTier from, EnergyTier to);
    }
}
