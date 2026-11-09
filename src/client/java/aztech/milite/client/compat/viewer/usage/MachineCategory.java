package aztech.milite.client.compat.viewer.usage;

import aztech.milite.client.compat.viewer.abstraction.ViewerCategory;
import aztech.milite.client.machines.gui.MachineScreen;
import aztech.milite.client.machines.guicomponents.EnergyBarClient;
import aztech.milite.client.machines.guicomponents.ProgressBarClient;
import aztech.milite.compat.rei.machines.MachineCategoryParams;
import aztech.milite.compat.rei.machines.SteamMode;
import aztech.milite.inventory.SlotPositions;
import aztech.milite.machines.init.MachineRegistrationHelper;
import aztech.milite.machines.init.MachineRegistrationHelper.RecipeMachineVariant;
import aztech.milite.machines.recipe.MachineRecipe;
import aztech.milite.MI;
import aztech.milite.MIText;
import aztech.milite.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.List;

public class MachineCategory extends ViewerCategory<RecipeHolder<MachineRecipe>> {
    private static final Style TOOLTIP_SPACING = Style.EMPTY.withFont(MI.id("tooltip_spacing"));

    public static MachineCategory create(MachineCategoryParams params) {
        var dimensions = params.calculateDimensions();
        int width = Math.max(dimensions.width() + 15, 120);
        int height = dimensions.height() + 25; // Room for text above
        return new MachineCategory(params, width, height);
    }

    private final MachineCategoryParams params;
    private final List<RecipeMachineVariant> variants;

    private MachineCategory(MachineCategoryParams params, int width, int height) {
        super(recipeHolderClass(), params.category,
                Component.translatable("rei_categories.%s.%s".formatted(params.category.getNamespace(), params.category.getPath())),
                BuiltInRegistries.ITEM.get(params.workstations.get(0)).getDefaultInstance(), width, height);

        this.params = params;
        this.variants = MachineRegistrationHelper.recipeMachines(params.recipeType).stream()
                .filter(variant -> params.workstations.contains(variant.workstation()))
                .sorted(Comparator.comparing(RecipeMachineVariant::level))
                .toList();
    }

    @Override
    public void buildWorkstations(WorkstationConsumer consumer) {
        for (ResourceLocation workstation : params.workstations) {
            consumer.accept(BuiltInRegistries.ITEM.get(workstation));
        }
    }

    @Override
    public void buildRecipes(RecipeManager recipeManager, RegistryAccess registryAccess, Consumer<RecipeHolder<MachineRecipe>> consumer) {
        params.recipeType.getRecipesWithoutCache(Minecraft.getInstance().level).stream()
                .filter(recipe -> params.recipePredicate.test(recipe.value()))
                .sorted(Comparator.comparing(RecipeHolder::id))
                .forEach(consumer);
    }

    @Override
    public void buildLayout(RecipeHolder<MachineRecipe> recipe, LayoutBuilder builder) {
        addItemInputs(builder, recipe.value());
        addItemOutputs(builder, recipe.value());
        addFluidInputs(builder, recipe.value());
        addFluidOutputs(builder, recipe.value());
    }

