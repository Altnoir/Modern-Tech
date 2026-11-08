---
navigation:
  title: "大型蒸汽锅炉"
  icon: "moderntech:large_steam_boiler"
  position: 103
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:large_steam_boiler
  - moderntech:bronze_plated_bricks
  - moderntech:heatproof_machine_casing
---

# 大型蒸汽锅炉

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="large_steam_boiler" />
</GameScene>

大型蒸汽锅炉是较小型的青铜和钢锅炉的升级版本。其燃料消耗速度是熔炉的8倍，但在完全加热时能以256 mB/t的速度产生蒸汽。

<Recipe id="moderntech:processing_table/large_steam_boiler" />

与单方块版本不同，在大型蒸汽锅炉中，任何未消耗热量的80%都会损失掉。这意味着，当输出功率降至最大值以下时，燃料消耗产生的能量比例将大幅下降。这种情况会在锅炉未以最大输出功率持续运行时发生。

主要方块是镀青铜砖块，但你还需要耐热机器外壳。

<Recipe id="moderntech:electric_age/casing/bronze_plated_bricks_asbl" />

<Recipe id="moderntech:electric_age/casing/heatproof_machine_casing_asbl" />

大型蒸汽锅炉由一层耐热机器外壳和三层镀青铜砖块组成。

控制器放在第二层（即镀青铜砖块的第一层）。

仓室必须放在底层。

