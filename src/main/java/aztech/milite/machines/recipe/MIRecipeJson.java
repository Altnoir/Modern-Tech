package aztech.milite.machines.recipe;

import aztech.milite.definition.FluidLike;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.init.MIMachineRecipeTypes;
import aztech.milite.machines.init.MachineRegistrationHelper;
import aztech.milite.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.Map;

@SuppressWarnings({ "FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection" })
public class MIRecipeJson<T extends MIRecipeJson<?>> {
    /** The energy per tick of a recipe that a bronze machine, and not just an electric one, can run. */
    private static final int BRONZE_ENERGY = 2;

    protected final MachineRecipe recipe;

    protected final void clampBuiltInLevel() {
        recipe.level = MachineRegistrationHelper.clampBuiltInRecipeLevel(recipe.type, recipe.level);
    }

    protected MIRecipeJson(MachineRecipeType machineRecipeType, int energy, int duration) {
        this.recipe = new MachineRecipe(machineRecipeType);
        recipe.energy = energy;
        recipe.duration = duration;
        recipe.level = defaultLevel(energy);
    }

    /**
     * The level a recipe gets when it does not state one.
     *
     * <p>
     * The energy a recipe costs is what used to decide which machine could run it, and the two standard values still
     * carry that meaning: two is a recipe for the bronze and steel machines, anything above that is a recipe for the
     * electric ones. Deriving the level here keeps that meaning in a single place instead of repeating it at every call
     * site. A recipe whose level is not implied by its energy, such as a blast furnace recipe, overrides the result with
     * {@link #level(MachineLevel)}.
     */
    private static MachineLevel defaultLevel(int energy) {
        return energy == BRONZE_ENERGY ? MachineLevel.BRONZE : MachineLevel.ELECTRIC;
    }

    protected MIRecipeJson(MIRecipeJson<?> otherWithSameData) {
        this((MachineRecipeType) otherWithSameData.recipe.getType(), otherWithSameData.recipe.energy,
                otherWithSameData.recipe.duration);

        recipe.level = otherWithSameData.recipe.level;
        recipe.itemInputs.addAll(otherWithSameData.recipe.itemInputs);
        recipe.fluidInputs.addAll(otherWithSameData.recipe.fluidInputs);
        recipe.itemOutputs.addAll(otherWithSameData.recipe.itemOutputs);
        recipe.fluidOutputs.addAll(otherWithSameData.recipe.fluidOutputs);
    }

    /**
     * @param level the machine level this recipe needs, see {@link MachineLevel}
     */
    public T level(MachineLevel level) {
        recipe.level = level;
        return (T) this;
    }

    /**
     * Sets the required level by its serialized name, such as {@code "bronze"}, {@code "electric"}, or
     * {@code "stainless_steel"}. Unknown names fail during data generation instead of silently changing a recipe.
     */
    public T level(String level) {
        return level(MachineLevel.byName(level));
    }

    public static MIRecipeJson<MIRecipeJson<?>> create(MachineRecipeType machineRecipeType, int fe, int duration) {
        return new MIRecipeJson<>(machineRecipeType, fe, duration);
    }

    public T addItemInput(String maybeTag, int amount) {
        return addItemInput(maybeTag, amount, 1);
    }

    public T addItemInput(TagKey<Item> tag, int amount) {
        return addItemInput(tag, amount, 1);
    }

    public T addItemInput(TagKey<Item> tag, int amount, float probability) {
        return addItemInput("#" + tag.location(), amount, probability);
    }

