---
navigation:
  title: "电力高炉"
  icon: "moderntech:electric_blast_furnace"
  position: 106
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_blast_furnace
  - moderntech:cupronickel_coil
  - moderntech:kanthal_coil
  - moderntech:superconductor_coil
  - moderntech:electric_energy_input_hatch
  - moderntech:aluminum_alloy_energy_input_hatch
---

# 电力高炉

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="electric_blast_furnace" />
</GameScene>

电力高炉是蒸汽高炉的电动版本。现在只有这一台电力高炉，它有三个档位，用界面里的滑块选择：白铜线圈档、坎塔尔合金线圈档和超导线圈档。滑块拨到哪一档，机器就按那一档的等级运行，而高等级档位还能处理所有低等级档位的配方。结构里必须放所选档位对应的那种线圈，滑块的选择与结构里的线圈不一致时多方块不会成型。蒸汽高炉的配方仍归蒸汽高炉所有，只有它能处理，所以那几条只有它会做的配方还是得留着蒸汽高炉。

<Recipe id="moderntech:processing_table/electric_blast_furnace" />

电力高炉由一层耐热机器外壳、两层中空的线圈和另一层耐热机器外壳制成，中间的两层要放滑块所选档位对应的线圈。控制器必须放在底层，且仓室需放在顶层或底层。

<Recipe id="moderntech:materials/cupronickel/craft/coil" />

不要忘记添加能量输入仓，否则电力高炉将没有能量！电力能量输入仓最多向高炉输入32 RF/t。

<Recipe id="moderntech:hatches/electric/energy_input_hatch" />

铝合金能量输入仓最多传输128 RF/t，如果你的电力高炉需要大量能量，这会非常有用！

<Recipe id="moderntech:hatches/aluminum_alloy/energy_input_hatch" />

