package aztech.milite;

import static aztech.milite.items.SortOrder.ITEMS_ORDERED;
import static aztech.milite.items.SortOrder.MATERIALS;

import aztech.milite.api.energy.EnergyItemStorage;
import aztech.milite.definition.ItemDefinition;
import aztech.milite.guidebook.GuideBookItem;
import aztech.milite.items.*;
import aztech.milite.items.armor.GraviChestPlateItem;
import aztech.milite.items.armor.JetpackItem;
import aztech.milite.items.armor.QuantumArmorItem;
import aztech.milite.items.diesel_tools.DieselToolItem;
import aztech.milite.items.tools.QuantumSword;
import aztech.milite.materials.MaterialProgression;
import aztech.milite.nuclear.NeutronBehaviour;
import aztech.milite.nuclear.NuclearComponentItem;
import aztech.milite.nuclear.NuclearConstant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.SortedMap;
import java.util.TreeMap;

@SuppressWarnings("unused")
public final class MIItem {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MI.ID);
    public static final SortedMap<ResourceLocation, ItemDefinition<?>> ITEM_DEFINITIONS = new TreeMap<>();

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    // @formatter:off
    // Guide book first so people read it!
    public static final ItemDefinition<GuideBookItem> GUIDE_BOOK = item("MI Guidebook", "guidebook", GuideBookItem::new, SortOrder.GUIDE_BOOK);



    public static final ItemDefinition<Item> RUBBER_SHEET = item("Rubber Sheet", "rubber_sheet", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> PACKER_BLOCK_TEMPLATE = item("Packer Block Template", "packer_block_template", p -> new Item(p.rarity(Rarity.RARE).stacksTo(1)), ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> PACKER_DOUBLE_INGOT_TEMPLATE = item("Packer Double Ingot Template", "packer_double_ingot_template", p -> new Item(p.rarity(Rarity.RARE).stacksTo(1)), ITEMS_ORDERED.next());

    public static final ItemDefinition<Item> MOTOR = item("Motor", "motor", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> LARGE_MOTOR = item("Large Motor", "large_motor", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> PUMP = item("Pump", "pump", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> LARGE_PUMP = item("Large Pump", "large_pump", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> PISTON = item("Piston", "piston", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> CONVEYOR = item("Conveyor", "conveyor", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> ROBOT_ARM = item("Robot Arm", "robot_arm", ITEMS_ORDERED.next());

    public static final ItemDefinition<Item> CIRCUIT_BOARD = item("Analog Circuit Board", "analog_circuit_board", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> ANALOG_CIRCUIT = item("Analog Circuit", "analog_circuit", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> ELECTRONIC_CIRCUIT_BOARD = item("Electronic Circuit Board", "electronic_circuit_board", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> ELECTRONIC_CIRCUIT = item("Electronic Circuit", "electronic_circuit", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> DIGITAL_CIRCUIT_BOARD = item("Digital Circuit Board", "digital_circuit_board", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> DIGITAL_CIRCUIT = item("Digital Circuit", "digital_circuit", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> PROCESSOR = item("Processor", "processor", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> QUANTUM_PROCESSOR = item("Quantum Processor", "quantum_processor", p -> new Item(p.rarity(Rarity.RARE)), ITEMS_ORDERED.next());

    public static final ItemDefinition<Item> RESISTOR = item("Resistor", "resistor", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> CAPACITOR = item("Capacitor", "capacitor", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> INDUCTOR = item("Inductor", "inductor", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> WOOD_PULP = item("Wood Pulp", "wood_pulp", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> INVAR_ROTARY_BLADE = item("Invar Rotary Blade", "invar_rotary_blade", ITEMS_ORDERED.next());

    public static final ItemDefinition<Item> OP_AMP = item("Op Amp", "op_amp", ITEMS_ORDERED.next());


    public static final ItemDefinition<Item> AIR_INTAKE = item("Air Intake", "air_intake", p -> new Item(p.stacksTo(1)), ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> MONOCRYSTALLINE_SILICON = item("Monocrystalline Silicon", "monocrystalline_silicon", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> SILICON_WAFER = item("Silicon Wafer", "silicon_wafer", ITEMS_ORDERED.next());

    // Quantum components
    public static final ItemDefinition<Item> COOLING_CELL = item("Cooling Cell", "cooling_cell", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> ULTRADENSE_METAL_BALL = item("Ultradense Metal Ball", "ultradense_metal_ball", ITEMS_ORDERED.next());
    public static final ItemDefinition<Item> SINGULARITY = item("Singularity", "singularity", p -> new Item(p.rarity(Rarity.EPIC)), ITEMS_ORDERED.next());


    public static final ItemDefinition<Item> QUANTUM_UPGRADE = item("Quantum Upgrade", "quantum_upgrade", (p) -> new Item(p.stacksTo(1).rarity(Rarity.RARE)), ITEMS_ORDERED.next());

    public static final ItemDefinition<WrenchItem> WRENCH = itemNoModel("Wrench", "wrench", WrenchItem::new, ITEMS_ORDERED.next());

    public static final ItemDefinition<SteamDrillItem> STEAM_MINING_DRILL = itemHandheld("Steam Mining Drill", "steam_mining_drill", SteamDrillItem::new, ITEMS_ORDERED.next());

    public static final ItemDefinition<DieselToolItem> DIESEL_MINING_DRILL = itemHandheld("Diesel Mining Drill", "diesel_mining_drill", s -> new DieselToolItem(s, 7), ITEMS_ORDERED.next())
            .withItemRegistrationEvent((item) -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidFuelItemHelper.ItemStorage(stack, DieselToolItem.CAPACITY), item);
                });
            });
    public static final ItemDefinition<DieselToolItem> DIESEL_CHAINSAW = itemHandheld("Diesel Chainsaw", "diesel_chainsaw", p -> new DieselToolItem(p, 12), ITEMS_ORDERED.next())
            .withItemRegistrationEvent((item) -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidFuelItemHelper.ItemStorage(stack, DieselToolItem.CAPACITY), item);
                });
            });

    public static final ItemDefinition<PortableStorageUnit> PORTABLE_STORAGE_UNIT = itemHandheld("Portable Storage Unit", "portable_storage_unit", PortableStorageUnit::new, ITEMS_ORDERED.next())
            .withItemRegistrationEvent(item -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> new EnergyItemStorage(stack, item), item);
                });
            });

    public static final ItemDefinition<JetpackItem> DIESEL_JETPACK = item("Diesel Jetpack", "diesel_jetpack", JetpackItem::new, ITEMS_ORDERED.next())
            .withItemRegistrationEvent(item -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidFuelItemHelper.ItemStorage(stack, JetpackItem.CAPACITY), item);
                });
            });

    public static final ItemDefinition<GraviChestPlateItem> GRAVICHESTPLATE = item("Gravichestplate", "gravichestplate", GraviChestPlateItem::new, ITEMS_ORDERED.next())
            .withItemRegistrationEvent(item -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> new EnergyItemStorage(stack, item), item);
                });
            });

    public static final ItemDefinition<QuantumSword> QUANTUM_SWORD = itemHandheld("Quantum Sword", "quantum_sword", QuantumSword::new, ITEMS_ORDERED.next());
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_HELMET = item("Quantum Helmet", "quantum_helmet", s -> new QuantumArmorItem(ArmorItem.Type.HELMET, s), ITEMS_ORDERED.next());
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_CHESTPLATE = item("Quantum Chestplate", "quantum_chestplate", s -> new QuantumArmorItem(ArmorItem.Type.CHESTPLATE, s), ITEMS_ORDERED.next());
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_LEGGINGS = item("Quantum Leggings", "quantum_leggings", s -> new QuantumArmorItem(ArmorItem.Type.LEGGINGS, s), ITEMS_ORDERED.next());
    public static final ItemDefinition<QuantumArmorItem> QUANTUM_BOOTS = item("Quantum Boots", "quantum_boots", s -> new QuantumArmorItem(ArmorItem.Type.BOOTS, s), ITEMS_ORDERED.next());

    public static final ItemDefinition<Item> UNCOOKED_STEEL_DUST = item("Uncooked Steel Dust", "uncooked_steel_dust", MATERIALS.and(MaterialProgression.rankOf("steel")));
    public static final ItemDefinition<Item> MIXED_INGOT_BLASTPROOF = item("Mixed Blastproof Ingot", "mixed_ingot_blastproof", MATERIALS.and(MaterialProgression.rankOf("blastproof_alloy")));
    public static final ItemDefinition<Item> MIXED_INGOT_IRIDIUM = item("Mixed Iridium Ingot", "mixed_ingot_iridium", s -> new Item(s.food(new FoodProperties.Builder().nutrition(20).saturationModifier(1).build())), MATERIALS.and(MaterialProgression.rankOf("iridium")));
    public static final ItemDefinition<Item> MIXED_PLATE_NUCLEAR = item("Nuclear Mixed Plate", "mixed_plate_nuclear", MATERIALS.and(MaterialProgression.rankOf("nuclear_alloy")));

    public static final ItemDefinition<Item> WAX = item("Wax", "wax", WaxItem::new, ITEMS_ORDERED.next());
    public static final ItemDefinition<NuclearComponentItem> SMALL_HEAT_EXCHANGER = NuclearComponentItem.of(
            "Small Heat Exchanger", "small_heat_exchanger",
            2500, 15 * NuclearConstant.BASE_HEAT_CONDUCTION, NeutronBehaviour.NO_INTERACTION);
    public static final ItemDefinition<NuclearComponentItem> LARGE_HEAT_EXCHANGER = NuclearComponentItem.of(
            "Large Heat Exchanger", "large_heat_exchanger",
            1800, 30 * NuclearConstant.BASE_HEAT_CONDUCTION, NeutronBehaviour.NO_INTERACTION);

    // @formatter:on

    public static <T extends Item> ItemDefinition<T> item(
            String englishName,
            String path,
            Function<Item.Properties, T> ctor,
            BiConsumer<Item, ItemModelProvider> modelGenerator,
            SortOrder sortOrder) {
        var holder = ITEMS.registerItem(path, ctor);
        var def = new ItemDefinition<>(englishName, holder, modelGenerator, sortOrder);
        ITEM_DEFINITIONS.put(holder.getId(), def);
        return def;
    }

    public static ItemDefinition<Item> item(String englishName, String path, SortOrder sortOrder) {
        return MIItem.item(englishName, path, Item::new, (item, modelGenerator) -> modelGenerator.basicItem(item), sortOrder);
    }

    public static <T extends Item> ItemDefinition<T> item(String englishName, String path, Function<Item.Properties, T> ctor,
                                                          SortOrder sortOrder) {
        return MIItem.item(englishName, path, ctor, (item, modelGenerator) -> modelGenerator.basicItem(item), sortOrder);
    }

    public static ItemDefinition<Item> itemNoModel(String englishName, String path, SortOrder sortOrder) {
        return MIItem.item(englishName, path, Item::new, (item, modelGenerator) -> {
        }, sortOrder);
    }

    public static <T extends Item> ItemDefinition<T> itemNoModel(String englishName, String path, Function<Item.Properties, T> ctor,
                                                                 SortOrder sortOrder) {
        return MIItem.item(englishName, path, ctor, (item, modelGenerator) -> {
        }, sortOrder);
    }

    public static <T extends Item> ItemDefinition<T> itemHandheld(String englishName, String path, Function<Item.Properties, T> ctor,
                                                                  SortOrder sortOrder) {
        return MIItem.item(englishName, path, p -> ctor.apply(p.stacksTo(1)), (item, modelGenerator) -> {
            modelGenerator.basicItem(item).parent(modelGenerator.getExistingFile(ResourceLocation.parse("minecraft:item/handheld")));
        }, sortOrder);
    }

    private MIItem() {
    }
}
