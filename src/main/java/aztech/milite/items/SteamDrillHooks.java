package aztech.milite.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class SteamDrillHooks {
    private SteamDrillHooks() {}

    private static final ThreadLocal<Boolean> canOverride = ThreadLocal.withInitial(() -> true);

    public static float getDestroyProgressRaw(BlockState state, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
        canOverride.set(false);
        try {
            return state.getDestroyProgress(pPlayer, pLevel, pPos);
        } finally {
            canOverride.set(true);
        }
    }

    public static void overrideDestroyProgress(Player pPlayer, BlockGetter pLevel, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = pPlayer.getMainHandItem();
        if (!(stack.getItem() instanceof AreaMiningDrill drillItem) || !canOverride.get()) {
            return;
        }

        var area = drillItem.getArea(pLevel, pPlayer, stack, false);
        if (area == null) {
            return;
        }

        MutableFloat minProgress = new MutableFloat(Float.MAX_VALUE);
        MutableBoolean foundAny = new MutableBoolean(false);

        SteamDrillItem.forEachMineableBlock(pLevel, area, pPlayer, (blockPos, state) -> {
            if (!drillItem.canMineAreaBlock(stack, state)) {
                return;
            }
            // Disable this hook during the call to avoid infinite recursion...
            float destroyProgress = getDestroyProgressRaw(state, pPlayer, pLevel, blockPos);

            if (destroyProgress > 1e-9) {
                foundAny.setTrue();
                minProgress.setValue(Math.min(minProgress.getValue(), destroyProgress));
            }
        });

        if (foundAny.isTrue()) {
            cir.setReturnValue(minProgress.getValue());
            cir.cancel();
        }
    }

    /**
     * Client-side only. Forces a block break reset when the targeted side changes.
     * Server doesn't need this because it handles block breaking progress differently.
     */
    @Nullable
    public static Direction breakingSide = null;

    private static void onStartBreaking(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getUseItem().isFalse() || event.getSide().isServer()) {
            return;
        }

        breakingSide = event.getFace();
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, SteamDrillHooks::onStartBreaking);
    }
}
