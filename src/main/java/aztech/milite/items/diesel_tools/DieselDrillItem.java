package aztech.milite.items.diesel_tools;

import aztech.milite.MICommonProxy;
import aztech.milite.MIText;
import aztech.milite.items.AreaMiningDrill;
import aztech.milite.items.FluidFuelItemHelper;
import aztech.milite.items.SteamDrillItem;
import aztech.milite.util.TextHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class DieselDrillItem extends DieselToolItem implements AreaMiningDrill {
    private boolean miningArea;

    public DieselDrillItem(Properties settings) {
        super(settings, 7);
    }

    @Override
    public boolean getDefaultActivatedState() {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            toggleMiningArea(player, stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player != null && context.getHand() == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
            toggleMiningArea(player, context.getItemInHand());
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        return super.useOn(context);
    }

    @Override
    public boolean canMineAreaBlock(ItemStack stack, BlockState state) {
        return isCorrectToolForDrops(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean canMine = canMineAreaBlock(stack, state);
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (miningArea || !canMine || !(miner instanceof ServerPlayer player) || FluidFuelItemHelper.getAmount(stack) <= 0) {
            return result;
        }
        var area = getArea(level, player, stack, false);
        if (area == null || !area.center().equals(pos)) {
            return result;
        }
        miningArea = true;
        try {
            SteamDrillItem.forEachMineableBlock(level, area, player, (target, targetState) -> {
                if (!target.equals(pos) && canMineAreaBlock(stack, targetState) && level.mayInteract(player, target)) {
                    player.gameMode.destroyBlock(target);
                }
            });
        } finally {
            miningArea = false;
        }
        return result;
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !newStack.is(this) || isActivated(oldStack) != isActivated(newStack)
                || FluidFuelItemHelper.getAmount(newStack) <= 0 || MICommonProxy.INSTANCE.shouldSteamDrillForceBreakReset();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(MIText.MiningArea
                .text((isActivated(stack) ? MIText.MiningArea3x3 : MIText.MiningArea1x1).text().setStyle(TextHelper.NUMBER_TEXT))
                .setStyle(TextHelper.GRAY_TEXT.withItalic(false)));
    }
}
