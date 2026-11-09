---
navigation:
  title: "High Pressure!"
  icon: "moderntech:pressurizer"
  position: 202
  parent: moderntech:midgame.md
item_ids:
  - moderntech:pressurizer
  - moderntech:high_pressure_large_steam_boiler
  - moderntech:high_pressure_advanced_large_steam_boiler
  - moderntech:large_steam_turbine
  - moderntech:heat_exchanger
  - moderntech:stainless_steel_steam_turbine
---

# High Pressure!

## Pressurizer

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="pressurizer" />
</GameScene>

The Pressurizer is a new multiblock made of titanium that can turn water into HP (high pressure) water, steam into HP steam, and the other way around.

<Recipe id="moderntech:processing_table/pressurizer" />

## HP Large Steam Boiler

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_large_steam_boiler" />
</GameScene>

Once you have access to HP water, you can use a High Pressure Large Steam Boiler to produce HP steam.

One millibucket of HP steam is worth 8 mb of regular steam, that is 8 FE.

<Recipe id="moderntech:processing_table/high_pressure_large_steam_boiler" />

## HP Advanced Large Steam Boiler

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_advanced_large_steam_boiler" />
</GameScene>

Later on, you will also be able to build the advanced version of the HP Large Steam Boiler.

## Large Steam Turbine

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_steam_turbine" />
</GameScene>

A Large Steam Turbine will accept both regular steam (1 mb = 1 FE) and HP steam (1 mb = 8 FE) into FE, and will generate up to 16384 FE/t! **However it will not give you back regular nor HP water.**

<Recipe id="moderntech:processing_table/large_steam_turbine" />

## Heat Exchanger

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="heat_exchanger" />
</GameScene>

Remember that pressurizing water into HP water costs a lot of energy, but the turbine will not give it back! You can use a Heat Exchanger to recover the HP water for another cycle.

<Recipe id="moderntech:processing_table/heat_exchanger" />

## Stainless Steel Steam Turbine

Another option for small setups is to use a Stainless Steel Steam Turbine. Like other generators of that tier, it will produce 512 FE/t. This one will only accept regular steam.

<Recipe id="moderntech:processing_table/stainless_steel_steam_turbine" />