    private static Ingredient itemIngredient(String maybeTag) {
        Ingredient ing;
        if (maybeTag.startsWith("#")) {
            ing = Ingredient.of(ItemTags.create(ResourceLocation.parse(maybeTag.substring(1))));
        } else {
            if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(maybeTag))) {
                throw new RuntimeException("Could not find item " + maybeTag);
            }
            ing = Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse(maybeTag)));
        }
        return ing;
    }

    public T addItemInput(String maybeTag, int amount, float probability) {
        return addItemInput(itemIngredient(maybeTag), amount, probability);
    }

    public T addReusableItemInput(String maybeTag, int amount) {
        recipe.itemInputs.add(new MachineRecipe.ItemInput(itemIngredient(maybeTag), amount, 1f, true));
        return (T) this;
    }

    public T addItemInput(ItemLike item, int amount) {
        return addItemInput(item.asItem(), amount, 1);
    }

    public T addItemInput(ItemLike item, int amount, float probability) {
        return addItemInput(Ingredient.of(item), amount, probability);
    }

    public T addItemInput(Ingredient ingredient, int amount, float probability) {
        recipe.itemInputs.add(new MachineRecipe.ItemInput(ingredient, amount, probability));
        return (T) this;
    }

    public T addItemOutput(String itemId, int amount) {
        return addItemOutput(itemId, amount, 1);
    }

    public T addItemOutput(String itemId, int amount, float probability) {
        return addItemOutput(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId)), amount, probability);
    }

    public T addItemOutput(ItemLike item, int amount) {
        return addItemOutput(item, amount, 1);
    }

    public T addItemOutput(Item item, int amount) {
        return addItemOutput(item, amount, 1);
    }

    public T addItemOutput(ItemLike item, int amount, float probability) {
        return addItemOutput(ItemVariant.of(item), amount, probability);
    }

    public T addItemOutput(ItemVariant variant, int amount, float probability) {
        recipe.itemOutputs.add(new MachineRecipe.ItemOutput(variant, amount, probability));
        return (T) this;
    }

    public T addFluidInput(String fluid, int amount) {
        return addFluidInput(fluid, amount, 1);
    }

    public T addFluidInput(String fluid, int amount, float probability) {
        return addFluidInput(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluid)), amount, probability);
    }

    public T addFluidInput(FluidLike fluid, int amount, float probability) {
        return addFluidInput(fluid.asFluid(), amount, probability);
    }

    public T addFluidInput(FluidLike fluid, int amount) {
        return addFluidInput(fluid.asFluid(), amount);
    }

    public T addFluidInput(Fluid fluid, int amount) {
        return addFluidInput(fluid, amount, 1);
    }

    public T addFluidInput(Fluid fluid, int amount, float probability) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id.equals(BuiltInRegistries.FLUID.getDefaultKey())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        return addFluidInput(FluidIngredient.of(fluid), amount, probability);
    }

    public T addFluidInput(TagKey<Fluid> tag, int amount) {
        return addFluidInput(tag, amount, 1);
    }

    public T addFluidInput(TagKey<Fluid> tag, int amount, float probability) {
        return addFluidInput(FluidIngredient.tag(tag), amount, probability);
    }

    public T addFluidInput(FluidIngredient ingredient, int amount, float probability) {
        recipe.fluidInputs.add(new MachineRecipe.FluidInput(ingredient, amount, probability));
        return (T) this;
    }

    public T addFluidOutput(String fluid, int amount) {
        return addFluidOutput(fluid, amount, 1);
    }

    public T addFluidOutput(FluidLike fluid, int amount) {
        return addFluidOutput(fluid.asFluid(), amount);
    }

    public T addFluidOutput(String fluid, int amount, float probability) {
        return addFluidOutput(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluid)), amount, probability);
    }

    public T addFluidOutput(Fluid fluid, int amount) {
        return addFluidOutput(fluid, amount, 1);
    }

    public T addFluidOutput(Fluid fluid, int amount, float probability) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id.equals(BuiltInRegistries.FLUID.getDefaultKey())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        recipe.fluidOutputs.add(new MachineRecipe.FluidOutput(fluid, amount, probability));
        return (T) this;
    }

    public static MIRecipeJson<?> assemblerFromShaped(ShapedRecipe recipe) {
        return fromShaped(
                MIMachineRecipeTypes.ASSEMBLER,
                8, 200, 1,
                recipe.result,
                recipe.pattern.data.get().pattern().toArray(String[]::new),
                recipe.pattern.data.get().key());
    }

    public static MIRecipeJson<?> fromShaped(
            MachineRecipeType machine,
            int fe, int duration, int division,
            ItemStack result,
            String[] pattern,
            Map<Character, Ingredient> key) {
        if (result.getCount() % division != 0) {
            throw new IllegalArgumentException("Output must be divisible by division");
        }

        MIRecipeJson<?> assemblerJson = MIRecipeJson.create(machine, fe, duration).addItemOutput(result.getItem(), result.getCount() / division);
        for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
            int count = 0;
            for (String row : pattern) {
                for (char c : row.toCharArray()) {
                    if (c == entry.getKey()) {
                        count++;
                    }
                }
            }

            if (count % division != 0) {
                throw new IllegalArgumentException("Input must be divisible by division");
            }

            Ingredient input = entry.getValue();
            assemblerJson.addItemInput(input, count / division, 1);
        }

        return assemblerJson;
    }
}
