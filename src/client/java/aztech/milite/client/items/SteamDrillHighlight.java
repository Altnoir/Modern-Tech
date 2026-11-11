package aztech.milite.client.items;

import aztech.milite.items.AreaMiningDrill;
import aztech.milite.items.SteamDrillHooks;
import aztech.milite.items.SteamDrillItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Objects;

public class SteamDrillHighlight {
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        var player = Objects.requireNonNull(Minecraft.getInstance().player);

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof AreaMiningDrill drillItem)) {
            return;
        }

        var area = drillItem.getArea(player.level(), player, stack, true);
        if (area == null) {
            return;
        }

        var origin = area.center();
        MutableObject<VoxelShape> fullShape = new MutableObject<>(Shapes.empty());

        SteamDrillItem.forEachMineableBlock(player.level(), area, player, (blockPos, state) -> {
            if (!drillItem.canMineAreaBlock(stack, state)) {
                return;
            }
            // Use raw method to skip our BlockStateBase mixin...
            float destroyProgress = SteamDrillHooks.getDestroyProgressRaw(state, player, player.level(), blockPos);
            if (destroyProgress <= 1e-9) {
                return;
            }

            var blockShape = state.getShape(player.level(), blockPos, CollisionContext.of(event.getCamera().getEntity()));
            blockShape = blockShape.move(blockPos.getX() - origin.getX(), blockPos.getY() - origin.getY(), blockPos.getZ() - origin.getZ());

            fullShape.setValue(Shapes.joinUnoptimized(fullShape.getValue(), blockShape, BooleanOp.OR));
        });

        if (fullShape.getValue() == Shapes.empty()) {
            return;
        }

        LevelRenderer.renderShape(
                event.getPoseStack(),
                event.getMultiBufferSource().getBuffer(RenderType.lines()),
                fullShape.getValue(),
                origin.getX() - event.getCamera().getPosition().x(),
                origin.getY() - event.getCamera().getPosition().y(),
                origin.getZ() - event.getCamera().getPosition().z(),
                0.0F,
                0.0F,
                0.0F,
                0.4F);
        event.setCanceled(true);
    }
}
