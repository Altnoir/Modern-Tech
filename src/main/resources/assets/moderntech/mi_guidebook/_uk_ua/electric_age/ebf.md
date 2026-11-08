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

Електрична доменна піч — це електрична версія парової доменної печі. Вона розблоковує нові рецепти, і, як і інші багатоблокові електричні машини, її розгін обмежено 128 RF/т усталено.

<Recipe id="moderntech:processing_table/electric_blast_furnace" />

Електрична доменна піч складається з одного шару термостійких машинних обшивок, двох шарів мідно-нікелевих котушок і ще одного шару термостійких машинних обшивок. Контролер повинен бути на нижньому шарі, а шлюзи на верхньому або нижньому шарі.

<Recipe id="moderntech:materials/cupronickel/craft/coil" />

Не забудьте додати вхідні шлюзи енергії, інакше електрична доменна піч не матиме енергії! Електричний роз'єм для подачі енергії подає в піч щонайбільше 32 RF/т.

<Recipe id="moderntech:hatches/electric/energy_input_hatch" />

Роз'єм для подачі енергії з алюмінієвого сплаву передає до 128 RF/т, що може бути корисним, якщо вам потрібна дуже багато енергії для електричної доменної печі!

<Recipe id="moderntech:hatches/aluminum_alloy/energy_input_hatch" />

