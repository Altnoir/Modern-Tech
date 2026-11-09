package aztech.milite.client.compat.viewer.impl.emi;

import aztech.milite.client.compat.viewer.abstraction.ViewerCategory;
import aztech.milite.client.compat.viewer.abstraction.ViewerPageManager;
import aztech.milite.client.machines.gui.MachineScreen;
import aztech.milite.MI;
import aztech.milite.util.TextHelper;
import aztech.milite.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.milite.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.milite.thirdparty.fabrictransfer.api.storage.TransferVariant;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import java.util.stream.Stream;

class ViewerCategoryEmi<D> extends EmiRecipeCategory {
    /**
     * Maps a {@link ViewerCategory} id (the same id MI stores in
     * {@link aztech.milite.compat.rei.machines.ReiMachineRecipes}) to the EMI category, so that
     * clicking a machine progress bar can display the matching recipes in EMI's sidebar.
     */
    static final java.util.Map<ResourceLocation, EmiRecipeCategory> BY_ID = new java.util.HashMap<>();

    public final ViewerCategory<D> wrapped;

    public ViewerCategoryEmi(ViewerCategory<D> category) {
        super(category.id, iconFor(category));

        this.wrapped = category;
        BY_ID.put(category.id, this);
    }

    private static EmiRenderable iconFor(ViewerCategory<?> category) {
        if (category.icon instanceof ViewerCategory.Icon.Stack(ItemStack stack)) {
            return EmiStack.of(stack);
        }

        return new EmiRenderable() {
            @Override
            public void render(GuiGraphics guiGraphics, int x, int y, float delta) {
                var texture = (ViewerCategory.Icon.Texture) category.icon;
                guiGraphics.blit(texture.loc(), x - 1, y - 1, 0, texture.u(), texture.v(), 18, 18, 256, 256);
            }
        };
    }

    @Override
    public Component getName() {
        return wrapped.title;
    }

    private void processLayout(D recipe, List<IngredientBuilder> inputs, List<IngredientBuilder> outputs) {
        wrapped.buildLayout(recipe, new ViewerCategory.LayoutBuilder() {
            @Override
            public ViewerCategory.SlotBuilder inputSlot(int x, int y) {
                var ing = new IngredientBuilder(x, y);
                inputs.add(ing);
                return ing;
            }

            @Override
            public ViewerCategory.SlotBuilder outputSlot(int x, int y) {
                var ing = new IngredientBuilder(x, y);
                outputs.add(ing);
                return ing;
            }

            @Override
            public void invisibleInput(Ingredient ingredient, int count) {
                var ing = new IngredientBuilder(0, 0);
                ing.ingredient(ingredient, count, 1);
                ing.isVisible = false;
                inputs.add(ing);
            }

            @Override
            public void invisibleOutput(Ingredient ingredient, int count) {
                var ing = new IngredientBuilder(0, 0);
                ing.ingredient(ingredient, count, 1);
                ing.isVisible = false;
                outputs.add(ing);
            }

            @Override
            public void scrollableSlots(int cols, int rows, List<SizedIngredient> ingredients) {
                ingredients.forEach((ingredient) -> this.invisibleInput(ingredient.ingredient(), ingredient.count()));
            }
        });
    }

    public void registerRecipes(EmiRegistry registry) {
        wrapped.buildRecipes(registry.getRecipeManager(), Minecraft.getInstance().level.registryAccess(), r -> registry.addRecipe(makeRecipe(r)));
    }

    private ViewerRecipe makeRecipe(D recipe) {
        List<IngredientBuilder> inputs = new ArrayList<>();
        List<IngredientBuilder> outputs = new ArrayList<>();

        processLayout(recipe, inputs, outputs);

        // If there is an invisible output, the recipe will be excluded from the tree.
        boolean excludeFromTree = outputs.stream().anyMatch(b -> !b.isVisible);

        // Remove empty ingredients that only exist for rendering reasons (e.g. empty fluid slots)
        inputs.removeIf(b -> b.ing.isEmpty());
        outputs.removeIf(b -> b.ing.isEmpty());

        List<IngredientBuilder> catalysts = inputs.stream().filter(b -> b.isCatalyst).toList();
        inputs.removeIf(b -> b.isCatalyst);

        return new ViewerRecipe(recipe, convertInputs(inputs), convertInputs(catalysts), convertOutputs(outputs), excludeFromTree);
    }

    private static List<EmiIngredient> convertInputs(List<IngredientBuilder> list) {
        return list.stream().map(b -> b.ing).toList();
    }

    private static List<EmiStack> convertOutputs(List<IngredientBuilder> list) {
        return list.stream().flatMap(b -> b.ing.getEmiStacks().stream()).toList();
    }

