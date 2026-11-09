# ModernTech

A slimmed-down fork of [Modern Industrialization](https://github.com/AztechMC/Modern-Industrialization):
**the machines stay, the logistics go.**

Steam machines, electric machines, multiblocks (coke oven, EBF, distillation tower, fusion
reactor), materials, tools and the in-game guidebook form the factory progression:

- no cables, no transformers, no fluid pipes, no item pipes;
- machines, tanks, barrels and hatches simply expose the standard NeoForge energy, fluid and item capabilities;
- electric machines use FE: power a machine from another mod's cable, energy pipe or energy storage block, or simply
  place a generator next to it;
- machine levels progress through bronze, steel, electric, aluminum alloy, stainless steel, titanium and iridium;
- recipes specify their minimum machine level. Higher levels have fixed speed multipliers; there is no gradual
  overclocking from continuous operation. Faster processing draws more FE/t while preserving total FE per craft;
- gunpowder temporarily doubles steam-machine speed, and an Overdrive Module doubles the speed of supported
  electric machines. A full energy buffer stops accepting power.

**Bring your own logistics mod, and ModernTech supplies the factory.**

The mod id is `moderntech`.

This namespace replaces `milite`. Existing worlds, data packs, resource packs and scripts using the old ID
are not automatically migrated; back up worlds and update integrations before upgrading.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.238 or newer
- [GuideME](https://modrinth.com/mod/guideme) (required, it renders the guidebook)
- AbyssLib Reginth 1.0.0 or newer (required)
- Optional: EMI, JEI or REI for recipe viewing, Jade for machine tooltips

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Put this mod, [GuideME](https://modrinth.com/mod/guideme) and AbyssLib Reginth into your `mods` folder.
3. Craft the guidebook in game, it walks you through the whole progression.

If you are updating an existing world or modpack, see the [FE migration notes](docs/FE_MIGRATION.md) before opening it.

## Building from source

Java 21 is required.

```
./gradlew build
```

The jar is written to `build/libs`. `./gradlew runClient` starts a development client, and
`./gradlew runGameTestServer` runs the game test suite.

## Bug reports and contributions

Please report bugs on the [issue tracker](https://github.com/Altnoir/Modern-Tech/issues).
Translations live in `src/main/resources/assets/moderntech/lang`, so pull requests with translation fixes are welcome.
Modpack and resource pack developers should have a look at [docs/README.md](docs/README.md).

## Credits and license

This fork is based on [Modern Industrialization](https://github.com/AztechMC/Modern-Industrialization) by Azerococo
and Technici4n: the design, the code and most of the assets are theirs.

The code is under the MIT license and the assets are under CC0, see [LICENSE](LICENSE).

### Sound credits

The machine working sounds come from [MI-Sound-Addon](https://github.com/TheStaticVoid/MI-Sound-Addon) by TheStaticVoid
(MIT), which takes most of them from [GregTech CEu Modern](https://github.com/GregTechCEu/GregTech-Modern) (CC0) and
the Fusion Reactor sound from [Mekanism](https://github.com/mekanism/mekanism) (MIT). These sounds come from
[Freesound](https://freesound.org):

- blast_furnace: [iankath](https://freesound.org/people/iankath/sounds/173991/) - CC0
- boiler: [Ezcah](https://freesound.org/people/Ezcah/sounds/248296/) - CC0
- distillation_tower: [krypaw](https://freesound.org/people/krypaw/sounds/584898/) - CC0
- oil_drilling_rig: [gecop](https://freesound.org/people/gecop/sounds/568860/) - CC0
- packer, unpacker: [AGMoneyTrigga](https://freesound.org/people/AGMoneyTrigga/sounds/231347/) - CC0
- polarizer: [DeVern](https://freesound.org/people/DeVern/sounds/346396/) - CC0
- replicator: [GameAudio](https://freesound.org/people/GameAudio/sounds/220191/) - CC0
- pressurizer: [beerbelly38](https://freesound.org/people/beerbelly38/sounds/77935/) - CC BY 4.0
- wiremill: [tim.kahn](https://freesound.org/people/tim.kahn/sounds/622245/) - CC BY 4.0

---

## ModernTech 中文简介

**ModernTech** 是 [Modern Industrialization](https://github.com/AztechMC/Modern-Industrialization)
的精简分支。

模组 id 是 `moderntech`。模组保留机器与工业进度，移除了自带管道、电缆和变压器，物流与供电需要搭配其他模组。

新命名空间取代了 `milite`。旧存档、数据包、资源包和脚本不会自动迁移；升级前请备份存档并更新相关内容。

电动机器使用 FE，配方通过机器等级限制加工要求。各等级使用固定速度倍率，加速提高每刻能耗，但同一配方的总能耗不变。
火药可临时加速蒸汽机器，超速模块可持续加速支持它的电动机器；连续加工不再逐步提升超频。

- 需要：Minecraft 1.21.1、NeoForge 21.1.238+、[GuideME](https://modrinth.com/mod/guideme)、AbyssLib Reginth 1.0.0+
- 可选：EMI / JEI / REI、Jade
- 自行构建：需要 Java 21，执行 `./gradlew build`，产物在 `build/libs`
- 问题反馈：[issue tracker](https://github.com/Altnoir/Modern-Tech/issues)
- 协议：代码 MIT，素材 CC0
