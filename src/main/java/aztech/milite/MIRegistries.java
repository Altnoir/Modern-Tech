package aztech.milite;

import aztech.milite.attributes.InfiniteDamageAttribute;
import aztech.milite.attributes.QuantumArmorAttribute;
import aztech.milite.blocks.creativestorageunit.CreativeStorageUnitBlockEntity;
import aztech.milite.blocks.forgehammer.ForgeHammerRecipe;
import aztech.milite.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.milite.blocks.processingtable.ProcessingTableRecipe;
import aztech.milite.blocks.processingtable.ProcessingTableScreenHandler;
import aztech.milite.blocks.storage.barrel.CreativeBarrelBlockEntity;
import aztech.milite.blocks.storage.tank.creativetank.CreativeTankBlockEntity;
import aztech.milite.blocks.storage.waterbarrel.WaterBarrelBlockEntity;
import aztech.milite.items.CreativeSections;
import aztech.milite.machines.gui.MachineMenuCommon;
import com.altnoir.abysslib.creative.BannerStyle;
import com.altnoir.abysslib.creative.CreativeTabSection;
import com.altnoir.abysslib.creative.SectionedCreativeModeTab;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;
import java.util.Set;

/**
 * Misc registry objects.
 */
public class MIRegistries {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MI.ID);

    public static final Supplier<BlockEntityType<CreativeBarrelBlockEntity>> CREATIVE_BARREL_BE = BLOCK_ENTITIES.register("creative_barrel", () -> {
        return BlockEntityType.Builder.of(CreativeBarrelBlockEntity::new, MIBlock.CREATIVE_BARREL.get()).build(null);
    });
    public static final Supplier<BlockEntityType<CreativeTankBlockEntity>> CREATIVE_TANK_BE = BLOCK_ENTITIES.register("creative_tank", () -> {
        return BlockEntityType.Builder.of(CreativeTankBlockEntity::new, MIBlock.CREATIVE_TANK.get()).build(null);
    });
    public static final Supplier<BlockEntityType<WaterBarrelBlockEntity>> WATER_BARREL_BE = BLOCK_ENTITIES.register("water_barrel", () -> {
        return BlockEntityType.Builder.of(WaterBarrelBlockEntity::new, MIBlock.WATER_BARREL.get()).build(null);
    });
    public static final Supplier<BlockEntityType<CreativeStorageUnitBlockEntity>> CREATIVE_STORAGE_UNIT_BE = BLOCK_ENTITIES
            .register("creative_storage_unit", () -> {
                return BlockEntityType.Builder.of(CreativeStorageUnitBlockEntity::new, MIBlock.CREATIVE_STORAGE_UNIT.get()).build(null);
            });

    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(
            NeoForgeRegistries.CONDITION_SERIALIZERS,
            MI.ID);

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MI.ID);

    public static final Supplier<MenuType<ForgeHammerScreenHandler>> FORGE_HAMMER_MENU = MENUS.register("forge_hammer", () -> {
        return new MenuType<>(ForgeHammerScreenHandler::new, FeatureFlags.VANILLA_SET);
    });
    public static final Supplier<MenuType<ProcessingTableScreenHandler>> PROCESSING_TABLE_MENU = MENUS.register("processing_table", () -> {
        return new MenuType<>(ProcessingTableScreenHandler::new, FeatureFlags.VANILLA_SET);
    });
    public static final Supplier<MenuType<? extends MachineMenuCommon>> MACHINE_MENU = MENUS.register("machine", () -> {
        return IMenuTypeExtension.create(MICommonProxy.INSTANCE::createClientMachineMenu);
    })::get;

    public static final DeferredRegister<PoiType> POIS = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MI.ID);

    public static final DeferredHolder<PoiType, PoiType> INDUSTRIALIST_POI = POIS.register("industrialist", () -> {
        return new PoiType(Set.copyOf(MIBlock.FORGE_HAMMER.asBlock().getStateDefinition().getPossibleStates()), 1, 1);
    });

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MI.ID);

    public static final Supplier<RecipeSerializer<ForgeHammerRecipe>> FORGE_HAMMER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("forge_hammer",
            ForgeHammerRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<ProcessingTableRecipe>> PROCESSING_TABLE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register("processing_table", ProcessingTableRecipe.Serializer::new);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MI.ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ForgeHammerRecipe>> FORGE_HAMMER_RECIPE_TYPE = RECIPE_TYPES.register("forge_hammer",
            () -> RecipeType.simple(MI.id("forge_hammer")));

    public static final DeferredHolder<RecipeType<?>, RecipeType<ProcessingTableRecipe>> PROCESSING_TABLE_RECIPE_TYPE = RECIPE_TYPES
            .register("processing_table", () -> RecipeType.simple(MI.id("processing_table")));

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MI.ID);

    /**
     * How much of its first row a section spends on the heading banner: three of the nine slots the tab is wide, so the
     * entries of the section start after the heading on that same row. The library draws the heading on its own
     * {@code abysslib:textures/gui/section/banner_3.png}, which is cut to that width.
     */
    private static final int SECTION_BANNER_UNITS = 3;

    /**
     * One tab, divided into the sections of {@link CreativeSections}. The tab is built by the library rather than by
     * vanilla because a section heading needs somewhere to be drawn, which is the empty slots each section leaves at
     * the start of its first row.
     */
    private static final Supplier<CreativeModeTab> TAB = TABS.register("general", () -> SectionedCreativeModeTab
            .configure(CreativeModeTab.builder()
                            .title(MIText.ModernIndustrialization.text())
                            .icon(() -> MIBlock.FORGE_HAMMER.asItem().getDefaultInstance()),
                    BannerStyle.texture(SECTION_BANNER_UNITS),
                    parameters -> CreativeSections.populate(),
                    CreativeSections.ALL.toArray(CreativeTabSection[]::new))
            .build());

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MI.ID);

    public static final Holder<VillagerProfession> INDUSTRIALIST = VILLAGER_PROFESSIONS.register("industrialist", () -> {
        return new VillagerProfession(
                INDUSTRIALIST_POI.getId().toString(),
                e -> e.is(INDUSTRIALIST_POI.getId()),
                e -> e.is(INDUSTRIALIST_POI.getId()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.VILLAGER_WORK_TOOLSMITH);
    });

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, MI.ID);

    public static final Holder<Attribute> QUANTUM_ARMOR = ATTRIBUTES.register("quantum_armor", () -> new QuantumArmorAttribute().setSyncable(true));
    public static final Holder<Attribute> INFINITE_DAMAGE = ATTRIBUTES.register("infinite_damage",
            () -> new InfiniteDamageAttribute().setSyncable(true));

    static void init(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        CONDITIONS.register(modBus);
        MENUS.register(modBus);
        POIS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        RECIPE_TYPES.register(modBus);
        TABS.register(modBus);
        VILLAGER_PROFESSIONS.register(modBus);
        ATTRIBUTES.register(modBus);
    }
}
