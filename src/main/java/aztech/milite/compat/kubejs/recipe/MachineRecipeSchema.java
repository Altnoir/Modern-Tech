package aztech.milite.compat.kubejs.recipe;

import aztech.milite.machines.recipe.condition.MachineProcessCondition;
import aztech.milite.machines.recipe.MachineRecipe;
import aztech.milite.machines.init.MachineLevel;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.IntBounds;
import dev.latvian.mods.kubejs.util.TickDuration;

import java.util.List;

public final class MachineRecipeSchema {
    private MachineRecipeSchema() {}

    private static <T> RecipeKey<List<T>> optionalList(RecipeComponentType<T> component, String name, ComponentRole role) {
        return component.instance()
                .asConditionalList()
                .orSelf()
                .withBounds(IntBounds.OPTIONAL)
                .key(name, role)
                .optional(List.of());
    }

    public static final RecipeKey<Integer> FE = NumberComponent.intRange(1, Integer.MAX_VALUE)
            .inputKey("energy")
            .functionNames(List.of("energy", "fe"));
    public static final RecipeKey<TickDuration> DURATION = TimeComponent.TICKS.inputKey("duration");
    public static final RecipeKey<String> LEVEL = StringComponent.STRING.instance()
            .otherKey("level")
            .optional(MachineLevel.BRONZE.getSerializedName())
            .noFunctions();
    public static final RecipeKey<List<MachineRecipe.ItemOutput>> ITEM_OUTPUTS = optionalList(ItemOutputComponent.TYPE, "item_outputs",
            ComponentRole.OUTPUT);
    public static final RecipeKey<List<MachineRecipe.FluidOutput>> FLUID_OUTPUTS = optionalList(FluidOutputComponent.TYPE, "fluid_outputs",
            ComponentRole.OUTPUT);
    public static final RecipeKey<List<MachineRecipe.ItemInput>> ITEM_INPUTS = optionalList(ItemInputComponent.TYPE, "item_inputs",
            ComponentRole.INPUT);
    public static final RecipeKey<List<MachineRecipe.FluidInput>> FLUID_INPUTS = optionalList(FluidInputComponent.TYPE, "fluid_inputs",
            ComponentRole.INPUT);
    public static final RecipeKey<List<MachineProcessCondition>> MACHINE_PROCESS_CONDITIONS = optionalList(
            MachineProcessConditionComponent.TYPE, "process_conditions", ComponentRole.OTHER);

    public static final RecipeSchema SCHEMA = new RecipeSchema(
            FE,
            DURATION,
            LEVEL,
            ITEM_OUTPUTS,
            FLUID_OUTPUTS,
            ITEM_INPUTS,
            FLUID_INPUTS,
            MACHINE_PROCESS_CONDITIONS)
                    .factory(MachineKubeRecipe.FACTORY)
                    .constructor(FE, DURATION);

    public static final RecipeSchema FORGE_HAMMER_SCHEMA = new RecipeSchema(
            ItemStackComponent.ITEM_STACK.outputKey("result"),
            IngredientComponent.INGREDIENT.inputKey("ingredient"),
            NumberComponent.INT.min(1).otherKey("count").optional(1));
}
