package aztech.milite.datagen.recipe;

import aztech.milite.MIBlock;
import aztech.milite.MIFluids;
import aztech.milite.MIItem;
import aztech.milite.machines.init.MIMachineRecipeTypes;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.recipe.MachineRecipeBuilder;
import aztech.milite.recipe.json.ShapedRecipeJson;
import aztech.milite.recipe.json.ShapelessRecipeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

/**
 * Datagen for recipes that produce vanilla materials using MI machines.
 */
public class VanillaCompatRecipesProvider extends MIRecipesProvider {
    public VanillaCompatRecipesProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void buildRecipes(RecipeOutput exporter) {
        generateConcrete(exporter);
        generateDyeMixing(exporter);
        generateCoralPropagation(exporter);

        generateCopperOxidation(exporter, Items.COPPER_BLOCK, Items.EXPOSED_COPPER, Items.WEATHERED_COPPER, Items.OXIDIZED_COPPER);
        generateCopperOxidation(exporter, Items.CUT_COPPER, Items.EXPOSED_CUT_COPPER, Items.WEATHERED_CUT_COPPER, Items.OXIDIZED_CUT_COPPER);
        generateCopperOxidation(exporter, Items.CUT_COPPER_SLAB, Items.EXPOSED_CUT_COPPER_SLAB, Items.WEATHERED_CUT_COPPER_SLAB, Items.OXIDIZED_CUT_COPPER_SLAB);
        generateCopperOxidation(exporter, Items.CUT_COPPER_STAIRS, Items.EXPOSED_CUT_COPPER_STAIRS, Items.WEATHERED_CUT_COPPER_STAIRS, Items.OXIDIZED_CUT_COPPER_STAIRS);
        generateCopperOxidation(exporter, Items.COPPER_BULB, Items.EXPOSED_COPPER_BULB, Items.WEATHERED_COPPER_BULB, Items.OXIDIZED_COPPER_BULB);
        generateCopperOxidation(exporter, Items.COPPER_DOOR, Items.EXPOSED_COPPER_DOOR, Items.WEATHERED_COPPER_DOOR, Items.OXIDIZED_COPPER_DOOR);
        generateCopperOxidation(exporter, Items.COPPER_TRAPDOOR, Items.EXPOSED_COPPER_TRAPDOOR, Items.WEATHERED_COPPER_TRAPDOOR, Items.OXIDIZED_COPPER_TRAPDOOR);
        generateCopperOxidation(exporter, Items.CHISELED_COPPER, Items.EXPOSED_CHISELED_COPPER, Items.WEATHERED_CHISELED_COPPER, Items.OXIDIZED_CHISELED_COPPER);
        generateCopperOxidation(exporter, Items.COPPER_GRATE, Items.EXPOSED_COPPER_GRATE, Items.WEATHERED_COPPER_GRATE, Items.OXIDIZED_COPPER_GRATE);
        // waxed variants
        generateCopperOxidation(exporter, Items.WAXED_COPPER_BLOCK, Items.WAXED_EXPOSED_COPPER, Items.WAXED_WEATHERED_COPPER, Items.WAXED_OXIDIZED_COPPER);
        generateCopperOxidation(exporter, Items.WAXED_CUT_COPPER, Items.WAXED_EXPOSED_CUT_COPPER, Items.WAXED_WEATHERED_CUT_COPPER, Items.WAXED_OXIDIZED_CUT_COPPER);
        generateCopperOxidation(exporter, Items.WAXED_CUT_COPPER_SLAB, Items.WAXED_EXPOSED_CUT_COPPER_SLAB, Items.WAXED_WEATHERED_CUT_COPPER_SLAB, Items.WAXED_OXIDIZED_CUT_COPPER_SLAB);
        generateCopperOxidation(exporter, Items.WAXED_CUT_COPPER_STAIRS, Items.WAXED_EXPOSED_CUT_COPPER_STAIRS, Items.WAXED_WEATHERED_CUT_COPPER_STAIRS, Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
        generateCopperOxidation(exporter, Items.WAXED_COPPER_BULB, Items.WAXED_EXPOSED_COPPER_BULB, Items.WAXED_WEATHERED_COPPER_BULB, Items.WAXED_OXIDIZED_COPPER_BULB);
        generateCopperOxidation(exporter, Items.WAXED_COPPER_DOOR, Items.WAXED_EXPOSED_COPPER_DOOR, Items.WAXED_WEATHERED_COPPER_DOOR, Items.WAXED_OXIDIZED_COPPER_DOOR);
        generateCopperOxidation(exporter, Items.WAXED_COPPER_TRAPDOOR, Items.WAXED_EXPOSED_COPPER_TRAPDOOR, Items.WAXED_WEATHERED_COPPER_TRAPDOOR, Items.WAXED_OXIDIZED_COPPER_TRAPDOOR);
        generateCopperOxidation(exporter, Items.WAXED_CHISELED_COPPER, Items.WAXED_EXPOSED_CHISELED_COPPER, Items.WAXED_WEATHERED_CHISELED_COPPER, Items.WAXED_OXIDIZED_CHISELED_COPPER);
        generateCopperOxidation(exporter, Items.WAXED_COPPER_GRATE, Items.WAXED_EXPOSED_COPPER_GRATE, Items.WAXED_WEATHERED_COPPER_GRATE, Items.WAXED_OXIDIZED_COPPER_GRATE);

        new MachineRecipeBuilder(MIMachineRecipeTypes.CENTRIFUGE, 2, 100).level(MachineLevel.ALUMINUM)
                .addItemInput(Items.HONEYCOMB, 1)
                .addItemOutput(MIItem.WAX, 4)
                .offerTo(exporter, "vanilla_recipes/centrifuge/honeycomb");

        ShapelessRecipeBuilder.shapeless(MIItem.WAX)
                .requires(Items.HONEYCOMB)
                .offerTo(exporter, "wax_from_honeycomb");

        ShapelessRecipeBuilder.shapeless(MIBlock.WATER_BARREL.asItem())
                .requires(Items.BARREL)
                .requires(MIItem.WAX)
                .offerTo(exporter, "water_barrel");

        new ShapedRecipeJson(MIBlock.WATER_BARREL.id(), 1, "PSP", "PWP", "PSP")
                .addInput('P', "#" + ItemTags.PLANKS.location())
                .addInput('S', "#" + ItemTags.WOODEN_SLABS.location())
                .addInput('W', MIItem.WAX.id())
                .offerTo(exporter, "water_barrel_from_planks");

        generateWax(exporter, Items.COPPER_BLOCK, Items.WAXED_COPPER_BLOCK);
        generateWax(exporter, Items.CUT_COPPER, Items.WAXED_CUT_COPPER);
        generateWax(exporter, Items.CUT_COPPER_SLAB, Items.WAXED_CUT_COPPER_SLAB);
        generateWax(exporter, Items.CUT_COPPER_STAIRS, Items.WAXED_CUT_COPPER_STAIRS);
        generateWax(exporter, Items.COPPER_BULB, Items.WAXED_COPPER_BULB);
        generateWax(exporter, Items.COPPER_DOOR, Items.WAXED_COPPER_DOOR);
        generateWax(exporter, Items.COPPER_TRAPDOOR, Items.WAXED_COPPER_TRAPDOOR);
        generateWax(exporter, Items.CHISELED_COPPER, Items.WAXED_CHISELED_COPPER);
        generateWax(exporter, Items.COPPER_GRATE, Items.WAXED_COPPER_GRATE);
        generateWax(exporter, Items.EXPOSED_COPPER, Items.WAXED_EXPOSED_COPPER);
        generateWax(exporter, Items.EXPOSED_CUT_COPPER, Items.WAXED_EXPOSED_CUT_COPPER);
        generateWax(exporter, Items.EXPOSED_CUT_COPPER_SLAB, Items.WAXED_EXPOSED_CUT_COPPER_SLAB);
        generateWax(exporter, Items.EXPOSED_CUT_COPPER_STAIRS, Items.WAXED_EXPOSED_CUT_COPPER_STAIRS);
        generateWax(exporter, Items.EXPOSED_COPPER_BULB, Items.WAXED_EXPOSED_COPPER_BULB);
        generateWax(exporter, Items.EXPOSED_COPPER_DOOR, Items.WAXED_EXPOSED_COPPER_DOOR);
        generateWax(exporter, Items.EXPOSED_COPPER_TRAPDOOR, Items.WAXED_EXPOSED_COPPER_TRAPDOOR);
        generateWax(exporter, Items.EXPOSED_CHISELED_COPPER, Items.WAXED_EXPOSED_CHISELED_COPPER);
        generateWax(exporter, Items.EXPOSED_COPPER_GRATE, Items.WAXED_EXPOSED_COPPER_GRATE);
        generateWax(exporter, Items.WEATHERED_COPPER, Items.WAXED_WEATHERED_COPPER);
        generateWax(exporter, Items.WEATHERED_CUT_COPPER, Items.WAXED_WEATHERED_CUT_COPPER);
        generateWax(exporter, Items.WEATHERED_CUT_COPPER_SLAB, Items.WAXED_WEATHERED_CUT_COPPER_SLAB);
        generateWax(exporter, Items.WEATHERED_CUT_COPPER_STAIRS, Items.WAXED_WEATHERED_CUT_COPPER_STAIRS);
        generateWax(exporter, Items.WEATHERED_COPPER_BULB, Items.WAXED_WEATHERED_COPPER_BULB);
        generateWax(exporter, Items.WEATHERED_COPPER_DOOR, Items.WAXED_WEATHERED_COPPER_DOOR);
        generateWax(exporter, Items.WEATHERED_COPPER_TRAPDOOR, Items.WAXED_WEATHERED_COPPER_TRAPDOOR);
        generateWax(exporter, Items.WEATHERED_CHISELED_COPPER, Items.WAXED_WEATHERED_CHISELED_COPPER);
        generateWax(exporter, Items.WEATHERED_COPPER_GRATE, Items.WAXED_WEATHERED_COPPER_GRATE);
        generateWax(exporter, Items.OXIDIZED_COPPER, Items.WAXED_OXIDIZED_COPPER);
        generateWax(exporter, Items.OXIDIZED_CUT_COPPER, Items.WAXED_OXIDIZED_CUT_COPPER);
        generateWax(exporter, Items.OXIDIZED_CUT_COPPER_SLAB, Items.WAXED_OXIDIZED_CUT_COPPER_SLAB);
        generateWax(exporter, Items.OXIDIZED_CUT_COPPER_STAIRS, Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
        generateWax(exporter, Items.OXIDIZED_COPPER_BULB, Items.WAXED_OXIDIZED_COPPER_BULB);
        generateWax(exporter, Items.OXIDIZED_COPPER_DOOR, Items.WAXED_OXIDIZED_COPPER_DOOR);
        generateWax(exporter, Items.OXIDIZED_COPPER_TRAPDOOR, Items.WAXED_OXIDIZED_COPPER_TRAPDOOR);
        generateWax(exporter, Items.OXIDIZED_CHISELED_COPPER, Items.WAXED_OXIDIZED_CHISELED_COPPER);
        generateWax(exporter, Items.OXIDIZED_COPPER_GRATE, Items.WAXED_OXIDIZED_COPPER_GRATE);

        // misc recipes
        new MachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .addItemInput(Items.STONE, 1)
                .addItemOutput(Items.COBBLESTONE, 1)
                .offerTo(exporter, "vanilla_recipes/macerator/stone_to_cobblestone");
        new MachineRecipeBuilder(MIMachineRecipeTypes.COMPRESSOR, 2, 100)
                .addItemInput(Items.STRING, 4)
                .addItemOutput(Items.WHITE_WOOL, 1)
                .offerTo(exporter, "vanilla_recipes/compressor/string_to_wool");

        generateGrasslike(exporter, Ingredient.of(Items.WHEAT_SEEDS), Items.GRASS_BLOCK);
        generateGrasslike(exporter, Ingredient.of(Tags.Items.MUSHROOMS), Items.MYCELIUM);
        generateGrasslike(exporter, Ingredient.of(Items.SPRUCE_SAPLING), Items.PODZOL);
    }

    private void generateGrasslike(RecipeOutput exporter, Ingredient from, Item to) {
        new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 2, 320).level(MachineLevel.ALUMINUM)
                .addFluidInput(Fluids.WATER, 1000)
                .addItemInput(Items.DIRT, 1)
                .addItemInput(from, 1, 0)
                .addItemOutput(to, 1)
                .offerTo(exporter, "vanilla_recipes/chemical_reactor/grasslike/" + BuiltInRegistries.ITEM.getKey(to).getPath());
    }

    private void generateConcrete(RecipeOutput exporter) {
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName();
            new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100)
                    .addItemInput(Items.SAND, 4)
                    .addItemInput(Items.GRAVEL, 4)
                    .addItemInput("minecraft:" + name + "_dye", 1)
                    .addItemOutput("minecraft:" + name + "_concrete_powder", 8)
                    .offerTo(exporter, "vanilla_recipes/mixer/concrete/" + name + "_concrete_powder");

            new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100)
                    .addItemInput("minecraft:" + name + "_concrete_powder", 1)
                    .addFluidInput(Fluids.WATER, 1000)
                    .addItemOutput("minecraft:" + name + "_concrete", 1)
                    .offerTo(exporter, "vanilla_recipes/mixer/concrete/" + name + "_concrete");
        }
    }

    private void generateCoralPropagation(RecipeOutput exporter) {
        for (String coral : new String[]{"brain", "bubble", "fire", "horn", "tube"}) {
            new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 2, 6000).level(MachineLevel.ALUMINUM)
                    .addItemInput("minecraft:" + coral + "_coral_block", 1)
                    .addItemInput(Items.BONE_MEAL, 4)
                    .addItemInput("#c:dusts/salt", 4)
                    .addFluidInput(Fluids.WATER, 9000)
                    .addItemOutput("minecraft:" + coral + "_coral_block", 2)
                    .addItemOutput("minecraft:" + coral + "_coral", 2)
                    .addItemOutput("minecraft:" + coral + "_coral_fan", 2)
                    .addFluidOutput(Fluids.WATER, 6000)
                    .offerTo(exporter, "vanilla_recipes/chemical_reactor/" + coral + "_coral");
        }
    }

    private void generateDyeMixing(RecipeOutput exporter) {
        List<DyeMixRecipe> recipes = List.of(
                new DyeMixRecipe("black_dye", "black", 1, List.of("moderntech:coal_dust"), "minecraft:water", 50),
                new DyeMixRecipe("black_dye2", "black", 1, List.of("minecraft:ink_sac")),
                new DyeMixRecipe("black_dye3", "black", 1, List.of("minecraft:wither_rose")),
                new DyeMixRecipe("blue_dye2", "blue", 1, List.of("minecraft:cornflower")),
                new DyeMixRecipe("brown_dye", "brown", 1, List.of("minecraft:cocoa_beans")),
                new DyeMixRecipe("cyan_dye", "cyan", 2, List.of("minecraft:green_dye", "minecraft:blue_dye")),
                new DyeMixRecipe("gray_dye", "gray", 2, List.of("minecraft:black_dye", "minecraft:white_dye")),
                new DyeMixRecipe("light_blue_dye", "light_blue", 1, List.of("minecraft:blue_orchid")),
                new DyeMixRecipe("light_blue_dye2", "light_blue", 2, List.of("minecraft:blue_dye", "minecraft:white_dye")),
                new DyeMixRecipe("light_gray_dye1", "light_gray", 1, List.of("minecraft:azure_bluet")),
                new DyeMixRecipe("light_gray_dye2", "light_gray", 1, List.of("minecraft:oxeye_daisy")),
                new DyeMixRecipe("light_gray_dye3", "light_gray", 1, List.of("minecraft:white_tulip")),
                new DyeMixRecipe("light_gray_dye4", "light_gray", 2, List.of("minecraft:gray_dye", "minecraft:white_dye")),
                new DyeMixRecipe("light_gray_dye5", "light_gray", 3, List.of("minecraft:black_dye", "minecraft:white_dye*2")),
                new DyeMixRecipe("lime_dye", "lime", 2, List.of("minecraft:green_dye", "minecraft:white_dye")),
                new DyeMixRecipe("magenta_dye1", "magenta", 1, List.of("minecraft:allium")),
                new DyeMixRecipe("magenta_dye2", "magenta", 2, List.of("minecraft:lilac")),
                new DyeMixRecipe("magenta_dye3", "magenta", 2, List.of("minecraft:purple_dye", "minecraft:pink_dye")),
                new DyeMixRecipe("magenta_dye4", "magenta", 4, List.of("minecraft:blue_dye", "minecraft:red_dye*2", "minecraft:white_dye")),
                new DyeMixRecipe("magenta_dye5", "magenta", 3, List.of("minecraft:blue_dye", "minecraft:red_dye", "minecraft:pink_dye")),
                new DyeMixRecipe("orange_dye1", "orange", 1, List.of("minecraft:orange_tulip")),
                new DyeMixRecipe("orange_dye2", "orange", 2, List.of("minecraft:yellow_dye", "minecraft:red_dye")),
                new DyeMixRecipe("pink_dye", "pink", 2, List.of("minecraft:peony")),
                new DyeMixRecipe("pink_dye2", "pink", 1, List.of("minecraft:pink_tulip")),
                new DyeMixRecipe("pink_dye3", "pink", 2, List.of("minecraft:red_dye", "minecraft:white_dye")),
                new DyeMixRecipe("purple_dye", "purple", 2, List.of("minecraft:red_dye", "minecraft:blue_dye")),
                new DyeMixRecipe("red_dye1", "red", 1, List.of("minecraft:beetroot")),
                new DyeMixRecipe("red_dye2", "red", 1, List.of("minecraft:poppy")),
                new DyeMixRecipe("red_dye3", "red", 2, List.of("minecraft:rose_bush")),
                new DyeMixRecipe("red_dye4", "red", 1, List.of("minecraft:red_tulip")),
                new DyeMixRecipe("white_dye1", "white", 1, List.of("minecraft:bone_meal")),
                new DyeMixRecipe("white_dye2", "white", 1, List.of("minecraft:lily_of_the_valley")),
                new DyeMixRecipe("yellow_dye", "yellow", 2, List.of("minecraft:sunflower")),
                new DyeMixRecipe("yellow_dye2", "yellow", 1, List.of("minecraft:dandelion")));

        for (DyeMixRecipe recipe : recipes) {
            MachineRecipeBuilder builder = new MachineRecipeBuilder(MIMachineRecipeTypes.MIXER, 2, 100);
            for (String ingredient : recipe.ingredients()) {
                int separator = ingredient.lastIndexOf('*');
                String item = separator < 0 ? ingredient : ingredient.substring(0, separator);
                int amount = separator < 0 ? 1 : Integer.parseInt(ingredient.substring(separator + 1));
                builder.addItemInput(item, amount);
            }
            if (recipe.fluid() != null) {
                builder.addFluidInput(recipe.fluid(), recipe.fluidAmount());
            }
            builder.addItemOutput("minecraft:" + recipe.color() + "_dye", recipe.outputAmount())
                    .offerTo(exporter, "vanilla_recipes/mixer/dye/" + recipe.id());
        }
    }

    private record DyeMixRecipe(String id, String color, int outputAmount, List<String> ingredients, String fluid,
                                int fluidAmount) {
        private DyeMixRecipe(String id, String color, int outputAmount, List<String> ingredients) {
            this(id, color, outputAmount, ingredients, null, 0);
        }
    }

    private void generateCopperOxidation(RecipeOutput exporter, Item unaffected, Item exposed, Item weathered, Item oxidized) {
        oxidize(exporter, unaffected, exposed);
        oxidize(exporter, exposed, weathered);
        oxidize(exporter, weathered, oxidized);
    }

    private void oxidize(RecipeOutput exporter, Item from, Item to) {
        new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100).level(MachineLevel.ALUMINUM)
                .addItemInput(from, 1)
                .addFluidInput(MIFluids.OXYGEN, 100)
                .addItemOutput(to, 1)
                .offerTo(exporter, "vanilla_recipes/oxidation/" + BuiltInRegistries.ITEM.getKey(from).getPath());
    }

    private void generateWax(RecipeOutput exporter, Item from, Item to) {
        var recipe = ShapelessRecipeBuilder.shapeless(to).requires(from).requires(MIItem.WAX);
        recipe.offerTo(exporter, "vanilla_recipes/waxing/" + BuiltInRegistries.ITEM.getKey(from).getPath());

        MachineRecipeBuilder chemicalReactorRecipe = new MachineRecipeBuilder(MIMachineRecipeTypes.CHEMICAL_REACTOR, 8, 100).level(MachineLevel.ALUMINUM)
                .addItemInput(from, 1)
                .addItemInput(MIItem.WAX, 1)
                .addItemOutput(to, 1);

        chemicalReactorRecipe.offerTo(exporter, "vanilla_recipes/chemical_reactor/waxing/" + BuiltInRegistries.ITEM.getKey(from).getPath());
    }
}
