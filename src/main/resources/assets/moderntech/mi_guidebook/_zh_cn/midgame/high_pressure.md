---
navigation:
  title: "高压！"
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

# 高压！

## 增压器

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="pressurizer" />
</GameScene>

增压器是一种由钛制成的新型多方块结构，可以将水变成高压水，将蒸汽变成高压蒸汽，以此类推。

<Recipe id="moderntech:processing_table/pressurizer" />

## 高压大型蒸汽锅炉

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_large_steam_boiler" />
</GameScene>

一旦能够制造高压水，你就可以使用高压大型蒸汽锅炉来生产高压蒸汽。

1 mB高压蒸汽相当于8 mB普通蒸汽，等价于8 FE。

<Recipe id="moderntech:processing_table/high_pressure_large_steam_boiler" />

## 高压进阶大型蒸汽锅炉

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_advanced_large_steam_boiler" />
</GameScene>

之后，你还可以建造进阶版的高压大型蒸汽锅炉。

## 大型蒸汽涡轮机

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_steam_turbine" />
</GameScene>

大型蒸汽涡轮机可将常规蒸汽（1 mB = 1 FE）和高压蒸汽（1 mB = 8 FE）转化为FE，且产出最高可达16384 FE/t！**然而它既不会返还普通水，也不会返还高压水。**

<Recipe id="moderntech:processing_table/large_steam_turbine" />

## 热交换机

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="heat_exchanger" />
</GameScene>

请记住，将水加压成高压水会消耗大量能量，但蒸汽涡轮机不会将其返还！你可以使用热交换机回收高压水来用于另一个循环。

<Recipe id="moderntech:processing_table/heat_exchanger" />

## 不锈钢蒸汽涡轮机

小型设施的另一种选择是使用不锈钢蒸汽涡轮机。与其他同等级的发电机一样，它能产出512 FE/t。它只接受普通蒸汽。

<Recipe id="moderntech:processing_table/stainless_steel_steam_turbine" />

