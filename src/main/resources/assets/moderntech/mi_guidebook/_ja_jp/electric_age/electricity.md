---
navigation:
  title: "電気"
  icon: "moderntech:electric_steam_turbine"
  position: 104
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_steam_turbine
  - moderntech:electric_diesel_generator
---

# 電気

電力蒸気タービンは蒸気を利用して発電する。1mbの蒸気を1 FEに変換し、1tickあたり最大32mbまで変換する。

<Recipe id="moderntech:processing_table/electric_steam_turbine" />

電力蒸気タービンは、出力側に直接接して設置された機械へ自動的にFEを送る。内部バッファの容量は3200 FE。

電力ディーゼル発電機は、電力蒸気タービンに代わるものである。様々な燃料を使って電気を作る。今のところ、クレオソートを燃やすことができる。(燃やせる燃料のリストはREIを参照)

<Recipe id="moderntech:processing_table/electric_diesel_generator" />

## ModernTechの電力

ModernTechは、**自前のケーブルや変圧器を追加しない**。電気機械はどれも標準のNeoForgeエネルギーケイパビリティを公開しているだけなので、**他のあらゆるMOD**のFEケーブル、エネルギーパイプ、エネルギー貯蔵ブロックから電力を供給できる。

つまり:

- 他のMODのケーブルやパイプから機械へ直接電力を送るか、発電機を機械のすぐ隣に置けばよい。
- 電圧を合わせる必要はもうない。等級間で電力を移動させるためだけにマシンハルを強化したり、変圧器を経由させたりする必要はない。
- 機械の等級は加工できるレシピと速度を決め、筐体は内部バッファの容量を決める。満杯になるとFEの受け入れが止まる。

機械は入力として指定された面でしかエネルギーを受け取らず、発電機は出力側からしかエネルギーを送り出さないことを覚えておこう。
