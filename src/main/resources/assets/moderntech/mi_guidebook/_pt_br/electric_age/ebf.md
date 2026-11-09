---
navigation:
  title: "Alto-forno Elétrico"
  icon: "moderntech:electric_blast_furnace"
  position: 106
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_blast_furnace
  - moderntech:cupronickel_coil
  - moderntech:electric_energy_input_hatch
  - moderntech:aluminum_alloy_energy_input_hatch
---

# Alto-forno Elétrico

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="electric_blast_furnace" />
</GameScene>

O Alto-forno Elétrico usa a bobina para determinar quais níveis de receita pode processar. Uma bobina de nível maior também processa as receitas dos níveis inferiores.

<Recipe id="moderntech:processing_table/electric_blast_furnace" />

O EBF é composto por uma camada de Estruturas de Máquina à Prova de Calor, duas camadas vazadas de Bobinas de Cuproníquel e mais uma camada de Estruturas de Máquina à Prova de Calor. O controlador deve ficar na camada inferior, e as aberturas nas camadas superior ou inferior.

<Recipe id="moderntech:materials/cupronickel/craft/coil" />

Adicione escotilhas de entrada de energia para alimentar o alto-forno. A escotilha elétrica armazena 153.600 FE.

<Recipe id="moderntech:hatches/electric/energy_input_hatch" />

A escotilha de entrada de liga de alumínio armazena 614.400 FE.

<Recipe id="moderntech:hatches/aluminum_alloy/energy_input_hatch" />

