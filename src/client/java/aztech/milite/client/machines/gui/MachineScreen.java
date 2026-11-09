package aztech.milite.client.machines.gui;

import aztech.milite.MI;
import aztech.milite.MIText;
import aztech.milite.client.DynamicTooltip;
import aztech.milite.client.screen.MIHandledScreen;
import aztech.milite.client.util.RenderHelper;
import aztech.milite.inventory.BackgroundRenderedSlot;
import aztech.milite.inventory.ConfigurableFluidStack;
import aztech.milite.inventory.ConfigurableItemStack;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.network.machines.AdjustSlotCapacityPacket;
import aztech.milite.network.machines.LockAllPacket;
import aztech.milite.network.machines.SetLockingModePacket;
import aztech.milite.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.milite.util.FluidHelper;
import aztech.milite.util.Rectangle;
import aztech.milite.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MachineScreen extends MIHandledScreen<MachineMenuClient> implements ClientComponentRenderer.ButtonContainer {
    public static final ResourceLocation SLOT_ATLAS = MI.id("textures/gui/container/slot_atlas.png");
    public static final ResourceLocation BACKGROUND = MI.id("textures/gui/container/background.png");

    private static final int BACKGROUND_SIZE = 256;
    private static final int INVENTORY_HEIGHT = 83;

    private final List<ClientComponentRenderer> renderers = new ArrayList<>();

    public MachineScreen(MachineMenuClient handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        for (GuiComponentClient<?, ?> component : handler.components) {
            renderers.add(component.createRenderer(this));
        }

        this.imageHeight = handler.guiParams.backgroundHeight;
        this.imageWidth = handler.guiParams.backgroundWidth;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    public int x() {
        return leftPos;
    }

    public int y() {
        return topPos;
    }

    private static final int BUTTON_LOCK_U = 24;
    private static final int BUTTON_V_UP = 18;
    private static final int BUTTON_V_DOWN = 30;

    private int nextButtonX;
    private static final int BUTTON_Y = 4;
    private static final int BUTTON_SIZE = 12;
    private static final int BUTTON_SPACING = 2;

    private int buttonX() {
        nextButtonX -= BUTTON_SIZE + BUTTON_SPACING;
        return nextButtonX + BUTTON_SIZE + BUTTON_SPACING + leftPos;
    }

    private int buttonY() {
        return BUTTON_Y + topPos;
    }

    @Override
    protected void init() {
        super.init();
        this.nextButtonX = 160;
        if (menu.guiParams.lockButton) {
            addLockButton();
        }

        for (ClientComponentRenderer renderer : renderers) {
            renderer.addButtons(this);
        }
    }

    public static void blitStretched(GuiGraphics guiGraphics, int x, int y, int width, int height, int u, int v, int cap, int middle) {
        blitStretched(guiGraphics, x, y, width, height, u, v, cap, middle, u + cap + middle);
    }

    public static void blitStretched(GuiGraphics guiGraphics, int x, int y, int width, int height, int u, int v, int cap, int middle, int rightU) {
        guiGraphics.blit(SLOT_ATLAS, x, y, u, v, cap, height);
        for (int i = 0; i < (width - 2 * cap) / middle; i++) {
            guiGraphics.blit(SLOT_ATLAS, x + cap + i * middle, y, u + cap, v, middle, height);
        }
        guiGraphics.blit(SLOT_ATLAS, x + width - cap, y, rightU, v, cap, height);
    }

    public void blitButton(Button button, GuiGraphics guiGraphics, int u, boolean pressed) {
        guiGraphics.blit(SLOT_ATLAS, button.getX(), button.getY(), u, pressed ? BUTTON_V_DOWN : BUTTON_V_UP,
                button.getWidth(), button.getHeight());
    }

    @Override
    public MachineButton addButton(int u, Consumer<Integer> pressAction, Supplier<List<Component>> tooltipSupplier,
                                   Supplier<Boolean> isPressed) {
        return addRenderableWidget(new MachineButton(buttonX(), buttonY(), BUTTON_SIZE, BUTTON_SIZE, b -> pressAction.accept(menu.containerId),
                tooltipSupplier,
                (screen, button, guiGraphics, mouseX, mouseY, delta) -> {
                    blitButton(button, guiGraphics, u, isPressed.get());
                }, () -> true));
    }

    @Override
    public MachineButton addButton(int posX, int posY, int width, int height, Consumer<Integer> pressAction,
                                   Supplier<List<Component>> tooltipSupplier, ClientComponentRenderer.CustomButtonRenderer renderer, Supplier<Boolean> isButtonPresent) {
        return addRenderableWidget(new MachineButton(posX + leftPos, posY + topPos, width, height, b -> {
            pressAction.accept(menu.containerId);
        }, tooltipSupplier, renderer, isButtonPresent));
    }

    private static final int SLOT_FRAME_U = 0;

    private void addLockButton() {
        addButton(BUTTON_LOCK_U, syncId -> {
            if (hasShiftDown()) {
                boolean lock = menu.hasUnlockedSlot();
                menu.lockAll(lock);
                new LockAllPacket(syncId, lock).sendToServer();
            } else {
                boolean newLockingMode = !menu.lockingMode;
                menu.lockingMode = newLockingMode;
                new SetLockingModePacket(syncId, newLockingMode).sendToServer();
            }
        }, () -> {
            List<Component> lines = new ArrayList<>();
            if (menu.lockingMode) {
                lines.add(MIText.LockingModeOn.text());
                lines.add(MIText.ClickToDisable.text().setStyle(TextHelper.GRAY_TEXT));
            } else {
                lines.add(MIText.LockingModeOff.text());
                lines.add(MIText.ClickToEnable.text().setStyle(TextHelper.GRAY_TEXT));
            }
            lines.add((menu.hasUnlockedSlot() ? MIText.ShiftClickToLockAll : MIText.ShiftClickToUnlockAll).text().setStyle(TextHelper.GRAY_TEXT));
            return lines;
        }, () -> menu.lockingMode);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        for (var element : renderables) {
            if (element instanceof MachineButton machineButton) {
                machineButton.visible = machineButton.isPresent.get();
            }
        }

        super.render(guiGraphics, mouseX, mouseY, delta);
        for (ClientComponentRenderer renderer : renderers) {
            if (renderer.renderTooltip(this, font, guiGraphics, leftPos, topPos, mouseX, mouseY)) {
                return;
            }
        }
        renderConfigurableSlotTooltips(guiGraphics, mouseX, mouseY);
    }

    private FormattedCharSequence titleToRender() {
        final int maxWidth = this.nextButtonX + 22 - this.titleLabelX;
        if (font.width(this.title) > maxWidth) {
            var text = FormattedText.composite(
                    font.substrByWidth(this.title, maxWidth - font.width("...")),
                    FormattedText.of("..."));
            return Language.getInstance().getVisualOrder(text);
        } else {
            return title.getVisualOrderText();
        }
    }

    /**
     * We override this method to clip the title if it's too long.
     */
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, titleToRender(), this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
    private static final int SLOT_FRAME_V = 88;

    @Override
    public MachineButton addButton(ClientComponentRenderer.CustomButtonRenderer renderer, Consumer<Integer> pressAction,
                                   Supplier<List<Component>> tooltipSupplier, Supplier<Boolean> isButtonPresent) {
        return addRenderableWidget(new MachineButton(buttonX(), buttonY(), BUTTON_SIZE, BUTTON_SIZE,
                b -> pressAction.accept(menu.containerId), tooltipSupplier, renderer, isButtonPresent));
    }
    private static final int SLOT_FRAME_BORDER = 4;

    private static void blitFrame(GuiGraphics guiGraphics, int x, int y, int width, int height, int u, int v) {
        blitFrame(guiGraphics, x, y, width, height, u, v, 4, 4);
    }

    private static void blitFrame(GuiGraphics guiGraphics, int x, int y, int width, int height, int u, int v, int sliceWidth, int sliceHeight) {
        guiGraphics.blit(SLOT_ATLAS, x, y, width, height, u, v, sliceWidth, sliceHeight, BACKGROUND_SIZE, BACKGROUND_SIZE);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int bw = menu.guiParams.backgroundWidth;
        int bh = menu.guiParams.backgroundHeight;
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, bw, 4);
        if (bh <= BACKGROUND_SIZE) {
            guiGraphics.blit(BACKGROUND, leftPos, topPos + 4, 0, BACKGROUND_SIZE - bh + 4, bw, bh - 4);
        } else {
            int sliceV = 80;
            int machineBottom = BACKGROUND_SIZE - INVENTORY_HEIGHT;
            int extraHeight = bh - 4 - INVENTORY_HEIGHT - (machineBottom - 4);
            int topHeight = sliceV - 4;
            guiGraphics.blit(BACKGROUND, leftPos, topPos + 4, 0, 4, bw, topHeight);
            guiGraphics.blit(BACKGROUND, leftPos, topPos + 4 + topHeight, 0, sliceV, bw, extraHeight + 1);
            guiGraphics.blit(BACKGROUND, leftPos, topPos + 4 + topHeight + extraHeight + 1, 0, sliceV + 1, bw,
                    machineBottom - sliceV - 1);
            guiGraphics.blit(BACKGROUND, leftPos, topPos + bh - INVENTORY_HEIGHT, 0, BACKGROUND_SIZE - INVENTORY_HEIGHT, bw,
                    INVENTORY_HEIGHT);
        }

        for (ClientComponentRenderer renderer : renderers) {
            renderer.renderBackground(guiGraphics, leftPos, topPos);
        }

        for (ClientComponentRenderer renderer : renderers) {
            renderer.renderForeground(guiGraphics, leftPos, topPos);
        }

        renderSlotBackgrounds(guiGraphics);
    }

    /**
     * Draws the atlas background of every slot that asks for one. The module slots of the side panel return no atlas
     * (their frames are part of the side panel sprite), so they are skipped here.
     */
    private void renderSlotBackgrounds(GuiGraphics guiGraphics) {
        renderItemSlotFrame(guiGraphics);

        for (Slot slot : this.menu.slots) {
            if (slot instanceof BackgroundRenderedSlot brs) {
                var atlas = brs.getBackgroundAtlasLocation();
                if (atlas == null) {
                    continue;
                }
                int px = leftPos + slot.x - 1;
                int py = topPos + slot.y - 1;
                guiGraphics.blit(atlas, px, py, brs.getBackgroundU(), brs.getBackgroundV(), 18, 18);
            }
        }
    }

    private void renderItemSlotFrame(GuiGraphics guiGraphics) {
        String id = menu.guiParams.blockId.getPath();
        if (!id.endsWith("_item_input_hatch") && !id.endsWith("_item_output_hatch")
                && !id.endsWith("_fluid_input_hatch") && !id.endsWith("_fluid_output_hatch")) {
            return;
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Slot slot : this.menu.slots) {
            if (!(slot instanceof BackgroundRenderedSlot)) {
                continue;
            }
            minX = Math.min(minX, slot.x);
            minY = Math.min(minY, slot.y);
            maxX = Math.max(maxX, slot.x);
            maxY = Math.max(maxY, slot.y);
        }
        if (minX == Integer.MAX_VALUE) {
            return;
        }

        int b = SLOT_FRAME_BORDER;
        int x = leftPos + minX - 1 - b;
        int y = topPos + minY - 1 - b;
        int width = maxX - minX + 18 + 2 * b;
        int height = maxY - minY + 18 + 2 * b;
        int middle = 18;
        int right = SLOT_FRAME_U + b + middle;
        int bottom = SLOT_FRAME_V + b + middle;

        blitFrame(guiGraphics, x, y, b, b, SLOT_FRAME_U, SLOT_FRAME_V);
        blitFrame(guiGraphics, x + width - b, y, b, b, right, SLOT_FRAME_V);
        blitFrame(guiGraphics, x, y + height - b, b, b, SLOT_FRAME_U, bottom);
        blitFrame(guiGraphics, x + width - b, y + height - b, b, b, right, bottom);
        blitFrame(guiGraphics, x + b, y, width - 2 * b, b, SLOT_FRAME_U + b, SLOT_FRAME_V, middle, b);
        blitFrame(guiGraphics, x + b, y + height - b, width - 2 * b, b, SLOT_FRAME_U + b, bottom, middle, b);
        blitFrame(guiGraphics, x, y + b, b, height - 2 * b, SLOT_FRAME_U, SLOT_FRAME_V + b, b, middle);
        blitFrame(guiGraphics, x + width - b, y + b, b, height - 2 * b, right, SLOT_FRAME_V + b, b, middle);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot cfs) {
            ConfigurableFluidStack stack = cfs.getConfStack();
            FluidVariant renderedKey = stack.getLockedInstance() == null ? stack.getResource() : FluidVariant.of(stack.getLockedInstance());
            if (!renderedKey.isBlank()) {
                RenderHelper.drawFluidInGui(guiGraphics, renderedKey, slot.x, slot.y);
            }
            return;
        }

        if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot itemSlot) {
            ConfigurableItemStack itemStack = itemSlot.getConfStack();
            if ((itemStack.isPlayerLocked() || itemStack.isMachineLocked()) && itemStack.getResource().isBlank()) {
                Item item = itemStack.getLockedInstance();
                if (item != Items.AIR) {
                    RenderHelper.renderAndDecorateItem(guiGraphics, font, new ItemStack(item), slot.x, slot.y, "0");
                }
            }
        }
        super.renderSlot(guiGraphics, slot);
    }

    private void renderConfigurableSlotTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Slot slot = hoveredSlot;
        if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
            ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
            FluidVariant renderedKey = stack.getLockedInstance() != null ? FluidVariant.of(stack.getLockedInstance()) : stack.getResource();
            List<Component> tooltip = new ArrayList<>(
                    FluidHelper.getTooltipForFluidStorage(renderedKey, stack.getAmount(), stack.getCapacity(), false));

            if (stack.canPlayerInsert()) {
                if (stack.canPlayerExtract()) {
                    tooltip.add(MIText.FluidSlotIO.text().setStyle(TextHelper.GRAY_TEXT));
                } else {
                    tooltip.add(MIText.FluidSlotInput.text().setStyle(TextHelper.GRAY_TEXT));
                }
            } else if (stack.canPlayerExtract()) {
                tooltip.add(MIText.FluidSlotOutput.text().setStyle(TextHelper.GRAY_TEXT));
            }
            guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot confSlot) {
            renderConfigurableItemStackTooltip(guiGraphics, confSlot.getConfStack(), mouseX, mouseY);
        } else if (slot != null && slot.hasItem()) {
            guiGraphics.renderTooltip(font, slot.getItem(), mouseX, mouseY);
        }
    }

    private void renderConfigurableItemStackTooltip(GuiGraphics guiGraphics, ConfigurableItemStack stack, int mouseX, int mouseY) {
        ItemStack vanillaStack = stack.isEmpty() ? stack.getLockedInstance() == null ? ItemStack.EMPTY : new ItemStack(stack.getLockedInstance())
                : stack.getResource().toStack((int) stack.getAmount());
        List<Component> textTooltip;
        if (vanillaStack.isEmpty()) {
            textTooltip = new ArrayList<>();
            textTooltip.add(MIText.Empty.text());
        } else {
            textTooltip = getTooltipFromItem(Minecraft.getInstance(), vanillaStack);
        }
        Optional<TooltipComponent> data = vanillaStack.getTooltipImage();
        var capacityText = Component.literal(String.valueOf(stack.getAdjustedCapacity()));
        if (stack.getAdjustedCapacity() != 64) {
            capacityText.setStyle(TextHelper.GOLD_BOLD);
        }
        textTooltip.add(MIText.ConfigurableSlotCapacity.text(capacityText).setStyle(TextHelper.GRAY_TEXT));
        guiGraphics.renderTooltip(font, textTooltip, data, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ClientComponentRenderer renderer : renderers) {
            if (renderer.mouseClicked(this, mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (ClientComponentRenderer renderer : renderers) {
            if (renderer.mouseDragged(this, mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ClientComponentRenderer renderer : renderers) {
            if (renderer.mouseReleased(this, mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        if (hoveredSlot instanceof ConfigurableItemStack.ConfigurableItemSlot confSlot) {
            ConfigurableItemStack stack = confSlot.getConfStack();
            boolean isIncrease = amountY > 0;
            boolean isShiftDown = hasShiftDown();
            // Client side update
            stack.adjustCapacity(isIncrease, isShiftDown);
            // Server side update
            new AdjustSlotCapacityPacket(menu.containerId, menu.slots.indexOf(hoveredSlot), isIncrease, isShiftDown)
                    .sendToServer();
            return true;
        }
        return false;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return getExtraBoxes().stream().noneMatch(r -> r.contains(mouseX, mouseY))
                && super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }

    public MachineGuiParameters getGuiParams() {
        return menu.getGuiParams();
    }

    // This is used by the REI plugin to detect fluid slots
    public Slot getFocusedSlot() {
        return hoveredSlot;
    }

    public List<Rectangle> getExtraBoxes() {
        var list = new ArrayList<Rectangle>();
        for (var renderer : renderers) {
            renderer.addExtraBoxes(list, leftPos, topPos);
        }
        return list;
    }

    public class MachineButton extends Button {
        private final Supplier<List<Component>> tooltipSupplier;
        final ClientComponentRenderer.CustomButtonRenderer renderer;
        final Supplier<Boolean> isPresent;

        private MachineButton(int x, int y, int width, int height, OnPress onPress, Supplier<List<Component>> tooltipSupplier,
                              ClientComponentRenderer.CustomButtonRenderer renderer, Supplier<Boolean> isPresent) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.tooltipSupplier = tooltipSupplier;
            this.setTooltip(new DynamicTooltip(tooltipSupplier));
            this.renderer = renderer;
            this.isPresent = isPresent;
        }

        @Override
        public Component getMessage() {
            var tooltip = tooltipSupplier.get();
            return tooltip.isEmpty() ? Component.empty() : tooltip.getFirst();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.visible = isPresent.get();
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            renderer.renderButton(MachineScreen.this, this, guiGraphics, mouseX, mouseY, partialTick);
        }

        public void renderVanilla(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        }
    }
}
