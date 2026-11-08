package aztech.milite.client.machines.multiblocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

final class MultiblockPreviewRenderer {
    private static final Map<RenderType, RenderType> PREVIEW_TYPES = new IdentityHashMap<>();
    private static final MultiBufferSource PREVIEW_BUFFERS = type -> new VertexConsumerWrapper(
            MultiblockHighlight.previewBuffer(translucent(type))) {
        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return super.setColor(red, green, blue, Math.round(alpha * 0.5f));
        }
    };

    private MultiblockPreviewRenderer() {}

    private static RenderType translucent(RenderType original) {
        return PREVIEW_TYPES.computeIfAbsent(original, type -> new RenderType("moderntech_preview", type.format(), type.mode(),
                type.bufferSize(), false, true, () -> {
                    type.setupRenderState();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.depthMask(false);
                }, () -> {
                    RenderSystem.depthMask(true);
                    type.clearRenderState();
                    RenderSystem.disableBlend();
                }) {});
    }

    static void render(BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, PoseStack pose,
            MultiblockPreviewView previewView, Predicate<BlockPos> fullPreviewAt) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) return;

        if (state.getRenderShape() == RenderShape.MODEL) {
            var dispatcher = minecraft.getBlockRenderer();
            BakedModel model = dispatcher.getBlockModel(state);
            boolean fullBlock = Block.isShapeFullBlock(state.getShape(previewView, pos));
            if (fullBlock) {
                Set<Direction> covered = EnumSet.noneOf(Direction.class);
                for (Direction direction : Direction.values()) {
                    if (fullPreviewAt.test(pos.relative(direction))) covered.add(direction);
                }
                if (!covered.isEmpty()) model = new CulledPreviewModel(model, covered);
            }
            ModelData data = blockEntity == null ? ModelData.EMPTY : blockEntity.getModelData();
            data = model.getModelData(previewView, pos, state, data);
            int tint = minecraft.getBlockColors().getColor(state, previewView, pos, 0);
            float red = (tint >> 16 & 255) / 255f;
            float green = (tint >> 8 & 255) / 255f;
            float blue = (tint & 255) / 255f;
            for (RenderType layer : model.getRenderTypes(state, RandomSource.create(42), data)) {
                dispatcher.getModelRenderer().renderModel(pose.last(), PREVIEW_BUFFERS.getBuffer(RenderTypeHelper.getEntityRenderType(layer, false)),
                        state, model, red, green, blue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, data, layer);
            }
        }
        if (blockEntity != null && minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity) != null) {
            minecraft.getBlockEntityRenderDispatcher().render(blockEntity, 0, pose, PREVIEW_BUFFERS);
        }
    }

    private static final class CulledPreviewModel extends BakedModelWrapper<BakedModel> {
        private final Set<Direction> covered;

        private CulledPreviewModel(BakedModel model, Set<Direction> covered) {
            super(model);
            this.covered = covered;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                ModelData data, @Nullable RenderType layer) {
            return originalModel.getQuads(state, side, random, data, layer).stream()
                    .filter(quad -> !covered(quad)).toList();
        }

        private boolean covered(BakedQuad quad) {
            Direction direction = quad.getDirection();
            if (!covered.contains(direction)) return false;
            int[] vertices = quad.getVertices();
            int stride = vertices.length / 4;
            int axis = direction.getAxis().ordinal();
            float boundary = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1f : 0f;
            for (int i = 0; i < 4; i++) {
                float coordinate = Float.intBitsToFloat(vertices[i * stride + axis]);
                if (Math.abs(coordinate - boundary) > 0.001f) return false;
            }
            return true;
        }
    }
}
