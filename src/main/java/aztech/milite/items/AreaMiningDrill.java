package aztech.milite.items;

import aztech.milite.MIText;
import aztech.milite.items.SteamDrillItem.Area;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface AreaMiningDrill extends ActivatableItem {
    @Nullable
    default Area getArea(BlockGetter level, Player player, ItemStack stack, boolean rayTraceOnly) {
        return isActivated(stack) && !player.isShiftKeyDown() ? SteamDrillItem.findArea(level, player, rayTraceOnly) : null;
    }

    default boolean canMineAreaBlock(ItemStack stack, BlockState state) {
        return true;
    }

    default void toggleMiningArea(Player player, ItemStack stack) {
        if (!player.level().isClientSide()) {
            boolean activated = !isActivated(stack);
            setActivated(stack, activated);
            player.displayClientMessage((activated ? MIText.ToolSwitched3x3 : MIText.ToolSwitchedNo3x3).text(), true);
        }
    }
}
