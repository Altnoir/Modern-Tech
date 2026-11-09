# Adding hatches
To add new energy, fluid, or item hatches, use the `MIMachineEvents.registerHatches` startup event.

## Energy
Use the `energy` registration method to register energy input and output hatches for the given tier.
For example:
```js
MIMachineEvents.registerHatches(event => {
    // Takes the internal name of the tier.
    event.energy("custom_alloy");
});
```

Register the custom tier first. The generated hatch model uses that tier's casing, and the hatch buffer holds 4,800 times its reference capacity in FE.

See also [Adding machine tiers](ADDING_TIERS.md) for more information on how to add a tier.

## Fluid
Use the `fluid` registration method to register fluid input and output hatches.
These hatches are not bound to a machine tier.

Here is an example:
```js
MIMachineEvents.registerHatches(event => {
    event.fluid(
        // English name for the hatch tier. " Fluid Input/Output Hatch" will be added by MI.
        "Huge",
        // Internal name for the tier.
        // In this cases, the block IDs of the hatches will be huge_fluid_input_hatch and huge_fluid_output_hatch.
        "huge",
        // Machine casing for the model of the hatch.
        "iridium",
        // Number of buckets that the hatch can hold.
        1000,
    );
});
```

## Item
Use the `item` registration method to register item input and output hatches.
These hatches are not bound to a machine tier.

Here is an example:
```js
MIMachineEvents.registerHatches(event => {
    event.item(
        // English name for the hatch tier. " Item Input/Output Hatch" will be added by MI.
        "Huge",
        // Internal name for the tier.
        // In this cases, the block IDs of the hatches will be huge_item_input_hatch and huge_item_output_hatch.
        "huge",
        // Machine casing for the model of the hatch.
        "iridium",
        // Number of slot rows and columns for the hatch slots:
        3, 7, // 3 rows and 7 columns here
        // Starting position of the slot grid:
        10, 18, // the first slot will be at (10, 18) inside the GUI
    );
});
```
