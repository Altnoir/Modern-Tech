---
navigation:
  title: "Moving Fluids and Items"
  icon: "minecraft:water_bucket"
  position: 203
  parent: moderntech:midgame.md
---

# Moving Fluids and Items

ModernTech **does not include fluid pipes, item pipes, or cables**. All transport is expected to come from another mod.

## What other mods can connect to

Every ModernTech machine, tank, barrel and multiblock hatch exposes the standard NeoForge capabilities:

- **Fluid** - `IFluidHandler`, so any fluid pipe, pump or tank from another mod can fill and drain it.
- **Items** - `IItemHandler`, so any item pipe, hopper or logistics mod can insert and extract.
- **Energy** - `IEnergyStorage` (FE), so any FE cable, energy pipe or battery can power it.

Because these are the standard capabilities, no compatibility module is needed: if the other mod's pipe can move fluids or items, it can work with these blocks.

## Things to keep in mind

Machines have their own internal fluid and item buffers, and the **auto-eject** and **auto-pull** settings in the machine interface let a machine push its outputs into an adjacent inventory or tank by itself. This is often enough to chain machines together without any pipe at all.

Multiblock machines are the exception: they need **Fluid Hatches** and **Item Hatches** to exchange fluids and items with the outside world. Those hatches also expose the standard capabilities, so connect your other mod's pipes to the hatch.

Tanks can be filled and drained from any side through the standard fluid capability. The Large Tank multiblock behaves the same way through its controller or its dedicated hatch.

Note that the transfer rate is now decided by the other mod's pipes rather than by ModernTech, so a fast pipe network will move fluids far quicker than the old one-bucket-per-pipe setup ever could.
