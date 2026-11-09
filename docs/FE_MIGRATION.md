# FE format migration

**Back up existing worlds before opening them with this version.** Shut down the game or server and rename the following keys in ModernTech block entity data using an offline NBT editor. Keep the existing numeric values and NBT types. Opening an unconverted world can reset stored energy and partially burned fuel to zero.

| Old NBT key | New NBT key | Stored value |
|---|---|---|
| `storedEu` | `storedFe` | Energy in machines, storage blocks and energy hatches |
| `euBuffer` | `feBuffer` | Generator fuel energy remaining |
| `burningEuBuffer` | `burningFeBuffer` | Boiler fuel energy remaining |
| `burningItemTotalEu` | `burningItemTotalFe` | Total energy of the currently burning solid fuel |

The values already represent FE. Existing item energy uses the `moderntech:energy` component. Block, item, recipe and data map IDs are unchanged.

## Datapacks and KubeJS

| Old format or API | Replacement |
|---|---|
| `eu_per_mb` in the `moderntech:fluid_fuels` data map | `fe_per_mb` |
| Recipe field `eu` or script method `.eu(...)` | JSON `energy`, script `.energy(...)` or `.fe(...)` |
| `registerCableTiers` and the `CableTier` Java type | `registerEnergyTiers` and `EnergyTier` |
| Tier registration `register(id, shortName, longName, fe, hull)` | `register(id, englishName, baseCapacity, hull)` |
| Tier reference value `tier.fe` or `tier.getFe()` | `tier.baseCapacity` |
| Energy tier names `lv`, `mv` / `aluminum`, `hv`, `ev`, `superconductor` | `electric`, `aluminum_alloy`, `stainless_steel`, `titanium`, `iridium` |
| EBF tier `add(id, englishName, coil, maxBaseFE, level)` | `add(id, englishName, coil, level)` |
| `simpleGeneratorSingleBlock` with an energy tier argument after the generator ID | Remove that argument. The following arguments still define generation rate and buffer size. |

## Large energy buffers

Machine screens and the ModernTech Jade integration use long FE values. NeoForge's standard FE interface reports at most 2,147,483,647 FE for either stored energy or capacity and transfers at most that amount per call. The full internal capacity remains available over multiple calls.
