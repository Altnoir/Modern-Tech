---
navigation:
  title: "Aço Inoxidável"
  icon: "moderntech:stainless_steel_dust"
  position: 201
  parent: moderntech:midgame.md
item_ids:
  - moderntech:vacuum_freezer
  - moderntech:distillation_tower
---

# Aço Inoxidável

## Aço Inoxidável

<ItemImage id="moderntech:stainless_steel_ingot" />

O Aço Inoxidável é o próximo material que você vai querer em grande quantidade. Continue lendo para saber por quê!

Fundir o pó de aço inoxidável no Alto-Forno Elétrico (EBF) vai gerar lingotes quentes, que você pode resfriar de volta para lingotes normais com um Congelador a Vácuo.

## Freezer à Vácuo

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="vacuum_freezer" />
</GameScene>

Você pode conferir os materiais base no JEI e a forma exata usando uma chave inglesa!

<Recipe id="moderntech:processing_table/vacuum_freezer" />

## Torre de Destilação

Quando começar a produzir circuitos digitais, não deixe de construir uma Torre de Destilação. A Destilaria dá apenas uma saída no processamento de óleo, mas a torre fornece UMA saída POR CAMADA!

<Recipe id="moderntech:processing_table/distillation_tower" />

Aqui está um exemplo do menor e do maior tamanho lado a lado.
Uma torre de tamanho 2 dará apenas a primeira saída da receita, uma torre de tamanho 3 dará as duas primeiras saídas, e assim por diante...


<GameScene zoom="1" interactive={true} fullWidth={true}>
    <MultiblockShape controller="distillation_tower" />
    <MultiblockShape controller="distillation_tower" x="-6" useBigShape={true} />
</GameScene>

