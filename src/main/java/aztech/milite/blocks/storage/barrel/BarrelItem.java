package aztech.milite.blocks.storage.barrel;

import aztech.milite.blocks.storage.AbstractStorageBlockItem;
import aztech.milite.items.ItemContainingItemHelper;
import aztech.milite.MIText;
import aztech.milite.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.milite.util.TextHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

public class BarrelItem extends AbstractStorageBlockItem<ItemVariant> implements ItemContainingItemHelper {
    private static final int ITEM_BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public BarrelItem(BarrelBlock block, Properties settings) {
        super(block, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (behaviour instanceof BarrelBlock.BarrelStorage barrelStorage) {
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);
            if (isEmpty(stack) && isUnlocked(stack)) {
                tooltip.add(MIText.Empty.text().setStyle(style));
                tooltip.add(MIText.BarrelStack.text(barrelStorage.stackCapacity).setStyle(TextHelper.GOLD));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    public long getCurrentCapacity(ItemStack stack) {
        return behaviour.getCapacityForResource(getResource(stack));
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!getBehaviour().isCreative()) {
            if (!isEmpty(stack) || !isUnlocked(stack)) {
                return Optional.of(new BarrelTooltipData(getResource(stack), getAmount(stack),
                        getCurrentCapacity(stack), false));
            }
        } else if (!getResource(stack).isBlank()) {
            return Optional.of(new BarrelTooltipData(getResource(stack), -1,
                    -1, true));
        }
        return Optional.empty();
    }

    public boolean isBarVisible(ItemStack stack) {
        return !getBehaviour().isCreative() && getAmount(stack) > 0;
    }

    public int getBarWidth(ItemStack stack) {
        return (int) Math.min(1 + (12 * getAmount(stack)) / getCurrentCapacity(stack), 13);
    }

    public int getBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stackBarrel, Slot slot, ClickAction clickType, Player player) {
        return handleStackedOnOther(stackBarrel, slot, clickType, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player,
            SlotAccess cursorStackReference) {
        return handleOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
    }
}
