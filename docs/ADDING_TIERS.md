# Adding machine tiers
A casing tier provides machine models and energy-buffer sizing. Recipe eligibility and fixed speed are controlled
separately by `MachineLevel`.
Tiers are not voltages: MI Lite ships no cables and no transformers,
machines simply expose NeoForge's standard energy capability.

Registering a tier also registers the [machine casing](ADDING_MACHINES.md#adding-new-casing-types) that machines of this tier use,
so you will need to provide the corresponding casing textures.

Use the `MIRegistrationEvents.registerEnergyTiers` startup event to register an energy tier.
```js
MIRegistrationEvents.registerEnergyTiers(event => {
    event.register(
        // Internal name for the tier, and the name of its machine casing.
        "custom_alloy",
        // English name for the tier.
        "Custom Alloy",
        // Base value used to size energy buffers.
        65536,
        // Resource location of the machine hull that upgrades a machine to this tier.
        "mypack:custom_alloy_machine_hull",
    );
});
```

The tier name, its reference capacity in FE and its machine hull must all be unique.

Crafting-machine buffers use the casing tier's value multiplied by 100 and the machine's configured speed factor,
with a cap based on the highest registered tier. Storage blocks and energy hatches define their capacity separately.
Registering a casing tier does not add a recipe level or change a machine's fixed speed multiplier.

Use `event.get("aluminum_alloy").baseCapacity` to read the reference capacity of an already registered tier.

Energy input and output hatches are not registered automatically for a new tier:
see the page on [adding hatches](ADDING_HATCHES.md).
