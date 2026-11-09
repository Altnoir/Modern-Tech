package aztech.milite.datagen.recipe;

import aztech.milite.MI;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.init.MachineRegistrationHelper;
import aztech.milite.machines.recipe.MachineRecipeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Writes conditional machine recipes without resolving optional mods' items during datagen. In particular, conditions
 * on individual outputs must reach the game's conditional codec intact rather than being evaluated while generating.
 */
final class ConditionalMachineRecipeBuilder {
    private final MachineRecipeType type;
    private final JsonObject recipe = new JsonObject();
    private final JsonArray inputs = new JsonArray();
    private final JsonArray outputs = new JsonArray();

    ConditionalMachineRecipeBuilder(MachineRecipeType type, int energy, int duration) {
        if (energy <= 0 || duration <= 0) {
            throw new IllegalArgumentException("Energy and duration must be positive");
        }
        this.type = type;
        recipe.addProperty("type", type.getId().toString());
        recipe.addProperty("energy", energy);
        recipe.addProperty("duration", duration);
        recipe.add("item_inputs", inputs);
        recipe.add("item_outputs", outputs);
    }

    ConditionalMachineRecipeBuilder level(String level) {
        var required = MachineRegistrationHelper.clampBuiltInRecipeLevel(type, MachineLevel.byName(level));
        recipe.addProperty("level", required.getSerializedName());
        return this;
    }

    ConditionalMachineRecipeBuilder withConditions(ICondition... conditions) {
        ICondition.writeConditions(JsonOps.INSTANCE, recipe, List.of(conditions));
        return this;
    }

    ConditionalMachineRecipeBuilder addItemInput(String itemOrTag, int amount, float probability) {
        inputs.add(entry(itemOrTag.startsWith("#") ? "tag" : "item",
                itemOrTag.startsWith("#") ? itemOrTag.substring(1) : itemOrTag, amount, probability));
        return this;
    }

    ConditionalMachineRecipeBuilder addReusableItemInput(String itemOrTag, int amount) {
        var input = entry(itemOrTag.startsWith("#") ? "tag" : "item",
                itemOrTag.startsWith("#") ? itemOrTag.substring(1) : itemOrTag, amount, 1);
        input.addProperty("reusable", true);
        inputs.add(input);
        return this;
    }

    ConditionalMachineRecipeBuilder addItemOutput(String item, int amount, float probability, ICondition... conditions) {
        var output = entry("item", item, amount, probability);
        if (conditions.length != 0) {
            ICondition.writeConditions(JsonOps.INSTANCE, output, List.of(conditions));
        }
        outputs.add(output);
        return this;
    }

    private static JsonObject entry(String key, String id, int amount, float probability) {
        if (amount <= 0 || !Float.isFinite(probability) || probability < 0 || probability > 1) {
            throw new IllegalArgumentException("Invalid ingredient amount or probability");
        }
        var entry = new JsonObject();
        entry.addProperty(key, ResourceLocation.parse(id).toString());
        entry.addProperty("amount", amount);
        if (probability != 1) {
            entry.addProperty("probability", probability);
        }
        return entry;
    }

    void offerTo(BiConsumer<ResourceLocation, JsonObject> output, String path) {
        if (!recipe.has("level") || inputs.isEmpty() || outputs.isEmpty()) {
            throw new IllegalStateException("Conditional recipes must specify a level, inputs, and outputs: " + path);
        }
        output.accept(MI.id(path), recipe.deepCopy());
    }
}
