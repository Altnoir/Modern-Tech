---
navigation:
  title: "Великий паровий котел"
  icon: "moderntech:large_steam_boiler"
  position: 103
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:large_steam_boiler
  - moderntech:bronze_plated_bricks
  - moderntech:heatproof_machine_casing
---

# Великий паровий котел

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_steam_boiler" />
</GameScene>

Великий паровий котел є вдосконаленою версією бронзових і сталевих котлів. Він використовує паливо у 8 разів швидше, ніж піч, але виробляє 256 мВ/т пари при повному нагріванні.

<Recipe id="moderntech:processing_table/large_steam_boiler" />

На відміну від одноблокового аналога, у великому паровому котлі буде втрачено 80% будь-якого неспожитого тепла. Це означає, що відношення енергії, виробленої до спожитого палива, різко зменшиться, коли вихідна потужність зменшиться нижче максимального. Це станеться, якщо котел не використовується постійно на максимальній потужності.

Основним блоком, який вам знадобиться, є бронзована цегла, але вам також знадобляться бронзові машинні обшивки для труб та термостійкі машинні обшивки.

<Recipe id="moderntech:electric_age/casing/bronze_plated_bricks_asbl" />

<Recipe id="moderntech:electric_age/casing/heatproof_machine_casing_asbl" />

Великий паровий котел складається з одного шару термостійких машинних обшивок та трьох шарів бронзованої цегли.

Контролер працює на другому шарі (тобто перший шар з бронзованої цегли).

Два середні блоки це бронзові машинні обшивки для труб.

Шлюзи повинні бути розміщені на нижньому шарі.

