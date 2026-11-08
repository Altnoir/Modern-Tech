---
navigation:
  title: "Electricity"
  icon: "moderntech:electric_steam_turbine"
  position: 104
  parent: moderntech:electric_age.md
item_ids:
  - moderntech:electric_steam_turbine
  - moderntech:electric_diesel_generator
---

# Electricity

The Steam Turbine uses steam to produce FE. It converts every mB of steam to 1 FE, up to 32 mB per tick.

<Recipe id="moderntech:processing_table/electric_steam_turbine" />

The electric Steam Turbine automatically sends FE to machines placed against its output side. Its internal buffer holds 3200 FE.

The Diesel Generator is an alternative to the Steam Turbine. It uses various fuels to produce electricity. For now, you can burn Creosote. (See REI for the list of burnable fuels).

<Recipe id="moderntech:processing_table/electric_diesel_generator" />

## Power in ModernTech

ModernTech uses the standard NeoForge energy capability. Supply FE through another mod's cables, energy pipes or storage blocks.

That means:

- Feed a machine directly from another mod's cable or pipe, or place a generator right next to it.
- There is no voltage to match any more: you no longer need to upgrade machine hulls or route power through transformers just to move it between tiers.
- Electric, aluminum alloy, stainless steel, titanium and iridium are machine levels with fixed speed multipliers and minimum recipe requirements. Casings size the energy buffer separately. A full buffer stops accepting FE.

Single-block electric crafting machines accept FE from all sides. Generators expose FE output on their configured output side.
