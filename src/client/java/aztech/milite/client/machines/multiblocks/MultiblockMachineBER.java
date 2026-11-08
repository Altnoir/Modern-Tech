package aztech.milite.client.machines.multiblocks;

import aztech.milite.client.machines.MachineBlockEntityRenderer;
import aztech.milite.config.MIClientConfig;
import aztech.milite.machines.MachineBlock;
import aztech.milite.machines.multiblocks.HatchBlockEntity;
import aztech.milite.machines.multiblocks.HatchFlags;
import aztech.milite.machines.multiblocks.HatchType;
import aztech.milite.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.milite.machines.multiblocks.ShapeMatcher;
import aztech.milite.machines.multiblocks.ShapeTemplate;
import aztech.milite.machines.multiblocks.SimpleMember;
import aztech.milite.MITags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MultiblockMachineBER extends MachineBlockEntityRenderer<MultiblockMachineBlockEntity> {
    private static final int HIGHLIGHT_CACHE_TICKS = 10;
    private static final Map<MultiblockMachineBlockEntity, HighlightScan> highlightScans = new WeakHashMap<>();

    public MultiblockMachineBER(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    private static boolean isClose(BlockPos pos, double maxDistanceSqr) {
        Player player = Minecraft.getInstance().player;
        return player == null || player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= maxDistanceSqr;
    }

    /**
     * Returns the cached scan of the shape, recomputed at most once every {@link #HIGHLIGHT_CACHE_TICKS} ticks and
     * invalidated by a shape change, a rotation or a shape validity change.
     */
    private static HighlightScan getScan(MultiblockMachineBlockEntity be, boolean shapeValid) {
        Level level = be.getLevel();
        ShapeTemplate shape = be.getActiveShape();
        Direction facing = be.getOrientation().facingDirection;
        long timeBucket = level.getGameTime() / HIGHLIGHT_CACHE_TICKS;

        HighlightScan cached = highlightScans.get(be);
        if (cached != null && cached.shape() == shape && cached.facing() == facing && cached.shapeValid() == shapeValid
                && cached.timeBucket() == timeBucket) {
            return cached;
        }

        HighlightScan scan = scan(be, level, shape, facing, shapeValid, timeBucket);
        highlightScans.put(be, scan);
        return scan;
    }

    private static HighlightScan scan(MultiblockMachineBlockEntity be, Level level, ShapeTemplate shape, Direction facing, boolean shapeValid,
            long timeBucket) {
        ShapeMatcher matcher = be.createShapeMatcher();
        List<BlockedPos> blocked = new ArrayList<>();
        List<MissingPreview> previews = new ArrayList<>();
        List<HatchPreview> hatches = new ArrayList<>();

        for (BlockPos pos : matcher.getPositions()) {
            SimpleMember member = matcher.getSimpleMember(pos);
            HatchFlags hatchFlags = matcher.getHatchFlags(pos);
            if (hatchFlags != null) {
                hatches.add(new HatchPreview(pos, hatchFlags, member));
            }

            if (shapeValid || matcher.matches(pos, level)) {
                // A completed shape has no blocked or missing position, so only the hatch positions are needed.
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (ShapeMatcher.canReplaceForBuild(state)) {
                BlockState previewState = ShapeMatcher.toWorldState(level, pos, member.getPreviewState(), matcher.controllerDirection);
                previews.add(new MissingPreview(pos, previewState, member.newBlockEntity(level.registryAccess(), level, pos, previewState), member, hatchFlags));
            } else {
                blocked.add(new BlockedPos(pos, member, hatchFlags));
            }
        }

        return new HighlightScan(shape, facing, shapeValid, timeBucket, List.copyOf(blocked), List.copyOf(previews), List.copyOf(hatches));
    }

    /**
     * Whether the player's look ray hits the given block position.
     */
    private static boolean isTargetingPos(BlockPos pos) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        var eyePos = player.getEyePosition();
        var view = player.getViewVector(1).scale(player.blockInteractionRange());
        var endPos = eyePos.add(view);
        return new AABB(pos).clip(eyePos, endPos).isPresent();
    }

    @Override
    public void render(MultiblockMachineBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int light, int overlay) {
        super.render(be, tickDelta, matrices, vcp, light, overlay);

        int maxHighlightDistance = MIClientConfig.INSTANCE.maxMultiblockHighlightOverlayRange.getAsInt();
        if (maxHighlightDistance == 0) {
            highlightScans.remove(be);
            return;
        }

        boolean shapeValid = be.isShapeValid();
        boolean holdingWrench = isHoldingWrench();
        boolean drawWrenchHighlights = holdingWrench && !shapeValid;
        HatchType hatchType = getHeldHatchType();

        if ((!drawWrenchHighlights && hatchType == null) || be.getLevel() == null) {
            // Nothing to draw. In particular, a completed shape is never scanned again.
            highlightScans.remove(be);
            return;
        }

        HighlightScan scan = getScan(be, shapeValid);
        double maxDistanceSqr = (double) maxHighlightDistance * maxHighlightDistance;

        boolean tooltipOverlay = MIClientConfig.INSTANCE.multiblockTooltipOverlay.getAsBoolean();

        if (hatchType != null && MIClientConfig.INSTANCE.hatchPlacementOverlay.getAsBoolean()) {
            for (HatchPreview hatch : scan.hatches()) {
                if (hatch.flags().allows(hatchType) && isClose(hatch.pos(), maxDistanceSqr)) {
                    MultiblockHighlight.enqueueHatchHighlight(hatch.pos());
                    if (tooltipOverlay && isTargetingPos(hatch.pos())) {
                        MultiblockHighlight.enqueueHighlightTargetPos(hatch.pos(), hatch.member(), hatch.flags());
                    }
                }
            }
        }

        if (drawWrenchHighlights) {
            for (MissingPreview preview : scan.previews()) {
                MultiblockHighlight.registerPreview(preview.pos(), preview.state(), preview.blockEntity());
                if (isClose(preview.pos(), maxDistanceSqr)) {
                    MultiblockHighlight.enqueueErrorHighlight(preview.pos(), preview.state(), preview.blockEntity());
                    if (tooltipOverlay && isTargetingPos(preview.pos())) {
                        MultiblockHighlight.enqueueHighlightTargetPos(preview.pos(), preview.member(), preview.hatchFlags());
                    }
                }
            }
            for (BlockedPos blockedPos : scan.blocked()) {
                if (isClose(blockedPos.pos(), maxDistanceSqr)) {
                    MultiblockHighlight.enqueueOutlineHighlight(blockedPos.pos());
                    if (tooltipOverlay && isTargetingPos(blockedPos.pos())) {
                        MultiblockHighlight.enqueueHighlightTargetPos(blockedPos.pos(), blockedPos.member(), blockedPos.hatchFlags());
                    }
                }
            }
        }
    }

    private record BlockedPos(BlockPos pos, SimpleMember member, @Nullable HatchFlags hatchFlags) {}

    private record MissingPreview(BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, SimpleMember member,
            @Nullable HatchFlags hatchFlags) {}

    private record HatchPreview(BlockPos pos, HatchFlags flags, SimpleMember member) {}

    private record HighlightScan(ShapeTemplate shape, Direction facing, boolean shapeValid, long timeBucket, List<BlockedPos> blocked,
            List<MissingPreview> previews, List<HatchPreview> hatches) {}

    private static boolean isHoldingWrench() {
        Player player = Minecraft.getInstance().player;
        return player != null && (player.getMainHandItem().is(MITags.WRENCHES) || player.getOffhandItem().is(MITags.WRENCHES));
    }

    @Nullable
    private static HatchType getHeldHatchType() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        HatchType mainHand = getHatchType(player.getMainHandItem());
        HatchType offHand = getHatchType(player.getOffhandItem());
        return mainHand == null ? offHand : mainHand;
    }

    @Nullable
    private static HatchType getHatchType(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof MachineBlock block) {
                BlockEntity be = block.newBlockEntity(new BlockPos(0, 0, 0), block.defaultBlockState());
                if (be instanceof HatchBlockEntity hatch) {
                    return hatch.getHatchType();
                }
            }
        }
        return null;
    }

    @Override
    public boolean shouldRenderOffScreen(MultiblockMachineBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(MultiblockMachineBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
