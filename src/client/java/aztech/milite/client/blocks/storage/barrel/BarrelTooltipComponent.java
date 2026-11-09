package aztech.milite.client.blocks.storage.barrel;

import aztech.milite.blocks.storage.barrel.BarrelTooltipData;
import aztech.milite.client.util.RenderHelper;
import aztech.milite.MIText;
import aztech.milite.MITooltips;
import aztech.milite.util.TextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;

public record BarrelTooltipComponent(BarrelTooltipData data) implements ClientTooltipComponent {
    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return Math.max(textRenderer.width(data.variant().toStack().getHoverName()), 20 + textRenderer.width(getItemNumber()));
    }

    @Override
    public void renderText(Font textRenderer, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource immediate) {
        Style style = MITooltips.DEFAULT_STYLE;

        textRenderer.drawInBatch(data.variant().toStack().getHoverName().copy().setStyle(style), x, y, -1, true, matrix4f, immediate,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

        textRenderer.drawInBatch(getItemNumber(), x + 20, y + 15, -1, true, matrix4f, immediate, Font.DisplayMode.NORMAL, 0,
                LightTexture.FULL_BRIGHT);
    }

    public Component getItemNumber() {
        long amount = data.amount();
        long capacity = data.capacity();
        Component itemNumber;

        Style style = TextHelper.GOLD;

        if (data.creative()) {
            itemNumber = Component.literal("∞ / ∞").setStyle(style);
        } else {
            String amountStr;
            String capacityStr;
            Component percent = MITooltips.INVERTED_RATIO_PERCENTAGE_PARSER.parse((double) amount / capacity);

            if (Screen.hasShiftDown()) {
                amountStr = "" + amount;
                capacityStr = "" + capacity;

            } else {
                var amountText = TextHelper.getAmount(amount);
                var capacityText = TextHelper.getAmount(capacity);
                amountStr = amountText.digit() + amountText.unit();
                capacityStr = capacityText.digit() + capacityText.unit();
            }
            itemNumber = MIText.BarrelStorageComponent.text(amountStr, capacityStr, percent).setStyle(style);
        }

        return itemNumber;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        RenderHelper.renderAndDecorateItem(guiGraphics, font, data.variant().toStack(), x, y + 10);
    }
}
