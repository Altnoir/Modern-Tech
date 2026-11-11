package aztech.milite.items.diesel_tools;

import aztech.milite.api.datamaps.FluidFuel;
import aztech.milite.fluid.MIFluid;
import aztech.milite.items.DynamicToolItem;
import aztech.milite.items.FluidFuelItemHelper;
import aztech.milite.items.ItemHelper;
import aztech.milite.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;
import java.util.Map;

public class DieselToolItem extends Item implements DynamicToolItem {
    public static final int CAPACITY = 4 * FluidType.BUCKET_VOLUME;
    private final double damage;

    public DieselToolItem(Properties settings, double damage) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON));
        this.damage = damage;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return Tiers.NETHERITE.getEnchantmentValue();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getDestroySpeed(world, pos) != 0.0f) {
            FluidFuelItemHelper.decrement(stack);
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        FluidFuelItemHelper.decrement(stack);
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (isSupportedBlock(stack, state) && FluidFuelItemHelper.getAmount(stack) > 0
                && !state.is(Tiers.NETHERITE.getIncorrectBlocksForDrops())) {
            return true;
        }
        return super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isSupportedBlock(stack, state)) {
            return getMiningSpeedMultiplier(stack);
        }
        return 1;
    }

    private float getMiningSpeedMultiplier(ItemStack stack) {
        long amount = FluidFuelItemHelper.getAmount(stack);
        if (amount > 0) {
            FluidVariant fluid = FluidFuelItemHelper.getFluid(stack);
            int burnTicks = FluidFuel.getFe(fluid.getFluid());
            if (burnTicks > 0) {
                return 1.0f + burnTicks / 8.0f;
            }
        }
        return 1.0f;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            return ItemHelper.getToolModifiers(damage * FluidFuel.getFe(FluidFuelItemHelper.getFluid(stack).getFluid()) / 600);
        }
        return ItemAttributeModifiers.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FluidFuelItemHelper.appendTooltip(stack, tooltip, CAPACITY);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(getDurabilityBarProgress(stack) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        Fluid fluid = FluidFuelItemHelper.getFluid(stack).getFluid();

        if (fluid instanceof MIFluid cf) {
            return cf.color;
        } else {
            return 0;
        }
    }

    public double getDurabilityBarProgress(ItemStack stack) {
        return (double) FluidFuelItemHelper.getAmount(stack) / CAPACITY;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level w = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = w.getBlockState(pos);
        Player player = context.getPlayer();
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            if (stack.is(ItemTags.AXES)) {
                Block newBlock = StrippingAccess.getStrippedBlocks().get(state.getBlock());
                if (newBlock != null) {
                    w.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1, 1);
                    if (!w.isClientSide) {
                        w.setBlock(pos, newBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)), 11);
                        FluidFuelItemHelper.decrement(stack);
                    }
                    return InteractionResult.sidedSuccess(w.isClientSide);
                }
            }
            if (stack.is(ItemTags.SHOVELS)) {
                BlockState newState = PathingAccess.getPathStates().get(state.getBlock());
                if (newState != null) {
                    w.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1, 1);
                    if (!w.isClientSide) {
                        w.setBlock(pos, newState, 11);
                        FluidFuelItemHelper.decrement(stack);
                    }
                    return InteractionResult.sidedSuccess(w.isClientSide);
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        return true;
    }

    private static class StrippingAccess extends AxeItem {
        private StrippingAccess(Tier material, Properties settings) {
            super(material, settings);
        }

        public static Map<Block, Block> getStrippedBlocks() {
            return STRIPPABLES;
        }
    }

    private static class PathingAccess extends ShovelItem {
        private PathingAccess(Tier material, Properties settings) {
            super(material, settings);
        }

        public static Map<Block, BlockState> getPathStates() {
            return FLATTENABLES;
        }
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        final int defaultMb = 100;
        float speedMultiplier = this.getMiningSpeedMultiplier(stack);
        int costMb = (int) (defaultMb / speedMultiplier);

        if (FluidFuelItemHelper.getAmount(stack) >= costMb) {
            if (stack.is(Tags.Items.TOOLS_SHEAR) && interactionTarget instanceof Shearable shearable) {
                if (!interactionTarget.level().isClientSide && shearable.readyForShearing()) {
                    shearable.shear(SoundSource.PLAYERS);
                    interactionTarget.gameEvent(GameEvent.SHEAR, player);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