    private void addItemInputs(LayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.itemInputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.inputSlot(x, y);

            if (i < recipe.itemInputs.size()) {
                var input = recipe.itemInputs.get(i);
                slot.ingredient(input.ingredient(), input.amount(), input.probability());
                if (input.reusable()) {
                    slot.markCatalyst();
                }
            }
        }
    }

    private void addItemOutputs(LayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.itemOutputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.outputSlot(x, y);

            if (i < recipe.itemOutputs.size()) {
                var output = recipe.itemOutputs.get(i);
                slot.item(output.getStack(), output.probability());
            }
        }
    }

    private void addFluidInputs(LayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.fluidInputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.inputSlot(x, y);

            if (i < recipe.fluidInputs.size()) {
                var input = recipe.fluidInputs.get(i);
                slot.fluid(input.fluid(), input.amount(), input.probability());
            } else {
                slot.variant(FluidVariant.blank());
            }
        }
    }

    private void addFluidOutputs(LayoutBuilder builder, MachineRecipe recipe) {
        var offset = getOffset();

        var positions = params.fluidOutputs;
        for (int i = 0; i < positions.size(); i++) {
            var x = offset.x + positions.getX(i);
            var y = offset.y + positions.getY(i);
            var slot = builder.outputSlot(x, y);

            if (i < recipe.fluidOutputs.size()) {
                var output = recipe.fluidOutputs.get(i);
                slot.fluid(FluidVariant.of(output.fluid()), output.amount(), output.probability());
            } else {
                slot.variant(FluidVariant.blank());
            }
        }
    }

    private static void addTimeTooltips(List<Component> tooltips, Font font, MachineRecipe recipe, List<RecipeMachineVariant> compatible) {
        if (compatible.isEmpty()) {
            tooltips.add(MIText.RecipeTimeUnavailable.text());
            return;
        }

        var labels = new ArrayList<Component>();
        int columnStart = 0;
        for (var variant : compatible) {
            Component name = variant.level().getDisplayName();
            if (variant.coil() != null) {
                name = Component.translatable(variant.coil().toLanguageKey("block")).append(" (").append(name).append(")");
            }
            var label = MIText.RecipeTimeAtLevel.text(name, Component.empty());
            labels.add(label);
            columnStart = Math.max(columnStart, font.width(label));
        }

        for (int i = 0; i < compatible.size(); i++) {
            var label = labels.get(i);
            int padding = columnStart - font.width(label);
            var time = durationText(getProcessingTicks(recipe, compatible.get(i)));
            // The spacing font advances exactly one pixel per space, regardless of the active text font.
            tooltips.add(Component.empty().append(label).append(Component.literal(" ".repeat(padding)).withStyle(TOOLTIP_SPACING)).append(time));
        }
    }

    private static int getProcessingTicks(MachineRecipe recipe, RecipeMachineVariant variant) {
        return recipe.getProcessingTicks(variant.level(), variant.speedBonus());
    }

    private static Component durationText(int ticks) {
        var seconds = BigDecimal.valueOf(ticks * 5L, 2).stripTrailingZeros().toPlainString();
        return MIText.BaseDurationSeconds.text(seconds);
    }

    private static Component fitText(Font font, Component text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        return Component.literal(font.plainSubstrByWidth(text.getString(), Math.max(0, maxWidth - font.width(ellipsis))) + ellipsis);
    }

    @Override
    public void buildWidgets(RecipeHolder<MachineRecipe> recipeHolder, WidgetList widgets) {
        var offset = getOffset();
        var recipe = recipeHolder.value();

        var compatible = variants.stream().filter(variant -> variant.level().canCraft(recipe.level)).toList();
        int processingTicks = compatible.isEmpty() ? 0 : getProcessingTicks(recipe, compatible.getFirst());
        widgets.drawable(matrices -> {
            ProgressBarClient.renderProgress(matrices, offset.x, offset.y, params.progressBarParams,
                    processingTicks == 0 ? 0 : (float) (System.currentTimeMillis() / (processingTicks * 50.0) % 1.0));
        });

        widgets.drawable(guiGraphics -> {
            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(5, 5, 0);
            guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
            switch (params.steamMode) {
                case BOTH -> {
                    guiGraphics.blit(MachineScreen.SLOT_ATLAS, 0, 0, 80, 19, 18, 18);
                }
                case STEAM_ONLY -> {
                    guiGraphics.blit(MI.id("textures/item/steam_bucket.png"), 0, 0, 0, 0, 16, 16, 16, 16);
                }
                case ELECTRIC_ONLY -> {
                    EnergyBarClient.Renderer.renderEnergy(guiGraphics, 0, 0, 1);
                }
            }

            guiGraphics.pose().popPose();
        });
        var font = Minecraft.getInstance().font;
        boolean showCondition = params.includeRecipeConditionDisplay(recipe);
        int startX = 15 + (params.steamMode.steam ? 2 : 0);
        int rightX = width - 5;
        int conditionSpace = showCondition ? 14 : 3;
        int levelSpace = params.steamMode == SteamMode.NEITHER ? 0 : 20;
        var durationText = fitText(font, processingTicks == 0 ? Component.literal("—") : durationText(processingTicks),
                rightX - startX - conditionSpace - levelSpace);
        int durationStart = rightX - font.width(durationText);
        int endOfLevelText = startX;
        List<Component> tooltips = new ArrayList<>();
        if (params.steamMode != SteamMode.NEITHER) {
            var levelText = recipe.level.getDisplayName();
            var fittedLevel = fitText(font, levelText, durationStart - startX - conditionSpace);
            widgets.text(fittedLevel, startX, 5, TextAlign.LEFT, false, true, null);
            endOfLevelText += font.width(fittedLevel);
            if (fittedLevel != levelText) {
                tooltips.add(levelText);
            }
        }
        widgets.text(durationText, rightX, 5, TextAlign.RIGHT, false, true, null);

        if (showCondition) {
            List<ItemStack> displayedItems = params.getRecipeConditionDisplayItems(recipe);

            double x = Math.min(durationStart - 12, Math.max(width / 2f - 3, endOfLevelText + 1));
            double y = 3.75;
            double wh = 10.8;
            widgets.drawable(graphics -> {
                int itemIndex = (int) ((System.currentTimeMillis() / 1500L) % displayedItems.size());
                ItemStack displayedItem = displayedItems.get(itemIndex).copyWithCount(1);

                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale((float) wh / 16, (float) wh / 16, 1);
                graphics.renderFakeItem(displayedItem, 0, 0);
                graphics.pose().popPose();
            });
        }
        addTimeTooltips(tooltips, font, recipe, compatible);
        if (params.steamMode != SteamMode.NEITHER) {
            tooltips.add(Component.empty());
        }
        tooltips.addAll(params.getRecipeTooltip(recipe));
        widgets.tooltip(3, 3, width - 6, 13, tooltips);
    }

    private DrawOffset getOffset() {
        int x = 1000, X = 0, y = 1000, Y = 0;
        for (SlotPositions positions : new SlotPositions[] { params.itemInputs, params.itemOutputs, params.fluidInputs, params.fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                x = Math.min(x, positions.getX(i));
                X = Math.max(X, positions.getX(i) + 16);
                y = Math.min(y, positions.getY(i));
                Y = Math.max(Y, positions.getY(i) + 16);
            }
        }
        int xoffset = (width - X + x) / 2 - x;
        int yoffset = 17 - y;

        return new DrawOffset(xoffset, yoffset);
    }

    private record DrawOffset(int x, int y) {}

    @Override
    public ResourceLocation getRecipeId(RecipeHolder<MachineRecipe> recipe) {
        return recipe.id();
    }
}
