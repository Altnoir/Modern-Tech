---
navigation:
  title: "电力"
  icon: "moderntech:electric_steam_turbine"
  position: 104
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_steam_turbine
  - moderntech:electric_diesel_generator
---

# 电力

电力蒸汽涡轮机使用蒸汽产生 FE。每 1 mB 蒸汽转换为 1 FE，每刻最多转换 32 mB。

<Recipe id="moderntech:processing_table/electric_steam_turbine" />

电力蒸汽涡轮机会自动向紧贴其输出面放置的机器输送 FE，内部缓存可容纳 3200 FE。

电力柴油发电机是电力蒸汽涡轮机的替代选择。它使用多种燃料发电。目前你可以燃烧杂酚油。（可用燃料列表请查阅 REI）。

<Recipe id="moderntech:processing_table/electric_diesel_generator" />

## ModernTech 中的电力

ModernTech 使用标准的 NeoForge 能源接口，可通过其他模组的电缆、能源管道或储能方块输入 FE。

这意味着：

- 直接用其它模组的电缆或管道给机器供电，或者把发电机紧挨着机器放置。
- 再也不需要匹配电压：你不必仅仅为了在等级之间传输电力而升级机器外壳，也不必让电力经过变压器。
- 电力、铝合金、不锈钢、钛和铱是机器等级，分别决定固定速度倍率与配方加工要求。外壳另行决定能源缓存，缓存满后停止接收 FE。

电力单方块加工机器可从各面接收 FE；发电机仅在设定的输出面提供 FE 输出。

机器界面可查看完整的大容量缓存。标准 FE 接口最多报告 2,147,483,647 FE，部分外部设备显示的数值可能较低。
