package aztech.milite.machines.recipe;

import aztech.milite.MI;
import aztech.milite.recipe.json.MIRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;

public class MachineRecipeBuilder extends MIRecipeJson<MachineRecipeBuilder> implements MIRecipeBuilder {
    public MachineRecipeBuilder(MachineRecipeType machineRecipeType, int energy, int duration) {
        super(machineRecipeType, energy, duration);
    }

    public MachineRecipeBuilder(MIRecipeJson<?> otherWithSameData) {
        super(otherWithSameData);
    }

    @Override
    public void offerTo(RecipeOutput recipeOutput, String path) {
        var id = MI.id(path);
        try {
            clampBuiltInLevel();
            recipeOutput.accept(id, recipe, null);
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to generate recipe {}", id);
            throw new RuntimeException(ex);
        }
    }
}
