package aztech.milite.machines.init;

import aztech.milite.MI;
import aztech.milite.MIBlock;
import aztech.milite.MIRegistries;
import aztech.milite.api.energy.EnergyTier;
import aztech.milite.datagen.model.MachineModelsToGenerate;
import aztech.milite.definition.BlockDefinition;
import aztech.milite.items.SortOrder;
import aztech.milite.machines.BEP;
import aztech.milite.machines.MachineBlock;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.models.MachineCasing;
import aztech.milite.machines.models.MachineCasings;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.util.MobSpawning;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class MachineRegistrationHelper {

    private static final Map<String, List<String>> MACHINE_FOLLOWS = Map.of(
            "steam_blast_furnace", List.of("bronze_boiler", "large_steam_boiler", "advanced_large_steam_boiler"),
            "steel_boiler", List.of("steam_quarry"),
            "steel_wiremill", List.of("large_tank"),
            "aluminum_alloy_wiremill", List.of("oil_drilling_rig", "electric_quarry", "vacuum_freezer"),
            "stainless_steel_distillery", List.of("distillation_tower"),
            "stainless_steel_steam_turbine", List.of("large_steam_turbine"),
            "stainless_steel_wiremill", List.of("high_pressure_large_steam_boiler", "high_pressure_advanced_large_steam_boiler", "heat_exchanger"),
            "iridium_wiremill", List.of("replicator", "plasma_turbine"));

    private static final List<String> MACHINE_TIER_PREFIXES = machineTierPrefixes();

    private static final List<String> MACHINE_HEAD = List.of("coke_oven");
    private static final List<String> MACHINE_TAIL = List.of("nuclear_reactor", "fusion_reactor");
    private static final int MACHINE_HEAD_RANK = -1;

    @SafeVarargs
    public static <T extends MachineBlockEntity> MachineDefinition<T> registerMachine(String englishName, String id,
                                                                                      Function<BEP, ? extends T> factory,
                                                                                      Consumer<? super BlockEntityType<T>>... extraRegistrators) {
        BlockEntityType<T>[] bet = new BlockEntityType[1];
        BiFunction<BlockPos, BlockState, T> ctor = (pos, state) -> factory.apply(new BEP(bet[0], pos, state));

        BlockDefinition<MachineBlock> blockDefinition = MIBlock.block(
                englishName,
                id,
                MIBlock.BlockDefinitionParams.defaultStone()
                        .sortOrder(machineSortOrder(id))
                        .withBlockConstructor((s) -> new MachineBlock(ctor, s))
                        .sound(SoundType.NETHERITE_BLOCK)
                        .withModel((block, gen) -> {
                            // 模型在 model provider 里生成
                        })
                        .isValidSpawn(MobSpawning.NO_SPAWN)
                        .isRedstoneConductor(Blocks::never));

        var blockEntityType = MIRegistries.BLOCK_ENTITIES.register(id, () -> {
            Block block = blockDefinition.asBlock();

            bet[0] = BlockEntityType.Builder.of(ctor::apply, block).build(null);

            for (Consumer<? super BlockEntityType<T>> extraRegistrator : extraRegistrators) {
                extraRegistrator.accept(bet[0]);
            }

            return bet[0];
        });

        return new MachineDefinition<>(blockEntityType, blockDefinition);
    }

    private static final List<String> HATCH_KINDS = List.of("item_input", "item_output", "fluid_input", "fluid_output",
            "energy_input", "energy_output");

    private static List<String> machineTierPrefixes() {
        var prefixes = new ArrayList<String>();
        prefixes.add("steam");
        for (var level : MachineLevel.values()) {
            prefixes.add(level.getSerializedName());
        }
        prefixes.addAll(List.of("advanced", "turbo", "highly_advanced", "nuclear"));
        return List.copyOf(prefixes);
    }

    private static SortOrder machineSortOrder(String id) {
        if (id.equals("configurable_chest")) {
            return SortOrder.BARRELS.and(Long.MAX_VALUE);
        }
        if (id.equals("configurable_tank")) {
            return SortOrder.TANKS.and(Long.MAX_VALUE);
        }
        if (id.endsWith("_hatch")) {
            return hatchSortOrder(id);
        }
        int head = MACHINE_HEAD.indexOf(id);
        if (head >= 0) {
            return SortOrder.MACHINES.and(MACHINE_HEAD_RANK).and(head);
        }
        int tail = MACHINE_TAIL.indexOf(id);
        if (tail >= 0) {
            return SortOrder.MACHINES.and(MACHINE_TIER_PREFIXES.size() + 1).and(tail);
        }
        for (var entry : MACHINE_FOLLOWS.entrySet()) {
            int index = entry.getValue().indexOf(id);
            if (index >= 0) {
                return machineSortOrder(entry.getKey()).and(index);
            }
        }
        for (int tier = 0; tier < MACHINE_TIER_PREFIXES.size(); tier++) {
            var prefix = MACHINE_TIER_PREFIXES.get(tier);
            if (id.startsWith(prefix + "_")) {
                var machine = id.substring(prefix.length() + 1);
                return SortOrder.MACHINES.and(tier).and(machineKindOrder(machine)).and(machine);
            }
        }
        return SortOrder.MACHINES.and(MACHINE_TIER_PREFIXES.size()).and(machineKindOrder(id)).and(id);
    }

    private static int machineKindOrder(String machine) {
        return switch (machine) {
            case "lithography" -> -1;
            case "blast_furnace" -> 0;
            default -> 1;
        };
    }

    private static int structureLevel(String prefix) {
        var level = MachineLevel.byNameOrNull(prefix);
        return level == null ? MachineLevel.values().length : level.ordinal();
    }

    private static SortOrder hatchSortOrder(String id) {
        if (id.equals("large_tank_hatch")) {
            return SortOrder.HATCHES.withLevel(structureLevel("steel")).and(HATCH_KINDS.size());
        }
        var kind = id.substring(0, id.length() - "_hatch".length());
        var level = MachineLevel.values().length;
        for (var prefix : MACHINE_TIER_PREFIXES) {
            if (id.startsWith(prefix + "_")) {
                kind = id.substring(prefix.length() + 1, id.length() - "_hatch".length());
                level = structureLevel(prefix);
                break;
            }
        }
        var kindOrder = HATCH_KINDS.indexOf(kind);
        return SortOrder.HATCHES.withLevel(level).and(kindOrder < 0 ? HATCH_KINDS.size() : kindOrder);
    }

    private static final Map<MachineRecipeType, MachineLevel> highestLevelByRecipeType = new HashMap<>();
    private static final Map<MachineRecipeType, List<RecipeMachineVariant>> recipeMachineVariants = new HashMap<>();

    public static void recordRecipeMachine(MachineRecipeType type, String workstation, MachineLevel level, int speedBonus) {
        recordRecipeMachine(type, workstation, level, speedBonus, null);
    }

    public static void recordRecipeMachine(MachineRecipeType type, String workstation, MachineLevel level, int speedBonus,
            @Nullable ResourceLocation coil) {
        recordMachineLevel(type, level);
        recipeMachineVariants.computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(new RecipeMachineVariant(MI.id(workstation), level, speedBonus, coil));
    }

    public static List<RecipeMachineVariant> recipeMachines(MachineRecipeType type) {
        return List.copyOf(recipeMachineVariants.getOrDefault(type, List.of()));
    }

    public static MachineLevel clampBuiltInRecipeLevel(MachineRecipeType type, MachineLevel level) {
        return recipeMachineVariants.getOrDefault(type, List.of()).stream().map(RecipeMachineVariant::level)
                .filter(machineLevel -> machineLevel.canCraft(level))
                .min(Comparator.naturalOrder()).orElse(level);
    }

    public static MachineCasing casingOf(MachineLevel level) {
        return switch (level) {
            case BRONZE -> MachineCasings.BRONZE;
            case STEEL -> MachineCasings.STEEL;
            case ELECTRIC -> EnergyTier.ELECTRIC.casing;
            case ALUMINUM -> EnergyTier.ALUMINUM.casing;
            case STAINLESS_STEEL -> EnergyTier.STAINLESS_STEEL.casing;
            case TITANIUM -> EnergyTier.TITANIUM.casing;
            case IRIDIUM -> EnergyTier.IRIDIUM.casing;
        };
    }

    public static void recordMachineLevel(MachineRecipeType recipeType, MachineLevel level) {
        highestLevelByRecipeType.merge(recipeType, level,
                (current, added) -> added.ordinal() > current.ordinal() ? added : current);
    }

    public static @Nullable MachineLevel highestLevel(MachineRecipeType recipeType) {
        return highestLevelByRecipeType.get(recipeType);
    }

    public static EnergyTier casingTierOf(MachineLevel level) {
        return switch (level) {
            case BRONZE, STEEL, ELECTRIC -> EnergyTier.ELECTRIC;
            case ALUMINUM -> EnergyTier.ALUMINUM;
            case STAINLESS_STEEL -> EnergyTier.STAINLESS_STEEL;
            case TITANIUM -> EnergyTier.TITANIUM;
            case IRIDIUM -> EnergyTier.IRIDIUM;
        };
    }

    @Deprecated(forRemoval = false)
    @SuppressWarnings("IfCanBeSwitch")
    public static void addMachineModel(String tier, String id, String machineType, boolean frontOverlay, boolean topOverlay, boolean sideOverlay) {
        MachineCasing defaultCasing;
        if (tier.equals("bronze")) {
            defaultCasing = MachineCasings.BRONZE;
        } else if (tier.equals("steel")) {
            defaultCasing = MachineCasings.STEEL;
        } else if (tier.equals("electric")) {
            defaultCasing = EnergyTier.ELECTRIC.casing;
        } else {
            throw new RuntimeException("Invalid tier: " + tier);
        }
        addMachineModel(id, machineType, defaultCasing, frontOverlay, topOverlay, sideOverlay);
    }

    public static void addMachineModel(MachineLevel level, String id, String machineType, boolean frontOverlay, boolean topOverlay,
                                       boolean sideOverlay) {
        addMachineModel(id, machineType, casingOf(level), frontOverlay, topOverlay, sideOverlay);
    }

    public record RecipeMachineVariant(ResourceLocation workstation, MachineLevel level, int speedBonus, @Nullable ResourceLocation coil) {}

    public static void addMachineModel(String id, String overlayFolder, MachineCasing defaultCasing, boolean frontOverlay, boolean topOverlay,
                                       boolean sideOverlay) {
        addMachineModel(id, overlayFolder, defaultCasing, frontOverlay, topOverlay, sideOverlay, true);
    }

    public static void addMachineModel(String id, String overlayFolder, MachineCasing defaultCasing, boolean frontOverlay, boolean topOverlay,
                                       boolean sideOverlay, boolean hasActive) {
        MachineModelsToGenerate.register(id, defaultCasing, overlayFolder, frontOverlay, topOverlay, sideOverlay, hasActive);
    }

    public static void addModelsForTiers(String name, boolean frontOverlay, boolean topOverlay, boolean sideOverlay, String... tiers) {
        for (String tier : tiers) {
            addMachineModel(tier, tier + "_" + name, name, frontOverlay, topOverlay, sideOverlay);
        }
    }
}
