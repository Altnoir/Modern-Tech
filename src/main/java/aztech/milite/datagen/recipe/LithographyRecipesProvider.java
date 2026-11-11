package aztech.milite.datagen.recipe;

import aztech.milite.MIItem;
import aztech.milite.machines.init.MIMachineRecipeTypes;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.recipe.MachineRecipeBuilder;
import aztech.milite.materials.MIMaterials;
import aztech.milite.materials.part.MIParts;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;

public final class LithographyRecipesProvider extends MIRecipesProvider {
    public LithographyRecipesProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        new MachineRecipeBuilder(MIMachineRecipeTypes.LITHOGRAPHY, 4, 3200)
                .level(MachineLevel.STAINLESS_STEEL)
                .addItemInput(MIMaterials.ANNEALED_COPPER.getPart(MIParts.PLATE), 2)
                .addItemInput(MIItem.SILICON_WAFER, 1)
                .addItemOutput(MIItem.PROCESSOR, 1)
                .offerTo(consumer, "lithography/processor");
        new MachineRecipeBuilder(MIMachineRecipeTypes.LITHOGRAPHY, 8, 6400)
                .level(MachineLevel.TITANIUM)
                .addItemInput(MIMaterials.SUPERCONDUCTOR.getPart(MIParts.PLATE), 2)
                .addItemInput(MIItem.SILICON_WAFER, 1)
                .addItemOutput(MIItem.QUANTUM_PROCESSOR, 1)
                .offerTo(consumer, "lithography/quantum_processor");
    }
}
