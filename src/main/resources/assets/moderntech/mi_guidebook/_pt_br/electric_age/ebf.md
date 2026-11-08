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

O Alto-forno Elétrico é a versão elétrica do Alto-forno a Vapor. Ele desbloqueia novas receitas e, como outras máquinas multibloco elétricas, seu overclock é limitado a 128 RF/t por padrão.

<Recipe id="moderntech:processing_table/electric_blast_furnace" />

O EBF é composto por uma camada de Estruturas de Máquina à Prova de Calor, duas camadas vazadas de Bobinas de Cuproníquel e mais uma camada de Estruturas de Máquina à Prova de Calor. O controlador deve ficar na camada inferior, e as aberturas nas camadas superior ou inferior.

<Recipe id="moderntech:materials/cupronickel/craft/coil" />

Não esqueça de adicionar Escotilha(s) de Entrada de Energia ou o EBF não terá energia! Uma Escotilha de Entrada de Energia Elétrica fornece no máximo 32 RF/t ao EBF.

<Recipe id="moderntech:hatches/electric/energy_input_hatch" />

Escotilhas de Entrada de Energia de Liga de Alumínio movem até 128 RF/t, o que pode ser útil se você precisar de muita energia para o seu EBF!

<Recipe id="moderntech:hatches/aluminum_alloy/energy_input_hatch" />

