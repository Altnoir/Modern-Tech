---
navigation:
  title: "Stainless Steel"
  icon: "moderntech:stainless_steel_dust"
  position: 201
  parent: moderntech:midgame.md
item_ids:
  - moderntech:vacuum_freezer
  - moderntech:distillation_tower
---

# Stainless Steel

## Stainless Steel

<ItemImage id="moderntech:stainless_steel_ingot" />

Stainless Steel is the next material you will want a large quantity of. Read on to know why!

Smelting stainless steel dust in an EBF will give you hot ingots, which you can cool back into regular ingots with a Vacuum Freezer.

## Vacuum Freezer

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="vacuum_freezer" />
</GameScene>

You can check the base materials with REI, and the exact shape with a wrench!

<Recipe id="moderntech:processing_table/vacuum_freezer" />

## Distillation Tower

Once you can make Digital Circuits, consider building a Distillation Tower. A Distillery produces one oil-processing output, while the tower produces one per layer.

<Recipe id="moderntech:processing_table/distillation_tower" />

Here is an example of the smallest size and the largest size side by side.
A tower of size 2 will only give you the first output from the recipe, a tower of size 3 will give the first two outputs, and so on...


<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="distillation_tower" />
    <MultiblockShape controller="distillation_tower" x="-6" useBigShape={true} />
</GameScene>

