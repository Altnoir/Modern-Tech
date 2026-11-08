---
navigation:
  title: "处理器与光刻机"
  icon: "moderntech:stainless_steel_lithography"
  position: 202
  parent: moderntech:midgame.md
item_ids:
  - moderntech:stainless_steel_lithography
  - moderntech:titanium_lithography
  - moderntech:iridium_lithography
  - moderntech:digital_circuit
  - moderntech:digital_circuit_board
  - moderntech:processor
  - moderntech:quantum_processor
  - moderntech:silicon_plate
---

# 处理器与光刻机

前期依次制作模拟电路、电子电路和数字电路。不锈钢框架使用数字电路；光刻机将硅板和金属板加工成处理器。机器有两个物品输入槽、一个输出槽，没有流体槽；每批稳定产出一个处理器，没有副产物。

数字电路板由装配机制作，再结合电子电路、运算放大器和铝线合成数字电路。

<Recipe id="moderntech:electric_age/circuit/assembler/digital_circuit_board" />
<Recipe id="moderntech:electric_age/circuit/craft/digital_circuit_asbl" />

## 准备硅板

使用压缩机将一个硅锭压成一块硅板。光刻配方使用硅板，不需要单晶硅或硅晶圆。

<Recipe id="moderntech:materials/silicon/compressor/main" />

## 第一台光刻机

使用铝合金机器外壳、两块不锈钢板、一个玻璃方块、一个机械臂和一个电子电路，在加工台中制造不锈钢光刻机。它不需要消耗自身产出的处理器。

<Recipe id="moderntech:processing_table/stainless_steel_lithography" />
<Recipe id="moderntech:lithography/processor" />

不锈钢光刻机使用一块硅板和两块退火铜板生产一个处理器，用于钛级机器。

## 升级光刻机

使用原有光刻机、下一等级的金属板、玻璃方块、机械臂和上一阶段的电路或处理器升级机器。高等级光刻机也可以加工低等级配方。

<Recipe id="moderntech:processing_table/titanium_lithography" />
钛光刻机使用一块硅板和两块超导体板生产一个量子处理器。

<Recipe id="moderntech:lithography/quantum_processor" />

<Recipe id="moderntech:processing_table/iridium_lithography" />

铱光刻机可更快地加工上述两种配方。没有超频且供电充足时，处理器在不锈钢光刻机中每批需要 10 秒，量子处理器在钛光刻机中每批需要 10 秒。
