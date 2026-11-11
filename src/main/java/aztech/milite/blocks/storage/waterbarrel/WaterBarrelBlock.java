package aztech.milite.blocks.storage.waterbarrel;

import aztech.milite.MI;
import aztech.milite.MIAdvancementTriggers;
import aztech.milite.blocks.TickableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jspecify.annotations.Nullable;

public class WaterBarrelBlock extends Block implements TickableBlock {
    private static final int BUCKET_VOLUME = 1000;

    public WaterBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public WaterBarrelBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaterBarrelBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (level instanceof ServerLevel && !oldState.is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel && placer instanceof ServerPlayer player
                && level.getBlockEntity(pos) instanceof WaterBarrelBlockEntity barrel && barrel.checkStructure(serverLevel)) {
            MIAdvancementTriggers.BUILT_MULTIBLOCK.get().trigger(player, MI.id("water_barrel"), null);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof WaterBarrelBlockEntity barrel) {
            barrel.checkStructure(level);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (level instanceof ServerLevel && !newState.is(this) && level.getBlockEntity(pos) instanceof WaterBarrelBlockEntity barrel) {
            barrel.onBroken();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof WaterBarrelBlockEntity barrel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        InteractionHand bucketHand = hand == InteractionHand.MAIN_HAND && stack.isEmpty() ? InteractionHand.OFF_HAND : hand;
        ItemStack bucket = player.getItemInHand(bucketHand);
        if (!bucket.is(Items.BUCKET)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        var handler = barrel.getDrainHandler(hit.getDirection());
        if (handler == null || handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE).getAmount() < BUCKET_VOLUME) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
        player.setItemInHand(bucketHand, ItemUtils.createFilledResult(bucket, player, Items.WATER_BUCKET.getDefaultInstance()));
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof WaterBarrelBlockEntity barrel && barrel.canAccess(hit.getDirection())) {
            if (player instanceof ServerPlayer serverPlayer) {
                barrel.openMenu(serverPlayer);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
