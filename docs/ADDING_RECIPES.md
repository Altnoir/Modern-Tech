# Recipe format and KubeJS integration
In general, a machine recipe JSON in MI has the following properties:
- `energy`: Reference FE/t before the machine's speed multiplier.
- `duration`: Reference processing time in ticks; total recipe energy is `energy * duration`.
- `level`: The minimum machine level; omitted JSON levels default to `bronze`.
- `item_inputs`: The list of required items. Set `"reusable": true` on an input that must remain in the machine; omit its `probability`.
- `item_outputs`: The list of items it produces.
- `fluid_inputs`: The list of fluids it consumes.
- `fluid_outputs`: The list of fluids it produces.
- `process_conditions`: A list of additional conditions for the recipe to be processed by the machine.

You can find plenty of examples for the JSON format in [our files](../src/generated/resources/data/moderntech/recipe).
For example, the assembler recipe for the trash can looks [like this](../src/generated/resources/data/moderntech/recipe/trash_can_assembler.json).

## Built-in recipe datagen

Built-in static recipes are defined in Java under `aztech.milite.datagen.recipe` and generated into
`src/generated/resources/data/moderntech/recipe`. Run `gradlew runData` after editing their definitions and include the
updated generated files with the change. Do not edit generated JSON directly.

The fixed providers group recipes by materials, electric age, steam age, oil, quarry, fusion, tools, vanilla recipes,
and miscellaneous recipes. They are registered in `MIDatagenServer` alongside the existing material and other bulk
generators. Each recipe keeps its explicit path, so moving its definition between providers does not change its ID.

Specify the required machine level explicitly when adding a recipe:

```java
new MachineRecipeBuilder(MIMachineRecipeTypes.ASSEMBLER, 8, 200)
        .level("bronze")
        .addItemInput("minecraft:chest", 1)
        .addItemOutput("moderntech:trash_can", 1)
        .offerTo(consumer, "example/trash_can");
```

The string overload also accepts `steel`, `electric`, `aluminum_alloy`, `stainless_steel`, `titanium`, and `iridium`.
It uses `MachineLevel.byName`, including its `aluminum` alias and case-insensitive lookup, and rejects unknown names.
The enum overload `.level(MachineLevel.BRONZE)` remains available. The builder still derives its default level from
energy, so the migrated recipes explicitly specify their original level instead of relying on that default.

`FixedRecipesProvider.shaped` emits the shaped recipe and, for a path ending in `_asbl`, its assembler counterpart
from the same definition. The counterpart retains the ID `assembler_generated/<path without _asbl>`, 8 FE/t,
200 ticks, electric level, and the exact ingredient and result quantities. It does not read another provider's output
or scan source files, so generation works both during development and with runtime datagen.

`ConditionalMachineRecipesProvider` uses `ConditionalMachineRecipeBuilder` for recipes that reference optional mods,
including conditional outputs inside otherwise unconditional quarry recipes. This builder writes item IDs and
NeoForge conditions without looking up optional items or evaluating conditions at generation time. For example:

```java
new ConditionalMachineRecipeBuilder(MIMachineRecipeTypes.QUARRY, 4, 600)
        .level("bronze")
        .addReusableItemInput("moderntech:bronze_drill", 1)
        .addItemOutput("minecraft:iron_ore", 1, 0.4f)
        .addItemOutput("mekanism:osmium_ore", 1, 0.12f, new ModLoadedCondition("mekanism"))
        .offerTo(consumer, "example/conditional_quarry");
```

For conditions on an entire typed recipe, use `consumer.withConditions(...)`. Conditions on individual optional
outputs must remain in JSON until the game loads recipes; use the conditional builder for those.

Runtime conversions of smelting, stonecutting, and compostable items remain in `RecipeConversions`, so recipes added
by other mods or data packs continue to participate. They should not be frozen into the built-in datagen output.

After generation, run `gradlew runGameTestServer` to check that `_asbl` counterparts are loaded with matching
ingredients, quantities, energy, duration, and level, and that migrated recipes preserve their legacy default level.
Built-in machine levels are also checked for reachability, including multiblocks. Enable `validateRecipeLevels` in
the startup config for a log-only check of custom recipes. Custom machine registrations should call
`MachineRegistrationHelper.recordMachineLevel(recipeType, highestLevel)` so this check can include them.

MI adds a nicer syntax when the recipes are being registered by KubeJS:
- Create the recipe with the reference FE/t and duration as the two constructor arguments.
- Use `.level("electric")` to set its minimum machine level. Names and aliases match datagen; omitted levels remain `bronze`.
- The energy key is `energy`. Either `.energy(...)` or `.fe(...)` sets the reference FE per tick.
- Then you can add inputs and outputs by calling the following functions. The can be called multiple times of course:
  - `itemIn(input specification)`: Add a new input.
  - `itemInReusable(input specification)`: Require an input without consuming it, such as a quarry drill.
  - `itemIn(input specification, consumption chance)`: Add a new chanced input. Chance can be `0` if the input should not be consumable.
  - `itemOut(output specification)` and `itemOut(output specification, production chance)`: Add a new output.
  - `fluidIn("1000x moderntech:oxygen")` and `fluidIn("1000x moderntech:oxygen", consumption chance)`: Add a fluid input in millibuckets. Chance can be `0` for a catalyst.
  - `fluidOut("1000x moderntech:creosote")` and `fluidOut("1000x moderntech:creosote", production chance)`: Add a fluid output in millibuckets.