    private static class IngredientBuilder implements ViewerCategory.SlotBuilder {
        private final int x, y;
        private EmiIngredient ing = EmiStack.EMPTY;
        private boolean isFluid = false;
        private boolean hasBackground = true;
        private boolean isVisible = true;
        private boolean isCatalyst = false;

        IngredientBuilder(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public ViewerCategory.SlotBuilder variant(TransferVariant<?> variant) {
            if (variant instanceof ItemVariant item) {
                item(item.toStack());
            } else if (variant instanceof FluidVariant fluid) {
                isFluid = true;
                hasBackground = false;
                if (!fluid.isBlank()) {
                    ing = EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch());
                }
            } else {
                throw new IllegalArgumentException("Unknown variant type: " + variant.getClass());
            }
            return this;
        }

        @Override
        public ViewerCategory.SlotBuilder fluid(FluidVariant fluid, long amount, float probability) {
            isFluid = true;
            hasBackground = false;
            ing = EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch(), amount);
            processProbability(probability);
            return this;
        }

        @Override
        public ViewerCategory.SlotBuilder fluid(FluidIngredient ingredient, long amount, float probability) {
            isFluid = true;
            hasBackground = false;
            ing = EmiIngredient.of(
                    Stream.of(ingredient.getStacks())
                            .map(fs -> EmiStack.of(fs.getFluid(), fs.getComponentsPatch()))
                            .toList(),
                    amount);
            processProbability(probability);
            return this;
        }

        private void processProbability(float probability) {
            if (probability == 0) {
                markCatalyst();
            }
            ing.setChance(probability);
        }

        @Override
        public ViewerCategory.SlotBuilder item(ItemStack stack, float probability) {
            ing = EmiStack.of(stack);
            processProbability(probability);
            return this;
        }

        @Override
        public ViewerCategory.SlotBuilder ingredient(Ingredient ingredient, long amount, float probability) {
            ing = EmiIngredient.of(ingredient, amount);
            processProbability(probability);
            return this;
        }

        @Override
        public ViewerCategory.SlotBuilder removeBackground() {
            hasBackground = false;
            return this;
        }

