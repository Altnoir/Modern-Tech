package aztech.milite.datagen.recipe;

import aztech.milite.machines.init.MIMachineRecipeTypes;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/** Conditional recipes and outputs stay in JSON until the game evaluates their mod-loading conditions. */
public class ConditionalMachineRecipesProvider implements DataProvider {
    private final PackOutput.PathProvider recipes;

    public ConditionalMachineRecipesProvider(PackOutput output) {
        recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> definitions = new LinkedHashMap<>();
        buildRecipes((id, recipe) -> {
            if (definitions.putIfAbsent(id, recipe) != null) {
                throw new IllegalStateException("Duplicate conditional recipe " + id);
            }
        });
        return CompletableFuture.allOf(definitions.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), recipes.json(entry.getKey())))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    private static void buildRecipes(BiConsumer<ResourceLocation, JsonObject> consumer) {
        addAe2Lithography(consumer, "calculation", "ae2:certus_quartz_crystal");
        addAe2Lithography(consumer, "engineering", "#c:gems/diamond");
        addAe2Lithography(consumer, "logic", "#c:ingots/gold");

        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.QUARRY, 128, 600)
                .level("bronze")
                .withConditions(new ModLoadedCondition("create"))
                .addReusableItemInput("create:mechanical_arm", 1)
                .addItemOutput("create:asurine", 1, 0.5f)
                .addItemOutput("create:crimsite", 1, 0.5f)
                .addItemOutput("create:ochrum", 1, 0.5f)
                .addItemOutput("create:veridium", 1, 0.5f)
                .offerTo(consumer, "compat/create/quarry/mechanical_arm");

        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .level("bronze")
                .withConditions(new ModLoadedCondition("mekanism"))
                .addItemInput("#c:ingots/osmium", 1, 1.0f)
                .addItemOutput("mekanism:dust_osmium", 1, 1.0f)
                .offerTo(consumer, "compat/mekanism/osmium_ingot_to_dust");

        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 100)
                .level("bronze")
                .withConditions(new ModLoadedCondition("mekanism"))
                .addItemInput("#c:ores/osmium", 1, 1.0f)
                .addItemOutput("mekanism:raw_osmium", 3, 1.0f)
                .offerTo(consumer, "compat/mekanism/osmium_ore_to_raw");

        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.QUARRY, 128, 600)
                .level("bronze")
                .withConditions(new ModLoadedCondition("mekanism"))
                .addReusableItemInput("mekanism:digital_miner", 1)
                .addItemOutput("mekanism:osmium_ore", 1, 0.5f)
                .addItemOutput("minecraft:diamond_ore", 1, 0.25f)
                .addItemOutput("moderntech:lead_ore", 1, 0.5f)
                .addItemOutput("mekanism:fluorite_ore", 1, 0.4f)
                .addItemOutput("moderntech:uranium_ore", 1, 0.2f)
                .offerTo(consumer, "compat/mekanism/quarry/digital_miner");

        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.MACERATOR, 2, 50)
                .level("bronze")
                .withConditions(new ModLoadedCondition("mekanism"))
                .addItemInput("#c:raw_materials/osmium", 1, 1.0f)
                .addItemOutput("mekanism:dust_osmium", 1, 1.0f)
                .addItemOutput("mekanism:dust_osmium", 1, 0.5f)
                .offerTo(consumer, "compat/mekanism/raw_osmium_to_dust");

        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.QUARRY, 4, 600)
                .level("bronze")
                .addReusableItemInput("moderntech:bronze_drill", 1)
                .addItemOutput("minecraft:iron_ore", 1, 0.4f)
                .addItemOutput("minecraft:coal_ore", 1, 0.4f)
                .addItemOutput("moderntech:lignite_coal_ore", 1, 0.24f)
                .addItemOutput("minecraft:copper_ore", 1, 0.2f)
                .addItemOutput("moderntech:tin_ore", 1, 0.3f)
                .addItemOutput("minecraft:gold_ore", 1, 0.15f)
                .addItemOutput("minecraft:redstone_ore", 1, 0.2f)
                .addItemOutput("create:zinc_ore", 1, 0.08f, new ModLoadedCondition("create"))
                .addItemOutput("mekanism:osmium_ore", 1, 0.12f, new ModLoadedCondition("mekanism"))
                .offerTo(consumer, "quarry/bronze");

        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.QUARRY, 12, 600)
                .level("bronze")
                .addReusableItemInput("moderntech:steel_drill", 1)
                .addItemOutput("moderntech:antimony_ore", 1, 0.2f)
                .addItemOutput("minecraft:diamond_ore", 1, 0.12f)
                .addItemOutput("minecraft:lapis_ore", 1, 0.1f)
                .addItemOutput("moderntech:lead_ore", 1, 0.25f)
                .addItemOutput("moderntech:nickel_ore", 1, 0.18f)
                .addItemOutput("moderntech:bauxite_ore", 1, 0.4f)
                .addItemOutput("moderntech:salt_ore", 1, 0.12f)
                .addItemOutput("minecraft:emerald_ore", 1, 0.1f)
                .addItemOutput("moderntech:quartz_ore", 1, 0.2f)
                .addItemOutput("powah:uraninite_ore", 1, 0.08f, new ModLoadedCondition("powah"))
                .offerTo(consumer, "quarry/steel");

        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.QUARRY, 128, 600)
                .level("bronze")
                .addReusableItemInput("moderntech:titanium_drill", 1)
                .addItemOutput("moderntech:uranium_ore", 1, 0.2f)
                .addItemOutput("moderntech:iridium_ore", 1, 0.05f)
                .addItemOutput("mekanism:fluorite_ore", 1, 0.3f, new ModLoadedCondition("mekanism"))
                .offerTo(consumer, "quarry/titanium");
    }

    private static void addAe2Lithography(BiConsumer<ResourceLocation, JsonObject> consumer, String kind, String material) {
        new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.LITHOGRAPHY, 4, 200)
                .level("stainless_steel")
                .withConditions(new ModLoadedCondition("ae2"))
                .addReusableItemInput("ae2:" + kind + "_processor_press", 1)
                .addItemInput(material, 1, 1.0f)
                .addItemOutput("ae2:printed_" + kind + "_processor", 1, 1.0f)
                .offerTo(consumer, "compat/ae2/lithography/printed_" + kind + "_processor");
    }
}
