package aztech.milite.materials.recipe.builder;

import static aztech.milite.materials.property.MaterialProperty.HARDNESS;

import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.machines.recipe.MIRecipeJson;
import aztech.milite.materials.MaterialBuilder;
import aztech.milite.materials.part.MaterialItemPart;
import aztech.milite.materials.part.PartKeyProvider;
import aztech.milite.MI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import org.jspecify.annotations.Nullable;

public class MIRecipeBuilder extends MIRecipeJson<MIRecipeBuilder> implements MaterialRecipeBuilder {
    public final String recipeId;
    private final MaterialBuilder.RecipeContext context;
    private boolean canceled = false;

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, MachineRecipeType type, String recipeSuffix, int fe, int duration) {
        super(type, fe, duration);
        this.recipeId = type.getPath() + "/" + recipeSuffix;
        this.context = context;
        context.addRecipe(this);
    }

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, String recipeSuffix,
            MIRecipeJson<?> otherWithSameRecipeData) {
        super(otherWithSameRecipeData);
        this.recipeId = BuiltInRegistries.RECIPE_TYPE.getKey(this.recipe.getType()).getPath() + "/" + recipeSuffix;
        this.context = context;
        context.addRecipe(this);
    }

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, MachineRecipeType type, String recipeSuffix) {
        this(context, type, recipeSuffix, 2, (int) (200 * context.get(HARDNESS).timeFactor));
    }

    public MIRecipeBuilder(MaterialBuilder.RecipeContext context, MachineRecipeType type, PartKeyProvider recipeSuffix) {
        this(context, type, recipeSuffix.key().key);
    }

    public MIRecipeBuilder addPartInput(PartKeyProvider part, int amount) {
        return addPartInput(context.getPart(part), amount);
    }

    public MIRecipeBuilder addTaggedPartInput(PartKeyProvider part, int amount) {
        return addTaggedPartInput(context.getPart(part), amount);
    }

    // TODO: remove these two if the part is always a string passed through
    // addPartInput
    public MIRecipeBuilder addPartInput(@Nullable MaterialItemPart part, int amount) {
        if (part == null) {
            canceled = true;
        } else {
            addItemInput(part.getItemId(), amount);
        }
        return this;
    }

    public MIRecipeBuilder addTaggedPartInput(@Nullable MaterialItemPart part, int amount) {
        if (part == null) {
            canceled = true;
        } else {
            addItemInput(part.getTaggedItemId(), amount);
        }
        return this;
    }

    public MIRecipeBuilder addPartOutput(PartKeyProvider part, int amount) {
        return addPartOutput(context.getPart(part), amount);
    }

    public MIRecipeBuilder addPartOutput(@Nullable MaterialItemPart part, int amount) {
        if (part == null) {
            canceled = true;
        } else {
            return addItemOutput(part.getItemId(), amount);
        }
        return this;
    }

    public MIRecipeBuilder addPartOutput(PartKeyProvider part, int amount, float probability) {
        return addPartOutput(context.getPart(part), amount, probability);
    }

    public MIRecipeBuilder addPartOutput(@Nullable MaterialItemPart part, int amount, float probability) {
        if (part == null) {
            canceled = true;
        } else {
            return addItemOutput(part.getItemId(), amount, probability);
        }
        return this;
    }

    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public String getRecipeId() {
        return recipeId;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        if (!canceled) {
            String fullId = "materials/" + context.getMaterialName() + "/" + recipeId;
            clampBuiltInLevel();
            recipeOutput.accept(MI.id(fullId), recipe, null);
        }
    }
}