        @Override
        public ViewerCategory.SlotBuilder markCatalyst() {
            isCatalyst = true;
            return this;
        }
    }

    private static void processIngredient(EmiRecipe recipe, WidgetHolder widgets, IngredientBuilder ing, boolean isInput) {
        if (!ing.isVisible) {
            return;
        }

        if (ing.isFluid) {
            createFluidSlotBackground(widgets, ing.x, ing.y);
        }
        var slot = widgets.addSlot(ing.ing, ing.x - 1 - 4, ing.y - 1 - 4);
        if (!isInput) {
            slot.recipeContext(recipe);
        }
        if (!ing.hasBackground) {
            slot.drawBack(false);
        } else if (!ing.isFluid) {
            slot.customBackground(MachineScreen.SLOT_ATLAS, isInput ? 0 : 36, 0, 18, 18);
        }
        if (ing.isCatalyst) {
            slot.catalyst(true);
            slot.appendTooltip(TextHelper.getNotConsumedTooltip());
        }
    }

    private static void createFluidSlotBackground(WidgetHolder widgets, int x, int y) {
        widgets.addDrawable(0, 0, 0, 0, (guiGraphics, mouseX, mouseY, delta) -> {
            guiGraphics.blit(MachineScreen.SLOT_ATLAS, x - 1 - 4, y - 1 - 4, 18, 0, 18, 18);
        });
    }

    public class ViewerRecipe implements EmiRecipe {
        final D recipe;
        private final List<EmiIngredient> inputs;
        private final List<EmiIngredient> catalysts;
        private final List<EmiStack> outputs;
        private final boolean excludeFromTree;

        public ViewerRecipe(D recipe, List<EmiIngredient> inputs, List<EmiIngredient> catalysts, List<EmiStack> outputs, boolean excludeFromTree) {
            this.recipe = recipe;
            this.inputs = inputs;
            this.catalysts = catalysts;
            this.outputs = outputs;
            this.excludeFromTree = excludeFromTree;
        }

        @Override
        public ViewerCategoryEmi<D> getCategory() {
            return ViewerCategoryEmi.this;
        }

        @Override
        public ResourceLocation getId() {
            return wrapped.getRecipeId(recipe);
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return inputs;
        }

        @Override
        public List<EmiIngredient> getCatalysts() {
            return catalysts;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return outputs;
        }

        @Override
        public int getDisplayWidth() {
            return wrapped.width - 8;
        }

        @Override
        public int getDisplayHeight() {
            return wrapped.height - 8;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            wrapped.buildWidgets(recipe, new ViewerCategory.WidgetList() {
                @Override
                public void text(Component text, float x, float y, ViewerCategory.TextAlign align, boolean shadow, boolean overrideColor,
                                 @Nullable Component tooltip) {
                    var font = Minecraft.getInstance().font;
                    var width = font.width(text);
                    var alignedX = (int) switch (align) {
                        case LEFT -> x;
                        case CENTER -> x - width / 2f;
                        case RIGHT -> x - width;
                    };
                    widgets.addText(text.getVisualOrderText(), alignedX - 4, (int) y - 4, overrideColor ? 0xFF404040 : -1, shadow);
                    if (tooltip != null) {
                        tooltip(alignedX, (int) y, width, font.lineHeight, List.of(tooltip));
                    }
                }

                @Override
                public void arrow(int x, int y) {
                    texture(MI.id("textures/gui/jei/arrow.png"), x, y, 0, 17, 24, 17);
                }

                @Override
                public void texture(ResourceLocation loc, int x, int y, int u, int v, int width, int height) {
                    widgets.addTexture(loc, x - 4, y - 4, width, height, u, v);
                }

                @Override
                public void drawable(Consumer<GuiGraphics> widget) {
                    widgets.addDrawable(-4, -4, 0, 0, (matrices, mouseX, mouseY, delta) -> {
                        widget.accept(matrices);
                    });
                }

                @Override
                public void tooltip(int x, int y, int w, int h, List<Component> tooltip) {
                    var mapped = tooltip.stream().map(c -> ClientTooltipComponent.create(c.getVisualOrderText())).toList();
                    widgets.addDrawable(x - 4, y - 4, w, h, (matrices, mouseX, mouseY, delta) -> {
                    }).tooltip((mouseX, mouseY) -> mapped);
                }

                @Override
                public void scrollableSlots(int cols, int rows, List<SizedIngredient> ingredients) {
                    int offsetX = (widgets.getWidth() / 2) - ((cols * 18) / 2);
                    int offsetY = 20;
                    int pageHeight = (widgets.getHeight() - 21) / 18;
                    int pageSize = cols * rows;
                    ViewerPageManager pages = new ViewerPageManager(ingredients, pageSize);
                    if (pageSize < ingredients.size()) {
                        widgets.addButton(2, 2, 12, 12, 0, 0, () -> true, (mx, my, button) -> {
                            pages.scroll(-1);
                        });
                        widgets.addButton(widgets.getWidth() - 14, 2, 12, 12, 12, 0, () -> true, (mx, my, button) -> {
                            pages.scroll(1);
                        });
                    }
                    int index = 0;
                    for (; index < ingredients.size() && index / cols <= pageHeight; index++) {
                        final int finalIndex = index;
                        widgets.add(new SlotWidget(EmiStack.EMPTY, index % cols * 18 + offsetX, index / cols * 18 + offsetY) {
                            @Override
                            public EmiIngredient getStack() {
                                var ingredient = pages.get(finalIndex);
                                return ingredient == null ? EmiStack.EMPTY : EmiIngredient.of(ingredient.ingredient());
                            }

                            @Override
                            public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                                var ingredient = pages.get(finalIndex);

                                Bounds bounds = getBounds();
                                int xOff = (bounds.width() - 16) / 2;
                                int yOff = (bounds.height() - 16) / 2;

                                // Draw count separately to handle amounts >= 100 reasonably
                                int stackX = bounds.x() + xOff;
                                int stackY = bounds.y() + yOff;
                                EmiIngredient.of(ingredient.ingredient()).render(draw, stackX, stackY, delta);
                                var amount = Component.literal(String.valueOf(ingredient.count()));
                                // Could use 1.0 if the count is < 100, but this looks more uniform
                                float scale = 0.8f;

                                draw.pose().pushPose();
                                draw.pose().translate(0, 0, 200);
                                int tx = stackX + 17 - Math.min(14, Math.round(Minecraft.getInstance().font.width(amount) * scale)) - 1;
                                draw.pose().translate(tx, stackY + 9, 0);
                                draw.pose().scale(scale, scale, 1);
                                draw.drawString(Minecraft.getInstance().font, amount, 0, 0, -1, true);
                                draw.pose().popPose();
                            }
                        }).customBackground(MachineScreen.SLOT_ATLAS, 0, 0, 18, 18);
                    }
                    for (; index < pageSize; index++) {
                        widgets.addSlot(EmiStack.EMPTY, index % cols * 18 + offsetX, index / cols * 18 + offsetY)
                                .customBackground(MachineScreen.SLOT_ATLAS, 0, 0, 18, 18);
                    }
                }
            });

            {
                List<IngredientBuilder> inputs = new ArrayList<>();
                List<IngredientBuilder> outputs = new ArrayList<>();

                processLayout(recipe, inputs, outputs);

                for (var input : inputs) {
                    processIngredient(this, widgets, input, true);
                }

                for (var output : outputs) {
                    processIngredient(this, widgets, output, false);
                }
            }
        }

        @Override
        public boolean supportsRecipeTree() {
            return !excludeFromTree && EmiRecipe.super.supportsRecipeTree();
        }
    }
}
