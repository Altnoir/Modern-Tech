package aztech.milite;

import static net.minecraft.world.level.material.MapColor.STONE;

import aztech.milite.blocks.creativestorageunit.CreativeStorageUnitBlock;
import aztech.milite.blocks.forgehammer.ForgeHammerBlock;
import aztech.milite.blocks.overclockerunit.OverclockerUnitBlock;
import aztech.milite.blocks.processingtable.ProcessingTableBlock;
import aztech.milite.blocks.storage.barrel.BarrelBlock;
import aztech.milite.blocks.storage.barrel.BarrelItem;
import aztech.milite.blocks.storage.barrel.CreativeBarrelBlockEntity;
import aztech.milite.blocks.storage.StorageBehaviour;
import aztech.milite.blocks.storage.tank.creativetank.CreativeTankBlockEntity;
import aztech.milite.blocks.storage.tank.TankBlock;
import aztech.milite.blocks.storage.tank.TankItem;
import aztech.milite.blocks.storage.waterbarrel.WaterBarrelBlock;
import aztech.milite.blocks.TrashCanBlock;
import aztech.milite.datagen.loot.MIBlockLoot;
import aztech.milite.datagen.model.BaseModelProvider;
import aztech.milite.definition.BlockDefinition;
import aztech.milite.items.ContainerItem;
import aztech.milite.items.SortOrder;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.materials.MaterialProgression;
import aztech.milite.materials.part.TankPart;
import aztech.milite.nuclear.NuclearOrder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MIBlock {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MI.ID);
    public static final SortedMap<ResourceLocation, BlockDefinition<?>> BLOCK_DEFINITIONS = new TreeMap<>();

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    // @formatter:off
    public static final BlockDefinition<ForgeHammerBlock> FORGE_HAMMER = block("Forge Hammer", "forge_hammer",
            BlockDefinitionParams.defaultStone().withBlockConstructor(ForgeHammerBlock::new).sortOrder(SortOrder.FORGE_HAMMER).noModel().destroyTime(6.0f).explosionResistance(1200)
                    .sound(SoundType.ANVIL));

    public static final BlockDefinition<ProcessingTableBlock> PROCESSING_TABLE = block("Processing Table", "processing_table",
            BlockDefinitionParams.defaultStone().withBlockConstructor(ProcessingTableBlock::new).sortOrder(SortOrder.PROCESSING_TABLE).noModel()
                    .destroyTime(6.0f).explosionResistance(1200).sound(SoundType.WOOD));

    public static final BlockDefinition<TrashCanBlock> TRASH_CAN = block("Automatic Trash Can", "trash_can",
            BlockDefinitionParams.defaultStone().withBlockConstructor(TrashCanBlock::new).sortOrder(SortOrder.BARRELS.and(Long.MAX_VALUE).and(1)).destroyTime(6.0f).explosionResistance(1200).dontConductRedstone())
            .withBlockRegistrationEvent(TrashCanBlock::onRegister);

    // Machine hulls. They are the casing a machine is built into, so the tab lists them beside the casings of the same
    // level rather than with the odds and ends.
    public static final BlockDefinition<Block> ELECTRIC_MACHINE_HULL = machineHull("Electric Machine Hull",
            MIBlockKeys.ELECTRIC_MACHINE_HULL.location().getPath(), MachineLevel.ELECTRIC);
    public static final BlockDefinition<Block> ALUMINUM_MACHINE_HULL = machineHull("Aluminum Alloy Machine Hull",
            MIBlockKeys.ALUMINUM_MACHINE_HULL.location().getPath(), MachineLevel.ALUMINUM);
    public static final BlockDefinition<Block> STAINLESS_STEEL_MACHINE_HULL = machineHull("Stainless Steel Machine Hull",
            MIBlockKeys.STAINLESS_STEEL_MACHINE_HULL.location().getPath(), MachineLevel.STAINLESS_STEEL);
    public static final BlockDefinition<Block> TITANIUM_MACHINE_HULL = machineHull("Titanium Machine Hull",
            MIBlockKeys.TITANIUM_MACHINE_HULL.location().getPath(), MachineLevel.TITANIUM);
    public static final BlockDefinition<Block> IRIDIUM_MACHINE_HULL = block("Iridium Machine Hull",
            MIBlockKeys.IRIDIUM_MACHINE_HULL.location().getPath(),
            machineHullParams(MachineLevel.IRIDIUM).explosionResistance(6000f));

    public static final BlockDefinition<Block> FUSION_CHAMBER = nonConductorBlock("Fusion Chamber", "fusion_chamber",
            SortOrder.FUSION_CHAMBER.withLevel(MachineLevel.TITANIUM.ordinal()));
    public static final BlockDefinition<Block> INDUSTRIAL_TNT = blockExplosive("Industrial TNT", "industrial_tnt");
    public static final BlockDefinition<Block> NUKE = blockExplosive("Nuke", "nuke");

    public static final BlockDefinition<WaterBarrelBlock> WATER_BARREL = block("Water Barrel", "water_barrel",
            BlockDefinitionParams.of(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).pushReaction(PushReaction.BLOCK))
                    .withBlockConstructor(WaterBarrelBlock::new)
                    .clearTags().addMoreTags(BlockTags.MINEABLE_WITH_AXE)
                    .sortOrder(SortOrder.TANKS)
                    .withModel((block, gen) -> gen.simpleBlockWithItem(block, gen.models().cubeColumn(gen.name(block),
                            ResourceLocation.withDefaultNamespace("block/barrel_side"),
                            ResourceLocation.withDefaultNamespace("block/barrel_bottom")))));

    public static final BlockDefinition<TankBlock> CREATIVE_TANK = block(
            "Creative Tank",
            "creative_tank",
            BlockDefinitionParams.defaultStone()
                    .withBlockConstructor(() -> new TankBlock(CreativeTankBlockEntity::new, StorageBehaviour.creative()))
                    .withBlockItemConstructor(TankItem::new)
                    .withModel(TankPart.MODEL_GENERATOR)
                    .withBlockEntityRendererItemModel()
                    .noLootTable()
    )
            .withBlockRegistrationEvent((block, item) -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, vd) -> new ContainerItem.FluidHandler(stack, (TankItem) item), item);
                });
            });
    public static final BlockDefinition<BarrelBlock> CREATIVE_BARREL = block(
            "Creative Barrel",
            "creative_barrel",
            BlockDefinitionParams.defaultStone()
                    .withBlockConstructor((p) -> new BarrelBlock(CreativeBarrelBlockEntity::new, StorageBehaviour.creative()))
                    .withBlockItemConstructor(BarrelItem::new)
                    .withModel((block, gen) -> {
                        String name = gen.name(block);
                        gen.simpleBlock(block, gen.models().cubeColumn(name, gen.blockTexture(name + "_side"), gen.blockTexture(name + "_top")));
                    })
                    .withBlockEntityRendererItemModel()
                    .noLootTable()
    );

    public static final BlockDefinition<CreativeStorageUnitBlock> CREATIVE_STORAGE_UNIT = block("Creative Storage Unit",
            "creative_storage_unit", BlockDefinitionParams.defaultStone().withBlockConstructor(CreativeStorageUnitBlock::new).dontConductRedstone());

    public static final BlockDefinition<Block> BLOCK_FIRE_CLAY_BRICKS = block("Fire Clay Bricks", "fire_clay_bricks",
            BlockDefinitionParams.of(BlockBehaviour.Properties.of().mapColor(STONE)).sortOrder(SortOrder.MATERIALS.and(MaterialProgression.rankOf("fire_clay"))).destroyTime(2.0f).explosionResistance(6.0f).requiresCorrectToolForDrops());

    public static final BlockDefinition<RotatedPillarBlock> CONTROL_ROD = block("Control Rod", "control_rod",
            BlockDefinitionParams.defaultStone()
                    .sound(SoundType.NETHERITE_BLOCK)
                    .sortOrder(SortOrder.NUCLEAR.create(NuclearOrder.CONTROL_ROD))
                    .withBlockConstructor(RotatedPillarBlock::new)
                    .withModel((block, gen) -> {
                        String name = gen.name(block);
                        var model = gen.models().cubeColumn(name, gen.blockTexture("control_rod_side"),
                                gen.blockTexture("control_rod_top"));
                        var variants = gen.getVariantBuilder(block);
                        variants.partialState().with(BlockStateProperties.AXIS, Direction.Axis.Y)
                                .modelForState().modelFile(model).addModel();
                        variants.partialState().with(BlockStateProperties.AXIS, Direction.Axis.X)
                                .modelForState().modelFile(model).rotationX(90).rotationY(90).addModel();
                        variants.partialState().with(BlockStateProperties.AXIS, Direction.Axis.Z)
                                .modelForState().modelFile(model).rotationX(90).addModel();
                        gen.itemModels().getBuilder(name).parent(model);
                    }));

    public static final BlockDefinition<OverclockerUnitBlock> OVERCLOCKER_UNIT = block("Overclocker Unit", "overclocker_unit",
            overclockerUnitParams());

    // @formatter:on

    public static <T extends Block> BlockDefinition<T> block(
            String englishName, String id, BlockDefinitionParams<T> params) {
        if (params.sortOrder.isMachineStructure() && !params.soundSet) {
            params.props.sound(SoundType.NETHERITE_BLOCK);
        }
        var holder = BLOCKS.registerBlock(id, params.ctor, params.props);
        var def = new BlockDefinition<>(
                englishName,
                holder,
                params.blockItemCtor,
                params.modelGenerator,
                params.itemModelGenerator,
                params.blockLoot,
                params.tags,
                params.sortOrder);
        BLOCK_DEFINITIONS.put(holder.getId(), def);
        return def;
    }

    public static BlockDefinition<Block> block(String englishName, String id) {
        return MIBlock.block(englishName, id, BlockDefinitionParams.defaultStone());
    }

    private static BlockDefinitionParams<OverclockerUnitBlock> overclockerUnitParams() {
        var params = BlockDefinitionParams.defaultStone().sound(SoundType.COPPER).destroyTime(3.0f)
                .explosionResistance(6.0f)
                .sortOrder(SortOrder.STRUCTURES.and(Integer.MAX_VALUE));
        params.props.lightLevel(state -> 7);
        return params.withBlockConstructor(OverclockerUnitBlock::new)
                .withModel((block, gen) -> {
                    String name = gen.name(block);
                    var model = gen.models().getBuilder(name)
                            .parent(gen.models().getExistingFile(ResourceLocation.withDefaultNamespace("block/block")))
                            .texture("particle", gen.blockTexture("overclocker_unit_top"))
                            .texture("side", gen.blockTexture("overclocker_unit_side"))
                            .texture("top", gen.blockTexture("overclocker_unit_top"));
                    model.element()
                            .from(4, 0, 4).to(12, 16, 12)
                            .allFaces((direction, face) -> {
                                boolean isEnd = direction.getAxis().isVertical();
                                face.texture(isEnd ? "#top" : "#side");
                                if (isEnd) {
                                    face.uvs(4, 4, 12, 12);
                                } else {
                                    face.uvs(4, 0, 12, 16);
                                }
                            })
                            .end();
                    var variants = gen.getVariantBuilder(block);
                    variants.partialState().with(BlockStateProperties.AXIS, Direction.Axis.Y)
                            .modelForState().modelFile(model).addModel();
                    variants.partialState().with(BlockStateProperties.AXIS, Direction.Axis.X)
                            .modelForState().modelFile(model).rotationX(90).rotationY(90).addModel();
                    variants.partialState().with(BlockStateProperties.AXIS, Direction.Axis.Z)
                            .modelForState().modelFile(model).rotationX(90).addModel();
                    gen.itemModels().getBuilder(name).parent(model);
                });
    }

    public static BlockDefinition<Block> nonConductorBlock(String englishName, String id, SortOrder sortOrder) {
        return MIBlock.block(englishName, id,
                BlockDefinitionParams.defaultStone().dontConductRedstone().sortOrder(sortOrder));
    }

    /**
     * A machine hull sits at the level of the casing it is made of, so it is listed with the rest of that level.
     */
    private static BlockDefinitionParams<Block> machineHullParams(MachineLevel level) {
        return BlockDefinitionParams.defaultStone().dontConductRedstone()
                .sortOrder(SortOrder.MACHINE_HULLS.withLevel(level.ordinal()));
    }

    private static BlockDefinition<Block> machineHull(String englishName, String id, MachineLevel level) {
        return MIBlock.block(englishName, id, machineHullParams(level));
    }

    public static BlockDefinition<Block> blockExplosive(String englishName, String id) {
        return MIBlock.block(
                englishName,
                id,
                BlockDefinitionParams.of(
                                BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).ignitedByLava().instabreak().sound(SoundType.GRASS))
                        .clearTags().noModel());
    }

    public static class BlockDefinitionParams<T extends Block> {
        public final BlockBehaviour.Properties props;
        public BiConsumer<Block, BaseModelProvider> modelGenerator;
        public BiConsumer<Item, ItemModelProvider> itemModelGenerator = (item, gen) -> {
        };
        @Nullable
        public MIBlockLoot blockLoot;
        public final ArrayList<TagKey<Block>> tags = new ArrayList<>();
        public SortOrder sortOrder = SortOrder.BLOCKS_OTHERS;
        public boolean soundSet;

        public Function<BlockBehaviour.Properties, T> ctor;
        public BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor;

        protected BlockDefinitionParams(BlockBehaviour.Properties properties,
                                        Function<BlockBehaviour.Properties, T> ctor,
                                        BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor,
                                        BiConsumer<Block, BaseModelProvider> modelGenerator,
                                        BiConsumer<Item, ItemModelProvider> itemModelGenerator,
                                        @Nullable MIBlockLoot blockLoot,
                                        List<TagKey<Block>> tags,
                                        SortOrder sortOrder,
                                        boolean soundSet) {
            this.props = properties;
            this.ctor = ctor;
            this.blockItemCtor = blockItemCtor;
            this.modelGenerator = modelGenerator;
            this.itemModelGenerator = itemModelGenerator;
            this.blockLoot = blockLoot;
            this.tags.addAll(tags);
            this.sortOrder = sortOrder;
            this.soundSet = soundSet;
        }

        public static BlockDefinitionParams<Block> of() {
            return of(BlockBehaviour.Properties.of());
        }

        public static BlockDefinitionParams<Block> of(BlockBehaviour.Properties properties) {
            return new BlockDefinitionParams<>(properties, Block::new, BlockItem::new,
                    (block, modelGenerator) -> modelGenerator.simpleBlockWithItem(block, modelGenerator.cubeAll(block)),
                    (item, gen) -> {
                    },
                    new MIBlockLoot.DropSelf(),
                    List.of(BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE),
                    SortOrder.BLOCKS_OTHERS,
                    false);
        }

        public static BlockDefinitionParams<Block> defaultStone() {
            return of(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops());
        }

        /**
         * Swaps the block constructor, which changes the type parameter, so this has to build a new set of parameters
         * rather than change this one. Every field has to be carried over: a chain such as
         * {@code defaultStone().sortOrder(MACHINES).withBlockConstructor(...)} used to silently drop the sort order and
         * fall back to {@link SortOrder#BLOCKS_OTHERS}, which buried every machine in the catch all group.
         */
        public <U extends Block> BlockDefinitionParams<U> withBlockConstructor(Function<BlockBehaviour.Properties, U> ctor) {
            return new BlockDefinitionParams<>(props, ctor, (BiFunction) this.blockItemCtor, this.modelGenerator,
                    this.itemModelGenerator, this.blockLoot, this.tags, this.sortOrder, this.soundSet);
        }

        public <U extends Block> BlockDefinitionParams<U> withBlockConstructor(Supplier<U> ctor) {
            return new BlockDefinitionParams<>(props,
                    p -> ctor.get(),
                    (BiFunction) this.blockItemCtor,
                    this.modelGenerator,
                    this.itemModelGenerator,
                    this.blockLoot,
                    this.tags,
                    this.sortOrder,
                    this.soundSet);
        }

        public BlockDefinitionParams<T> withBlockItemConstructor(BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor) {
            this.blockItemCtor = blockItemCtor;
            return this;
        }

        public BlockDefinitionParams<T> withModel(BiConsumer<Block, BaseModelProvider> modelGenerator) {
            this.modelGenerator = modelGenerator;
            return this;
        }

        public BlockDefinitionParams<T> withItemModel(BiConsumer<Item, ItemModelProvider> itemModelGenerator) {
            this.itemModelGenerator = itemModelGenerator;
            return this;
        }

        public BlockDefinitionParams<T> withBlockEntityRendererItemModel() {
            return withItemModel((item, gen) -> {
                var builder = gen.getBuilder(BuiltInRegistries.ITEM.getKey(item).toString())
                        .parent(new ModelFile.UncheckedModelFile("builtin/entity"));
                var transforms = builder.transforms();
                transforms.transform(ItemDisplayContext.GUI)
                        .rotation(30, 225, 0)
                        .translation(0, 0, 0)
                        .scale(0.625f, 0.625f, 0.625f);
                transforms.transform(ItemDisplayContext.GROUND)
                        .rotation(0, 0, 0)
                        .translation(0, 3, 0)
                        .scale(0.25f, 0.25f, 0.25f);
                transforms.transform(ItemDisplayContext.FIXED)
                        .rotation(0, 0, 0)
                        .translation(0, 0, 0)
                        .scale(0.5f, 0.5f, 0.5f);
                transforms.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                        .rotation(75, 45, 0)
                        .translation(0, 2.5f, 0)
                        .scale(0.375f, 0.375f, 0.375f);
                transforms.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                        .rotation(0, 45, 0)
                        .translation(0, 0, 0)
                        .scale(0.4f, 0.4f, 0.4f);
                transforms.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                        .rotation(0, 225, 0)
                        .translation(0, 0, 0)
                        .scale(0.4f, 0.4f, 0.4f);
            });
        }

        public BlockDefinitionParams<T> withLoot(MIBlockLoot blockLoot) {
            this.blockLoot = blockLoot;
            return this;
        }

        public BlockDefinitionParams<T> noModel() {
            this.modelGenerator = (block, modelGenerator) -> modelGenerator.existingModelWithItem(block);
            return this;
        }

        public BlockDefinitionParams<T> noLootTable() {
            this.blockLoot = null;
            return this;
        }

        public BlockDefinitionParams<T> clearTags() {
            this.tags.clear();
            return this;
        }

        public BlockDefinitionParams<T> addMoreTags(TagKey<Block>... tagsToAdd) {
            return this.addMoreTags(Arrays.asList(tagsToAdd));
        }

        public BlockDefinitionParams<T> addMoreTags(Collection<TagKey<Block>> tagsToAdd) {
            this.tags.addAll(tagsToAdd);
            return this;
        }

        public BlockDefinitionParams<T> sortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public BlockDefinitionParams<T> isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn) {
            this.props.isValidSpawn(isValidSpawn);
            return this;
        }

        public BlockDefinitionParams<T> dontConductRedstone() {
            this.props.isRedstoneConductor(Blocks::never);
            return this;
        }

        public BlockDefinitionParams<T> isRedstoneConductor(BlockBehaviour.StatePredicate isRedstoneConductor) {
            this.props.isRedstoneConductor(isRedstoneConductor);
            return this;
        }

        public BlockDefinitionParams<T> destroyTime(float destroyTime) {
            this.props.destroyTime(destroyTime);
            return this;
        }

        public BlockDefinitionParams<T> explosionResistance(float explosionResistance) {
            this.props.explosionResistance(explosionResistance);
            return this;
        }

        public BlockDefinitionParams<T> sound(SoundType soundType) {
            this.props.sound(soundType);
            this.soundSet = true;
            return this;
        }

        public BlockDefinitionParams<T> requiresCorrectToolForDrops() {
            this.props.requiresCorrectToolForDrops();
            return this;
        }
    }
}
