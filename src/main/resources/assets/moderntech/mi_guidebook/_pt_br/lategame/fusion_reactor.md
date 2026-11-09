---
navigation:
  title: "FUUUUUSÃO"
  icon: "moderntech:fusion_reactor"
  position: 302
  parent: moderntech:endgame.md
item_ids:
  - moderntech:fusion_reactor
  - moderntech:plasma_turbine
---

# FUUUUUSÃO

## Reator de Fusão

<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="fusion_reactor" />
</GameScene>

O Reator de Fusão é a fonte de energia suprema! Ele pode combinar Deutério, Trítio e/ou Hélio-3 em Plasma de Hélio, o combustível mais poderoso do jogo! No entanto, é necessário uma grande quantidade de energia para iniciar a reação.

<Recipe id="moderntech:processing_table/fusion_reactor" />

## Turbina de Plasma

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="plasma_turbine" />
</GameScene>

A Turbina de Plasma pode converter o Plasma de Hélio em FE, na taxa de 100 kFE por milibalde. Sua taxa máxima de produção é de aproximadamente 1 MFE/t.

<Recipe id="moderntech:processing_table/plasma_turbine" />

