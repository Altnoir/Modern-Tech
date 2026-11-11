package aztech.milite.blocks.storage.waterbarrel;

import aztech.milite.MIBlock;
import aztech.milite.MIRegistries;
import aztech.milite.api.machine.component.FluidAccess;
import aztech.milite.api.machine.holder.FluidStorageComponentHolder;
import aztech.milite.inventory.MIInventory;
import aztech.milite.machines.BEP;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.MachineComponent;
import aztech.milite.machines.components.OrientationComponent;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.gui.MachineMenuServer;
import aztech.milite.machines.guicomponents.LargeTankFluidDisplay;
import aztech.milite.machines.models.MachineModelClientData;
import aztech.milite.machines.multiblocks.world.ChunkEventListener;
import aztech.milite.machines.multiblocks.world.ChunkEventListeners;
import aztech.milite.sounds.MISounds;
import aztech.milite.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.milite.util.Tickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jspecify.annotations.Nullable;

public class WaterBarrelBlockEntity extends MachineBlockEntity implements MachineComponent, Tickable, FluidStorageComponentHolder {
    private static final int CAPACITY = Integer.MAX_VALUE;
    private static final int WATER_PER_TICK = 1_000_000;
    private static final FluidVariant WATER = FluidVariant.of(Fluids.WATER);
    private final FluidAccess fluidStorage = new FluidAccess() {
        @Override
        public FluidVariant getVariant() {
            return water == 0 ? FluidVariant.blank() : WATER;
        }

        @Override
        public long getAmount() {
            return water;
        }

        @Override
        public long getCapacity() {
            return CAPACITY;
        }
    };
    private final ChunkEventListener chunkListener = new ChunkEventListener() {
        @Override
        public void onBlockUpdate(BlockPos pos) {}

        @Override
        public void onLoad() {
            if (level instanceof ServerLevel serverLevel) {
                scheduleCheck(serverLevel);
            }
        }

        @Override
        public void onUnload() {
            if (level instanceof ServerLevel serverLevel) {
                suspend(serverLevel);
            }
        }
    };
    @Nullable
    private BlockPos controllerPos;
    private int water;
    private boolean online;
    private int menuRevision;
    private boolean watchingChunks;

