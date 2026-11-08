---
navigation:
  title: "Oil Processing"
  icon: "moderntech:diesel_bucket"
  position: 109
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:oil_drilling_rig
  - moderntech:aluminum_alloy_diesel_generator
---

# Oil Processing

<GameScene zoom="1.5" interactive={true} fullWidth={true}>
    <MultiblockShape controller="oil_drilling_rig" />
</GameScene>

The Oil Drilling Rig is a huge multiblock that can dig for Crude Oil under the bedrock using drills. Yes, it's an oil quarry basically. Oil processing will give you a ton of byproducts and energy!

<Recipe id="moderntech:processing_table/oil_drilling_rig" />

The structure of the multiblock is made of: Aluminum Alloy Machine Casings, Steel Pipe Machine Casings and Chains as you can see by placing the controller and holding a Wrench. The Hatches can be replaced by Aluminum Alloy Machine Casings, but be sure to have at least Item Input, Fluid Output and Energy Input Hatches!

Crude Oil can be turned into various fuels and used for more efficient Rubber Sheet production.

Various fuels can be burned in the Diesel Generator, and you can check in REI how many RF each fuel produces. The RF produced by these fuels in the Diesel Generator is doubled. The Diesel Generator will produce up to 256 RF/t.

<Recipe id="moderntech:processing_table/aluminum_alloy_diesel_generator" />

Diesel Generators will only use fuel when they need to, but if you need a constant supply of energy, you can put liquid fuels in the Large Steam Boiler instead of items.

A fully heated Large Steam Boiler will roughly produce half the amount of energy a Diesel Generator would produce using the same quantity of fuel.

