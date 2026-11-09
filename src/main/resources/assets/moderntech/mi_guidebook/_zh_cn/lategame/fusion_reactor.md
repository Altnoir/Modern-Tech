---
navigation:
  title: "聚变！！！！"
  icon: "moderntech:fusion_reactor"
  position: 302
  parent: moderntech:endgame.md
item_ids:
  - moderntech:fusion_reactor
  - moderntech:plasma_turbine
---

# 聚变！！！！

## 聚变反应堆

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="fusion_reactor" />
</GameScene>

聚变反应堆是终极的能量源！它可以将氘、氚和/或氦-3结合成氦等离子体，这是游戏中最强大的燃料！然而，它需要大量的能量来点燃反应。

<Recipe id="moderntech:processing_table/fusion_reactor" />

## 等离子涡轮机

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="plasma_turbine" />
</GameScene>

等离子涡轮机可以将氦等离子体转化为FE，转化率为100 kFE/mB。其最大功率约为1 MFE/t。

<Recipe id="moderntech:processing_table/plasma_turbine" />