Here is an example to get you started:
```js
ServerEvents.recipes(event => {
    // start a new recipe for the blast furnace, with 128 FE/t and a duration of 100 ticks
    event.recipes.moderntech.blast_furnace(128, 100)
        .level("electric")
        // add all the inputs and outputs:
        .itemIn("64x #minecraft:logs_that_burn")
        .itemOut("64x minecraft:charcoal")
        .fluidIn("1000x moderntech:oxygen")
        .fluidOut("5000x moderntech:creosote")
})
```

## Process conditions
The easiest way to add process conditions is via KubeJS, similarly to how inputs and outputs are added.
Here is the list of currently supported conditions:
- `dimension(dimension key)`: Requires the machine to be in the specified dimension.
- `biome(biome key)`: Requires the machine to be in the specified biome.
- `biomeTag(biome tag key)`: Requires the machine to be in a biome of the specified tag.
- `adjacentBlock(block, position)`: Requires a specific block to be next to the machine.
  - Position indicates where the block should be.
  - For multiblocks, the position is always relative to the controller.
  - For now, the only supported positions are `"below"` and `"behind"`.

Here is an example that removes the default bronze drill quarry recipe, and adds one that requires the machine to be in the overworld and right above bedrock:
```js
ServerEvents.recipes(event => {
    event.remove({id: "moderntech:quarry/bronze"})
    event.recipes.moderntech.quarry(4, 600)
        .itemInReusable("moderntech:bronze_drill")
        .itemOut("minecraft:iron_ore", 0.4)
        .itemOut("minecraft:coal_ore", 0.4)
        .itemOut("minecraft:copper_ore", 0.2)
        .itemOut("moderntech:tin_ore", 0.3)
        .itemOut("minecraft:gold_ore", 0.15)
        .itemOut("minecraft:redstone_ore", 0.2)
        .dimension("overworld")
        .adjacentBlock("minecraft:bedrock", "below")
})
```

## Custom process conditions
You can also add your own process conditions by writing KubeJS checks directly, they can be slow so don't use too many of them.

First, we need to register the condition and its description using the `MIRecipeEvents.customConditions` event.
The description is mandatory and will be used in the display of the recipe in REI.
Then, we can use it in the recipe. Here is a full example:

```js
// Add "odd_x_pos" condition that requires the machine to be in an odd X position
MIRecipeEvents.customCondition(event => {
    event.register("odd_x_pos", // ID of the condition
            // condition itself, receives the machine context and the recipe that is being checked
            (context, recipe) => {
                return context.getBlockEntity().getBlockPos().getX() % 2 !== 0;
            },
            // description for REI-like mods
            Text.of("Must be placed on an odd X position"));
});

// Add recipe that uses the condition, see above
ServerEvents.recipes(event => {
    event.recipes.moderntech.compressor(2, 200)
        .itemIn("dirt")
        .itemOut("diamond")
        // Use custom condition defined above
        .customCondition("odd_x_pos")
});
```

## Registered process conditions
If a process condition doesn't have direct KubeJS support (like from an addon mod), you can manually write its parameters using `registeredCondition(JsonObject)`:

```js
// Manually adding a dimension condition - of course, this is just an example and you don't have a reason to actually do it this way.
ServerEvents.recipes(event => {
    event.recipes.moderntech.compressor(2, 200)
        .itemIn("dirt")
        .itemOut("diamond")
        .registeredCondition({
          "moderntech:dimension": {
            "dimension": "minecraft:the_nether"
          }
        })
});
```

## Adding multiblock slots
Multiblock machines always have an unlimited number of input and output slots
(provided the recipe type allows the relevant input/output types).
However, only a limited number of slots is shown in EMI/JEI/REI.

MI adds an event that you can use to add slots to multiblock machines.
The event is called `MIMachineEvents.addMultiblockSlots` **and must be in a startup script**.

The event registration needs a parameter which is the identifier of the category in EMI/JEI/REI.
In the event, you can use `event.<type of slots>.addSlot(slot x position, slot y position)` to add a slot.
The `<type of slots>` can be one of the following:
- `itemInputs`,
- `itemOutputs`,
- `fluidInputs`,
- `fluidOutputs`.

Here is an example to add a fluid output slot to the steam blast furnace and to the electric blast furnace:
```js
MIMachineEvents.addMultiblockSlots("steam_blast_furnace", event => {
    event.fluidOutputs.addSlot(122, 53);
})
MIMachineEvents.addMultiblockSlots("electric_blast_furnace", event => {
    event.fluidOutputs.addSlot(122, 53);
})
```

The category ID can be found in various ways, for example by inspecting the `en_us.json` lang file:
```json
{
  "rei_categories.moderntech.electric_blast_furnace": "Electric Blast Furnace"
}
```
From this we deduce that the category is `electric_blast_furnace`.

All three electric blast furnaces share this single category: they only differ by the level of the recipes they can run, which is a property of the recipe, so a category per furnace would only show the same list three times. A new blast furnace added through `MIMachineEvents.addEbfTiers` becomes a workstation of the same category.
