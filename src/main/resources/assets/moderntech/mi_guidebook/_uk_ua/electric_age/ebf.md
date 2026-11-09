---
navigation:
  title: "Електрична доменна піч"
  icon: "moderntech:electric_blast_furnace"
  position: 106
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_blast_furnace
  - moderntech:cupronickel_coil
  - moderntech:electric_energy_input_hatch
  - moderntech:aluminum_alloy_energy_input_hatch
---

# Електрична доменна піч

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="electric_blast_furnace" />
</GameScene>

Котушка визначає доступні рівні рецептів електричної доменної печі. Котушка вищого рівня також підтримує рецепти нижчих рівнів.

<Recipe id="moderntech:processing_table/electric_blast_furnace" />

Електрична доменна піч складається з одного шару термостійких машинних обшивок, двох шарів мідно-нікелевих котушок і ще одного шару термостійких машинних обшивок. Контролер повинен бути на нижньому шарі, а шлюзи на верхньому або нижньому шарі.

<Recipe id="moderntech:materials/cupronickel/craft/coil" />

Для подачі енергії до печі додайте вхідний енергетичний шлюз. Електричний шлюз вміщує 153 600 FE.

<Recipe id="moderntech:hatches/electric/energy_input_hatch" />

Вхідний шлюз з алюмінієвого сплаву вміщує 614 400 FE.

<Recipe id="moderntech:hatches/aluminum_alloy/energy_input_hatch" />

