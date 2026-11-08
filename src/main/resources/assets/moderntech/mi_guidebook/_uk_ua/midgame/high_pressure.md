---
navigation:
  title: "Високий тиск!"
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

# Високий тиск!

## Ущільнювач

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="pressurizer" />
</GameScene>

Ущільнювач — це новий багатоблок із титану, який може перетворювати воду на високотисну воду, пару — на високотисну пару та навпаки.

<Recipe id="moderntech:processing_table/pressurizer" />

## Високотискний великий паровий котел

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_large_steam_boiler" />
</GameScene>

Отримавши доступ до високотискної води, ви можете використовувати вискотискний великий паровий котел для виробництва високотискного пару.

Одне мВ високотискного пару коштує 8 мВ звичайного пару, тобто 8 RF.

<Recipe id="moderntech:processing_table/high_pressure_large_steam_boiler" />

## Покращений високотискний паровий котел

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="high_pressure_advanced_large_steam_boiler" />
</GameScene>

Пізніше ви також зможете створити покращену версію високотискного великого парового котла.

## Велика парова турбіна

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_steam_turbine" />
</GameScene>

Велика парова турбіна прийматиме вRF як звичайну пару (1 мВ = 1 RF), так високотискний пар (1 мВ = 8 RF) і генеруватиме до 16384 RF/т! **Однак це не поверне вам звичайну воду чи високотисну воду**.

<Recipe id="moderntech:processing_table/large_steam_turbine" />

## Heat Exchanger

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="heat_exchanger" />
</GameScene>

Пам'ятайте, що стискання води у високотискну воду вимагає багато енергії, але турбіна її не дасть! Ви можете використовувати теплообмінник для відновлення високотискної води для іншого циклу.

<Recipe id="moderntech:processing_table/heat_exchanger" />

## Парова турбіна з нержавіючої сталі

Іншим варіантом для невеликих установок є використання парової турбіни з нержавіючої сталі. Як і інші генератори цього рівня, він вироблятиме 512 RF/т. Він приймає лише звичайний пар.

<Recipe id="moderntech:processing_table/stainless_steel_steam_turbine" />

