---
navigation:
  title: "蒸気機械"
  icon: "moderntech:bronze_furnace"
  position: 3
  parent: moderntech:steam_age.md
item_ids:
  - moderntech:bronze_compressor
  - moderntech:bronze_mixer
  - moderntech:bronze_macerator
  - moderntech:bronze_cutting_machine
  - moderntech:bronze_furnace
  - moderntech:wrench
---

# 蒸気機械

いよいよ青銅の機械を作る時が来た。ボイラーは自動的に蒸気を発生させた蒸気を最大8mb/tで隣接する機械に送る。

青銅の機械は2 FE/tまでのレシピしか処理できない。つまり、加工レシピは1Tickにつき2エネルギーユニットを消費する。1mbの蒸気が1 FEなので、最高温度のボイラーで4台の機械を同時に動かすことができるわけだ！

圧縮機はインゴットを1:1の割合で板にする。ダブルインゴットを2枚の板にすることもできる。おそらく最初に作りたくなる機械だろう！

<Recipe id="moderntech:processing_table/bronze_compressor" />

混合機は粉を混ぜ合わせるのに役立ち、青銅や耐火粘土、そして後で未精錬の鋼鉄を効率よく作成することができる。また、丸石や粘土を作ることもできる。

<Recipe id="moderntech:processing_table/bronze_mixer" />

粉砕機は3つの鉱石の破砕物/原石を4.5個の粉に変換する。4.5倍化というわけだ！また、あらゆる種類のアイテムを粉に変えてリサイクルすることもできる。

<Recipe id="moderntech:processing_table/bronze_macerator" />

切断機はボルト、歯車、リング、棒の生産比を向上させる。加工ごとに少量の潤滑剤を使用する。

<Recipe id="moderntech:processing_table/bronze_cutting_machine" />

かまどは……えー、かまどだ。通常のかまどと同じように速度は遅いが、蒸気が非常に効率的なために使用する燃料は10分の1である。

<Recipe id="moderntech:processing_table/bronze_furnace" />

蒸気機械はアイテムであれ、蒸気であれ、他の流体であれ、どの面からの入力でも受け入れる。

インターフェイスのロックをクリックすることで、スロットをロックすることができる。ロックされたスロットは1種類のアイテムまたは液体しか受け付けない。

すべての機械には出力側があり、インターフェイスから自動出力を有効にすることができる。もちろんホッパー、パイプ、その他の自動化機器はどの側からも出力アイテムを取り出すことができる。また、スクロールすることでスロット内のアイテムの最大数を減らすこともできる。

レンチを使えば、右クリックでどんなマシンでも回転させることができる。Shiftキーを押しながらクリックすると、出力側を回転させることができる。

スパナで機械を壊してもアイテム化しない。ツルハシを使おう！

<Recipe id="moderntech:tools/wrench" />

