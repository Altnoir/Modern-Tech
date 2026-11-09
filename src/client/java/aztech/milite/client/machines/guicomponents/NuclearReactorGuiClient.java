package aztech.milite.client.machines.guicomponents;

import static aztech.milite.nuclear.NeutronType.*;

import aztech.milite.client.machines.gui.ClientComponentRenderer;
import aztech.milite.client.machines.gui.GuiComponentClient;
import aztech.milite.client.machines.gui.MachineScreen;
import aztech.milite.client.util.RenderHelper;
import aztech.milite.machines.blockentities.hatches.NuclearHatch;
import aztech.milite.machines.guicomponents.NuclearReactorGui;
import aztech.milite.MI;
import aztech.milite.MICommonProxy;
import aztech.milite.MIText;
import aztech.milite.nuclear.NeutronType;
import aztech.milite.nuclear.NuclearComponent;
import aztech.milite.nuclear.NuclearComponentItem;
import aztech.milite.nuclear.NuclearConstant;
import aztech.milite.nuclear.NuclearFuel;
import aztech.milite.nuclear.NuclearTileData;
import aztech.milite.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.milite.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.milite.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.milite.util.FluidHelper;
import aztech.milite.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NuclearReactorGuiClient extends GuiComponentClient<Unit, NuclearReactorGui.Data> {
    public static final ResourceLocation TEXTURE_ATLAS = MI.id("textures/gui/rei/texture_atlas.png");
    static final int GRID_CENTER_X = 88;
    static final int GRID_CENTER_Y = 88;

    public NuclearReactorGuiClient(Unit params, NuclearReactorGui.Data data) {
        super(params, data);
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer(machineScreen);
    }

    public class Renderer implements ClientComponentRenderer {
        final int centerX = GRID_CENTER_X, centerY = GRID_CENTER_Y;
        private final MachineScreen screen;
        Renderer.Mode currentMode = Renderer.Mode.NUCLEAR_FUEL;
        NeutronType neutronMode = BOTH;

        Renderer(MachineScreen screen) {
            this.screen = screen;
        }

        ItemStack fuelStack = new ItemStack(BuiltInRegistries.ITEM.get(MI.id("uranium_fuel_rod")), 1);

        private final ResourceLocation COLORBAR = MI.id("textures/gui/colorbar.png");

        private enum Mode {
            NUCLEAR_FUEL(0),
            TEMPERATURE(1),
            NEUTRON_ABSORPTION(2),
            NEUTRON_FLUX(3),
            NEUTRON_GENERATION(4),
            FE_GENERATION(5);

            final int index;

            Mode(int index) {
                this.index = index;
            }
        }

        Component[] modeTooltip = new Component[] { MIText.NuclearFuelMode.text(), MIText.TemperatureMode.text(), MIText.NeutronAbsorptionMode.text(),
                MIText.NeutronFluxMode.text(), MIText.NeutronGenerationMode.text(), MIText.FeGenerationMode.text() };

        Component[] neutronModeTooltip = new Component[] { MIText.FastNeutron.text(), MIText.ThermalNeutron.text(), MIText.Both.text() };

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
            if (data.valid()) {
                for (int i = 0; i < data.gridSizeX(); i++) {
                    for (int j = 0; j < data.gridSizeY(); j++) {
                        int index = data.toIndex(i, j);
                        Optional<NuclearTileData> tile = data.tilesData().get(index).data();
                        if (tile.isPresent()) {
                            NuclearTileData tileData = tile.get();
                            int px = x + centerX - data.gridSizeX() * 9 + i * 18;
                            int py = y + centerY - data.gridSizeY() * 9 + j * 18;

                            if (tileData.isFluid()) {
                                guiGraphics.blit(MachineScreen.SLOT_ATLAS, px, py, 18, 0, 18, 18);
                            } else {
                                guiGraphics.blit(MachineScreen.SLOT_ATLAS, px, py, 0, 0, 18, 18);
                            }

                            TransferVariant<?> variant = tile.get().getVariant();
                            long variantAmount = tile.get().getVariantAmount();

                            if (variantAmount > 0 & !variant.isBlank()) {
                                if (variant instanceof ItemVariant itemVariant) {
                                    var stack = itemVariant.toStack((int) variantAmount);
                                    RenderHelper.renderAndDecorateItem(guiGraphics, Minecraft.getInstance().font, stack, px + 1, py + 1);
                                } else if (variant instanceof FluidVariant fluidVariant) {
                                    RenderSystem.disableBlend();
                                    RenderHelper.drawFluidInGui(guiGraphics, fluidVariant, px + 1, py + 1);
                                }
                            }

                            if (currentMode != Renderer.Mode.NUCLEAR_FUEL) {
                                int u;
                                int v;

                                if (currentMode == Renderer.Mode.TEMPERATURE) {
                                    v = 30;
                                    u = (int) (299 * tileData.getTemperature() / NuclearConstant.MAX_TEMPERATURE);
                                } else if (currentMode == Renderer.Mode.FE_GENERATION) {
                                    v = 30;
                                    u = (int) (299 * Math.min(1.0, tileData.getMeanFeGeneration() / (2 * NuclearConstant.MAX_HATCH_FE_PRODUCTION)));
                                } else {
                                    double neutronRate;
                                    double factor = neutronMode == BOTH ? 2 : 1;
                                    if (currentMode == Renderer.Mode.NEUTRON_ABSORPTION) {
                                        neutronRate = 5 * tileData.getMeanNeutronAbsorption(neutronMode);
                                    } else if (currentMode == Renderer.Mode.NEUTRON_FLUX) {
                                        neutronRate = tileData.getMeanNeutronFlux(neutronMode);
                                    } else if (currentMode == Renderer.Mode.NEUTRON_GENERATION) {
                                        if (neutronMode == THERMAL) {
                                            neutronRate = 0;
                                        } else {
                                            neutronRate = tileData.getMeanNeutronGeneration();
                                            factor = 1;
                                        }
                                    } else {
                                        neutronRate = 0;
                                    }
                                    v = 0;
                                    u = (int) (299 * NuclearReactorGui.neutronColorScheme(factor * neutronRate));
                                }

                                RenderSystem.disableDepthTest();
                                RenderSystem.enableBlend();

                                guiGraphics.pose().translate(px, py, 0);
                                guiGraphics.pose().scale(18, 18, 1);
                                guiGraphics.blit(COLORBAR, 0, 0, u, v, 1, 1, 300, 60);
                                guiGraphics.pose().scale(1 / 18f, 1 / 18f, 1);
                                guiGraphics.pose().translate(-px, -py, 0);

                            }

                            if (currentMode == Renderer.Mode.TEMPERATURE) {
                                if (!variant.isBlank() && variant instanceof ItemVariant itemVariant) {
                                    if (itemVariant.getItem() instanceof NuclearComponentItem item) {
                                        if (tileData.getTemperature() + 100 > item.getMaxTemperature()) {
                                            RenderSystem.enableBlend();
                                            RenderSystem.disableDepthTest();
                                            if (System.currentTimeMillis() % 1000 > 500) {
                                                guiGraphics.blit(MachineScreen.SLOT_ATLAS, px + 1, py + 1, 22, 42, 16, 16);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (data.feFuelConsumption() > 0 && currentMode == Renderer.Mode.FE_GENERATION) {
                    Font font = Minecraft.getInstance().font;
                    guiGraphics.pose().translate(0, 0, 256);
                    guiGraphics.drawString(font, getEfficiencyText(), x + 8, y + 16, 0xFFFFFF, false);
                    guiGraphics.pose().translate(0, 0, -256);
                }

            } else {
                Font font = Minecraft.getInstance().font;
                Component text = MIText.MultiblockShapeInvalid.text().setStyle(TextHelper.RED.withBold(true));
                int width = font.width(text);
                guiGraphics.drawString(font, text, x + centerX - width / 2, y + MultiblockBuildClient.getReactorStatusTop(screen, font),
                        0xFFFFFF, false);
            }
        }

        @Override
        public boolean renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int x, int y, int cursorX, int cursorY) {
            int i = (cursorX - (x + centerX - data.gridSizeX() * 9)) / 18;
            int j = (cursorY - (y + centerY - data.gridSizeY() * 9)) / 18;

            if (data.valid()) {
                if (i >= 0 && j >= 0 && i < data.gridSizeX() && j < data.gridSizeY()) {
                    int index = data.toIndex(i, j);
                    Optional<NuclearTileData> tile = data.tilesData().get(index).data();
                    if (tile.isPresent()) {
                        NuclearTileData tileData = tile.get();
                        TransferVariant<?> variant = tileData.getVariant();
                        if (currentMode == Renderer.Mode.NUCLEAR_FUEL) {
                            long variantAmount = tile.get().getVariantAmount();
                            if (variantAmount > 0 & !variant.isBlank()) {
                                if (variant instanceof ItemVariant itemVariant) {
                                    guiGraphics.renderTooltip(font, itemVariant.toStack((int) variantAmount), cursorX, cursorY);
                                    return true;
                                } else if (variant instanceof FluidVariant fluidVariant) {
                                    guiGraphics.renderTooltip(font,
                                            FluidHelper.getTooltipForFluidStorage(fluidVariant, variantAmount, NuclearHatch.capacity, false),
                                            Optional.empty(), cursorX, cursorY);
                                    return true;
                                }
                            }

                        } else if (currentMode == Renderer.Mode.TEMPERATURE) {
                            int temperature = (int) tileData.getTemperature();
                            List<Component> tooltip = new ArrayList<>();

                            tooltip.add(MIText.Temperature.text(temperature));

                            if (!variant.isBlank() && variant instanceof ItemVariant itemVariant) {
                                if (itemVariant.getItem() instanceof NuclearComponentItem item) {
                                    tooltip.add(MIText.MaxTemp.text(item.getMaxTemperature()).setStyle(TextHelper.GOLD));
                                }
                            }

                            guiGraphics.renderTooltip(font, tooltip, Optional.empty(), cursorX, cursorY);
                            return true;

                        } else if (currentMode == Renderer.Mode.FE_GENERATION) {
                            double feGeneration = tileData.getMeanFeGeneration();
                            guiGraphics.renderTooltip(font, TextHelper.getFeTextTick(feGeneration, true), cursorX, cursorY);
                            return true;
                        } else {
                            double neutronRateFast;
                            double neutronRateThermal;

                            if (currentMode == Renderer.Mode.NEUTRON_ABSORPTION) {

                                neutronRateFast = tileData.getMeanNeutronAbsorption(FAST);
                                neutronRateThermal = tileData.getMeanNeutronAbsorption(THERMAL);

                            } else if (currentMode == Renderer.Mode.NEUTRON_FLUX) {

                                neutronRateFast = tileData.getMeanNeutronFlux(FAST);
                                neutronRateThermal = tileData.getMeanNeutronFlux(THERMAL);

                            } else if (currentMode == Renderer.Mode.NEUTRON_GENERATION) {

                                neutronRateFast = tileData.getMeanNeutronGeneration();
                                neutronRateThermal = 0;
                            } else {
                                neutronRateFast = 0;
                                neutronRateThermal = 0;
                            }

                            double neutronRate = 0;

                            if (neutronMode == BOTH) {
                                neutronRate = neutronRateFast + neutronRateThermal;
                            } else if (neutronMode == FAST) {
                                neutronRate = neutronRateFast;
                            } else if (neutronMode == THERMAL) {
                                neutronRate = neutronRateThermal;
                            }
                            String neutronRateString = String.format("%.1f", neutronRate);
                            List<Component> tooltips = new ArrayList<>();

                            tooltips.add(MIText.NeutronsRate.text(neutronRateString));

                            if (neutronMode == BOTH && neutronRate > 0 && currentMode != Renderer.Mode.NEUTRON_GENERATION) {
                                String neutronRateFastString = String.format("%.1f", neutronRateFast);
                                String neutronRateThermalString = String.format("%.1f", neutronRateThermal);
                                String neutronRateFastFractionString = String.format(" (%.1f %%)", 100 * neutronRateFast / neutronRate);
                                String neutronRateThermalFractionString = String.format(" (%.1f %%)", 100 * neutronRateThermal / neutronRate);

                                if (neutronRateFast > 0) {
                                    tooltips.add(Component
                                            .literal(MIText.FastNeutron.text().getString() + " : "
                                                    + MIText.NeutronsRate.text(neutronRateFastString).getString() + neutronRateFastFractionString)
                                            .setStyle(TextHelper.GRAY_TEXT));
                                }
                                if (neutronRateThermal > 0) {
                                    tooltips.add(Component.literal(MIText.ThermalNeutron.text().getString() + " : "
                                            + MIText.NeutronsRate.text(neutronRateThermalString).getString() + neutronRateThermalFractionString)
                                            .setStyle(TextHelper.GRAY_TEXT));
                                }
                            }

                            if (currentMode == Renderer.Mode.NEUTRON_GENERATION) {
                                NuclearComponent<?> component = tileData.getComponent();
                                if (component instanceof NuclearFuel fuel) {
                                    double efficiencyFactor = fuel.efficiencyFactor(tileData.getTemperature());
                                    tooltips.add(MIText.ThermalEfficiency.text(String.format("%.1f", efficiencyFactor * 100))
                                            .setStyle(TextHelper.GOLD));
                                }
                            }

                            guiGraphics.renderTooltip(font, tooltips, Optional.empty(), cursorX, cursorY);
                            return true;
                        }

                    }
                }
            }

            if (data.feFuelConsumption() > 0 && currentMode == Renderer.Mode.FE_GENERATION) {
                Font renderer = Minecraft.getInstance().font;
                int width = renderer.width(getEfficiencyText().getString());
                if (cursorX > x + 8 && cursorX < x + 8 + width && cursorY >= y + 15 && cursorY < y + 24) {

                    Component feProduction = TextHelper.getFeTextTick(data.feProduction(), true);
                    Component feFuelConsumption = TextHelper.getFeTextTick(data.feFuelConsumption(), true);

                    Component tooltip = MIText.NuclearFuelEfficiencyTooltip.text(feProduction, feFuelConsumption);

                    guiGraphics.renderTooltip(font, tooltip, cursorX, cursorY);
                    return true;
                }
            }
            return false;
        }

        public Component getEfficiencyText() {
            String eff = String.format("%.1f", 100 * data.feProduction() / data.feFuelConsumption());
            return MIText.EfficiencyNuclear.text(eff).setStyle(TextHelper.RED);
        }

        private boolean drawButton() {
            return data.valid();
        }

        private boolean drawNeutronButton() {
            return data.valid() && (currentMode == Renderer.Mode.NEUTRON_FLUX || currentMode == Renderer.Mode.NEUTRON_ABSORPTION);
        }

        private NeutronType nextNeutronMode() {
            return NeutronType.TYPES[(neutronMode.index + 1) % NeutronType.TYPES.length];
        }

        private NeutronType previousNeutronMode() {
            return NeutronType.TYPES[(neutronMode.index - 1 + NeutronType.TYPES.length) % NeutronType.TYPES.length];
        }

        private Mode nextMode() {
            return Renderer.Mode.values()[(currentMode.index + 1) % Renderer.Mode.values().length];
        }

        private Mode previousMode() {
            return Renderer.Mode.values()[(currentMode.index - 1 + Renderer.Mode.values().length) % Renderer.Mode.values().length];
        }

        private Mode circulateMode() {
            if (MICommonProxy.INSTANCE.hasShiftDown()) {
                return previousMode();
            } else {
                return nextMode();
            }
        }

        private NeutronType circulateNeutronMode() {
            if (MICommonProxy.INSTANCE.hasShiftDown()) {
                return previousNeutronMode();
            } else {
                return nextNeutronMode();
            }
        }

        @Override
        public void addButtons(ButtonContainer container) {
            container.addButton(centerX - 9, 7, 18, 18,
                    (i) -> currentMode = circulateMode(),
                    () -> List.of(modeTooltip[currentMode.index], MIText.ClickToSwitch
                            .text(modeTooltip[nextMode().index]).setStyle(TextHelper.GRAY_TEXT),
                            MIText.ShiftClickToSwitch
                                    .text(modeTooltip[previousMode().index])
                                    .setStyle(TextHelper.GRAY_TEXT)),
                    (screen, button, guiGraphics, mouseX, mouseY, delta) -> {
                        guiGraphics.blit(MachineScreen.SLOT_ATLAS, button.getX(), button.getY(),
                                button.isHoveredOrFocused() ? 126 : 108, 18, 18, 18);
                        if (currentMode == Renderer.Mode.NUCLEAR_FUEL) {
                            RenderHelper.renderAndDecorateItem(guiGraphics, fuelStack, button.getX() + 1, button.getY() + 1);
                        } else if (currentMode == Renderer.Mode.FE_GENERATION) {
                            guiGraphics.blit(MachineScreen.SLOT_ATLAS, button.getX() + 3, button.getY() + 1, 243, 1, 13, 17);
                        } else {
                            guiGraphics.blit(MachineScreen.SLOT_ATLAS, button.getX() - 1, button.getY() - 1, 124 + currentMode.index * 20, 0, 20, 20);
                        }
                    }, this::drawButton);

            container.addButton(centerX - 9, 151, 18, 18, (i) -> neutronMode = circulateNeutronMode(),
                    () -> List.of(neutronModeTooltip[neutronMode.index],
                            MIText.ClickToSwitch.text(neutronModeTooltip[nextNeutronMode().index]).setStyle(TextHelper.GRAY_TEXT),
                            MIText.ShiftClickToSwitch.text(neutronModeTooltip[previousNeutronMode().index]).setStyle(TextHelper.GRAY_TEXT)),
                    (screen, button, guiGraphics, mouseX, mouseY, delta) -> {

                        guiGraphics.blit(MachineScreen.SLOT_ATLAS, button.getX(), button.getY(),
                                button.isHoveredOrFocused() ? 126 : 108, 18, 18, 18);

                        if (neutronMode == FAST) {
                            guiGraphics.blit(TEXTURE_ATLAS, button.getX() + 1, button.getY() + 1, 0, 240, 16, 16);
                        } else if (neutronMode == NeutronType.THERMAL) {
                            guiGraphics.blit(TEXTURE_ATLAS, button.getX() + 1, button.getY() + 1, 160, 240, 16, 16);
                        } else if (neutronMode == BOTH) {
                            guiGraphics.blit(TEXTURE_ATLAS, button.getX() + 1, button.getY() + 1, 80, 240, 16, 16);
                        }
                    }, this::drawNeutronButton);
        }
    }
}
