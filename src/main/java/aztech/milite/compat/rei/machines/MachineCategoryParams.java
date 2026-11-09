package aztech.milite.compat.rei.machines;

import aztech.milite.inventory.SlotPositions;
import aztech.milite.machines.guicomponents.ProgressBar;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.recipe.MachineRecipe;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.MI;
import aztech.milite.MIItem;
import aztech.milite.MIText;
import aztech.milite.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.List;

public class MachineCategoryParams {
    public final String englishName;
    public final ResourceLocation category;
    public final SlotPositions itemInputs;
    public final SlotPositions itemOutputs;
    public final SlotPositions fluidInputs;
    public final SlotPositions fluidOutputs;
    public final ProgressBar.Params progressBarParams;
    public final MachineRecipeType recipeType;
    public final Predicate<MachineRecipe> recipePredicate;
    public final boolean isMultiblock;
    public final SteamMode steamMode;
    public final List<ResourceLocation> workstations = new ArrayList<>();

    public MachineCategoryParams(String englishName, ResourceLocation category, SlotPositions itemInputs, SlotPositions itemOutputs,
            SlotPositions fluidInputs, SlotPositions fluidOutputs, ProgressBar.Params progressBarParams, MachineRecipeType recipeType,
            Predicate<MachineRecipe> recipePredicate, boolean isMultiblock, SteamMode steamMode) {
        this.englishName = englishName;
        this.category = category;
        this.itemInputs = itemInputs;
        this.itemOutputs = itemOutputs;
        this.fluidInputs = fluidInputs;
        this.fluidOutputs = fluidOutputs;
        this.progressBarParams = progressBarParams;
        this.recipeType = recipeType;
        this.recipePredicate = recipePredicate;
        this.isMultiblock = isMultiblock;
        this.steamMode = steamMode;
    }

    public record Dimensions(int minX, int minY, int width, int height) {}

    public Dimensions calculateDimensions() {
        int x = 1000, X = 0;
        int y = 1000, Y = 0;
        for (SlotPositions positions : new SlotPositions[] { itemInputs, itemOutputs, fluidInputs, fluidOutputs }) {
            for (int i = 0; i < positions.size(); ++i) {
                x = Math.min(x, positions.getX(i));
                X = Math.max(X, positions.getX(i) + 16);
                y = Math.min(y, positions.getY(i));
                Y = Math.max(Y, positions.getY(i) + 16);
            }
        }

        int width = X - x;
        int height = Y - y;

        return new Dimensions(x, y, width, height);
    }

    private boolean steelHatchRequired(MachineRecipe recipe) {
        return steamMode.steam && isMultiblock && recipe.level == MachineLevel.STEEL;
    }

    public boolean includeRecipeConditionDisplay(MachineRecipe recipe) {
        return steelHatchRequired(recipe) ||
                !recipe.conditions.isEmpty();
    }

    public List<ItemStack> getRecipeConditionDisplayItems(MachineRecipe recipe) {
        List<ItemStack> items = new ArrayList<>();
        if (steelHatchRequired(recipe)) {
            items.add(BuiltInRegistries.ITEM.get(MI.id("steel_item_input_hatch")).getDefaultInstance());
        }
        for (var condition : recipe.conditions) {
            ItemStack displayedItem = condition.icon();
            if (!displayedItem.isEmpty()) {
                items.add(displayedItem);
            }
        }
        if (items.isEmpty()) {
            items.add(MIItem.WRENCH.stack());
        }
        return items;
    }

    public List<Component> getRecipeTooltip(MachineRecipe recipe) {
        List<Component> lines = new ArrayList<>();
        if (steamMode != SteamMode.NEITHER) {
            lines.add(MIText.BaseFeTotal.text(TextHelper.getFeText(recipe.getTotalFe())).withStyle(TextHelper.GOLD));
        }
        if (steamMode.steam && MachineLevel.STEEL.canCraft(recipe.level)) {
            lines.add((steamMode.electric ? MIText.AcceptsSteamToo : MIText.AcceptsSteam).text().withStyle(ChatFormatting.GRAY));
            if (steelHatchRequired(recipe)) {
                lines.add(MIText.RequiresSteelHatch0.text().setStyle(Style.EMPTY.withUnderlined(true)));
                lines.add(MIText.RequiresSteelHatch1.text().withStyle(ChatFormatting.GRAY));
            }
        }
        for (var condition : recipe.conditions) {
            condition.appendDescription(lines);
        }
        return lines;
    }
}
