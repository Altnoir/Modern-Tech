---
navigation:
  title: "Electric Blast Furnace"
  icon: "moderntech:electric_blast_furnace"
  position: 106
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_blast_furnace
  - moderntech:cupronickel_coil
  - moderntech:kanthal_coil
  - moderntech:superconductor_coil
  - moderntech:electric_energy_input_hatch
  - moderntech:aluminum_alloy_energy_input_hatch
---

# Electric Blast Furnace

<GameScene zoom="2" interactive={true} fullWidth={true}>
    <MultiblockShape controller="electric_blast_furnace" />
</GameScene>

The Electric Blast Furnace is the electric version of the Steam Blast Furnace. There is only one of them, and it runs at one of three tiers that you pick with the slider in its interface: the Cupronickel Coil tier, the Kanthal Coil tier and the Superconductor Coil tier. The tier you pick is the level the furnace runs at, and a higher tier also runs every recipe of the tiers below it. The coil in the structure has to be the coil of that tier, so the slider and the coils you build with must agree or the multiblock will not form. The recipes of the Steam Blast Furnace stay with the steam furnace, which is the only one that runs them, so keep it around for the few recipes that only it knows.

<Recipe id="moderntech:processing_table/electric_blast_furnace" />

The furnace is made of one layer of Heatproof Machine Casings, two hollow layers of coil and one other layer of Heatproof Machine Casings; the two middle layers take the coil of the tier the slider is set to. The controller must go on the bottom layer, and hatches on the top or bottom layer.

<Recipe id="moderntech:materials/cupronickel/craft/coil" />

Power the furnace through Energy Input Hatches. An Electric Energy Input Hatch stores 153,600 FE.

<Recipe id="moderntech:hatches/electric/energy_input_hatch" />

An Aluminum Alloy Energy Input Hatch stores 614,400 FE. A larger buffer lets the furnace run longer between energy deliveries.

<Recipe id="moderntech:hatches/aluminum_alloy/energy_input_hatch" />

