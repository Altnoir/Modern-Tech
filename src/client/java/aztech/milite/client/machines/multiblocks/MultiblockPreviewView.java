package aztech.milite.client.machines.multiblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/** Read-only view that overlays the pending preview blocks onto the real client world. */
record MultiblockPreviewView(BlockAndTintGetter world, Map<BlockPos, Preview> previews) implements BlockAndTintGetter {
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Preview preview = previews.get(pos);
        return preview == null ? world.getBlockState(pos) : preview.state();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Preview preview = previews.get(pos);
        return preview == null ? world.getBlockEntity(pos) : preview.blockEntity();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        Preview preview = previews.get(pos);
        return preview == null ? world.getFluidState(pos) : preview.state().getFluidState();
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        Preview preview = previews.get(pos);
        if (preview == null) return world.getModelData(pos);
        return preview.blockEntity() == null ? ModelData.EMPTY : preview.blockEntity().getModelData();
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return world.getShade(direction, shade);
    }

    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return world.getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return world.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return world.getBlockTint(pos, resolver);
    }

    @Override
    public @Nullable AuxiliaryLightManager getAuxLightManager(ChunkPos pos) {
        return world.getAuxLightManager(pos);
    }

    @Override
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return world.getMinBuildHeight();
    }

    record Preview(BlockState state, @Nullable BlockEntity blockEntity) {}
}