    public WaterBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(new BEP(MIRegistries.WATER_BARREL_BE.get(), pos, state),
                new MachineGuiParameters.Builder("water_barrel", false).backgroundHeight(185).build(),
                OrientationComponent.Params.noFacingNoOutput());
        registerComponents(this);
        registerGuiComponent(new LargeTankFluidDisplay(this::getFluidData));
    }

    private static boolean tryForm(ServerLevel level, BlockPos center) {
        if (!allChunksLoaded(level, center) || find(level, center) == null) {
            return false;
        }
        for (BlockPos pos : cube(center)) {
            var barrel = find(level, pos);
            // A wet former controller keeps its water for a rebuild around the same center.
            if (barrel == null || barrel.controllerPos != null || (barrel.water > 0 && !pos.equals(center))) {
                return false;
            }
        }

        BlockPos controllerPos = center.immutable();
        var controller = find(level, controllerPos);
        for (BlockPos pos : cube(controllerPos)) {
            find(level, pos).link(controllerPos);
        }
        controller.watchChunks(level);
        controller.enable(level);
        return true;
    }

    private static void disband(ServerLevel level, BlockPos center) {
        var controller = find(level, center);
        if (controller != null && controller.isController()) {
            controller.setOnline(level, false);
            controller.unwatchChunks(level);
        }
        for (BlockPos pos : cube(center)) {
            var barrel = find(level, pos);
            if (barrel != null && center.equals(barrel.controllerPos)) {
                barrel.link(null);
                barrel.setOnline(level, false);
                barrel.scheduleCheck(level);
            }
        }
    }

    private static boolean allChunksLoaded(ServerLevel level, BlockPos center) {
        return level.hasChunksAt(center.offset(-1, 0, -1), center.offset(1, 0, 1));
    }

    private static Iterable<BlockPos> cube(BlockPos center) {
        return BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1));
    }

    @Nullable
    private static WaterBarrelBlockEntity find(ServerLevel level, BlockPos pos) {
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof WaterBarrelBlockEntity barrel && !barrel.isRemoved()) {
            return barrel;
        }
        return null;
    }

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    public MachineModelClientData getMachineModelData() {
        return new MachineModelClientData();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            scheduleCheck(serverLevel);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel && watchingChunks) {
            suspend(serverLevel);
            unwatchChunks(serverLevel);
        }
        super.setRemoved();
    }

    private void scheduleCheck(ServerLevel level) {
        level.scheduleTick(worldPosition, MIBlock.WATER_BARREL.asBlock(), 1);
    }

    boolean checkStructure(ServerLevel level) {
        if (controllerPos != null) {
            if (!level.hasChunkAt(controllerPos)) {
                return false;
            }
            var controller = find(level, controllerPos);
            if (controller != null && controller.isController()) {
                controller.resume(level);
                return false;
            }
            link(null);
        }

        for (BlockPos candidate : cube(worldPosition)) {
            if (tryForm(level, candidate)) {
                return true;
            }
        }
        return false;
    }

    private void resume(ServerLevel level) {
        if (online) {
            return;
        }
        watchChunks(level);
        if (!allChunksLoaded(level, worldPosition)) {
            return;
        }
        for (BlockPos pos : cube(worldPosition)) {
            var barrel = find(level, pos);
            if (barrel == null || !worldPosition.equals(barrel.controllerPos)) {
                disband(level, worldPosition);
                return;
            }
        }
        enable(level);
    }

    private void enable(ServerLevel level) {
        setOnline(level, true);
        for (BlockPos pos : cube(worldPosition)) {
            find(level, pos).setOnline(level, true);
        }
    }

    private void suspend(ServerLevel level) {
        setOnline(level, false);
        for (BlockPos pos : cube(worldPosition)) {
            var barrel = find(level, pos);
            if (barrel != null && worldPosition.equals(barrel.controllerPos)) {
                barrel.setOnline(level, false);
            }
        }
    }

    void onBroken() {
        if (level instanceof ServerLevel serverLevel && controllerPos != null) {
            disband(serverLevel, controllerPos);
        }
    }

    private void link(@Nullable BlockPos center) {
        controllerPos = center;
        setChanged();
    }

    private void setOnline(ServerLevel level, boolean value) {
        if (online != value) {
            online = value;
            menuRevision++;
            sync(false);
            level.invalidateCapabilities(worldPosition);
        }
    }

    private void watchChunks(ServerLevel level) {
        if (!watchingChunks) {
            ChunkPos.rangeClosed(new ChunkPos(worldPosition.offset(-1, 0, -1)), new ChunkPos(worldPosition.offset(1, 0, 1)))
                    .forEach(chunk -> ChunkEventListeners.listeners.add(level, chunk, chunkListener));
            watchingChunks = true;
        }
    }

    private void unwatchChunks(ServerLevel level) {
        if (watchingChunks) {
            ChunkPos.rangeClosed(new ChunkPos(worldPosition.offset(-1, 0, -1)), new ChunkPos(worldPosition.offset(1, 0, 1)))
                    .forEach(chunk -> ChunkEventListeners.listeners.remove(level, chunk, chunkListener));
            watchingChunks = false;
        }
    }

    private boolean isController() {
        return worldPosition.equals(controllerPos);
    }

    @Nullable
    private WaterBarrelBlockEntity getController() {
        if (!online || isRemoved() || controllerPos == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        var controller = find(serverLevel, controllerPos);
        return controller != null && controller.online && controller.isController() && allChunksLoaded(serverLevel, controllerPos)
                ? controller : null;
    }

    boolean canAccess(@Nullable Direction side) {
        if (!online || controllerPos == null || isController()) {
            return false;
        }
        return side == null
                || (worldPosition.getX() - controllerPos.getX()) * side.getStepX()
                        + (worldPosition.getY() - controllerPos.getY()) * side.getStepY()
                        + (worldPosition.getZ() - controllerPos.getZ()) * side.getStepZ() == 1;
    }

    @Override
    public void tick() {
        if (online && isController() && level instanceof ServerLevel serverLevel && water < CAPACITY) {
            if (allChunksLoaded(serverLevel, worldPosition)) {
                water += Math.min(WATER_PER_TICK, CAPACITY - water);
                setChanged();
            } else {
                suspend(serverLevel);
            }
        }
    }

    @Override
    public void openMenu(ServerPlayer player) {
        var controller = getController();
        if (controller == null || !canAccess(null)) {
            return;
        }
        int revision = controller.menuRevision;
        var serverLevel = player.serverLevel();
        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> {
            var menu = new MachineMenuServer(id, inventory, this, guiParams) {
                @Override
                public boolean stillValid(Player player) {
                    return super.stillValid(player) && controller.menuRevision == revision && getController() == controller;
                }

                @Override
                public void removed(Player player) {
                    super.removed(player);
                    if (player.level() == serverLevel) {
                        serverLevel.playSound(null, worldPosition, MISounds.WATER_BARREL_CLOSE.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
                    }
                }
            };
            serverLevel.playSound(null, worldPosition, MISounds.WATER_BARREL_OPEN.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            return menu;
        }, getDisplayName()), this::writeScreenOpeningData);
    }

    @Override
    @Nullable
    public FluidAccess getFluidStorageComponent() {
        var controller = getController();
        return controller == null ? null : controller.fluidStorage;
    }

    private LargeTankFluidDisplay.Data getFluidData() {
        var storage = getFluidStorageComponent();
        return storage == null ? new LargeTankFluidDisplay.Data(FluidVariant.blank(), 0, CAPACITY)
                : new LargeTankFluidDisplay.Data(storage.getVariant(), storage.getAmount(), storage.getCapacity());
    }

    @Nullable
    public IFluidHandler getDrainHandler(@Nullable Direction side) {
        var controller = getController();
        return controller != null && canAccess(side) ? new DrainHandler(controller) : null;
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        if (controllerPos != null) {
            tag.putLong("waterBarrelController", controllerPos.asLong());
        }
        if (water > 0) {
            tag.putInt("waterAmount", water);
        }
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        readController(tag);
        water = Math.clamp(tag.getInt("waterAmount"), 0, CAPACITY);
    }

    @Override
    public void writeClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("waterBarrelOnline", online);
        if (online && controllerPos != null) {
            tag.putLong("waterBarrelController", controllerPos.asLong());
        }
    }

    @Override
    public void readClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        online = tag.getBoolean("waterBarrelOnline");
        readController(tag);
    }

    private void readController(CompoundTag tag) {
        controllerPos = null;
        if (tag.contains("waterBarrelController", Tag.TAG_LONG)) {
            BlockPos center = BlockPos.of(tag.getLong("waterBarrelController"));
            if (Math.abs(center.getX() - worldPosition.getX()) <= 1
                    && Math.abs(center.getY() - worldPosition.getY()) <= 1
                    && Math.abs(center.getZ() - worldPosition.getZ()) <= 1) {
                controllerPos = center;
            }
        }
    }

    private final class DrainHandler implements IFluidHandler {
        private final WaterBarrelBlockEntity controller;

        private DrainHandler(WaterBarrelBlockEntity controller) {
            this.controller = controller;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? drain(CAPACITY, FluidAction.SIMULATE) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.getFluid() == Fluids.WATER && resource.getComponentsPatch().isEmpty()
                    ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || getController() != controller) {
                return FluidStack.EMPTY;
            }
            int amount = Math.min(maxDrain, controller.water);
            if (amount == 0) {
                return FluidStack.EMPTY;
            }
            if (action.execute()) {
                controller.water -= amount;
                controller.setChanged();
            }
            return new FluidStack(Fluids.WATER, amount);
        }
    }
}
