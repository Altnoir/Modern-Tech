package aztech.milite.machines.guicomponents;

import aztech.milite.MI;
import aztech.milite.machines.gui.GuiComponentServer;
import aztech.milite.machines.gui.MachineMenuCommon;
import aztech.milite.machines.multiblocks.HatchBlockEntity;
import aztech.milite.machines.multiblocks.HatchFlags;
import aztech.milite.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.milite.machines.multiblocks.ShapeMatcher;
import aztech.milite.machines.multiblocks.ShapeTemplate;
import aztech.milite.machines.multiblocks.SimpleMember;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Builds the active multiblock shape from the player inventory, and reports the missing materials and the progress.
 */
public class MultiblockBuild implements GuiComponentServer<Unit, MultiblockBuild.Data> {
    public static final Type<Unit, Data> TYPE = new Type<>(MI.id("multiblock_build"), StreamCodec.unit(Unit.INSTANCE), Data.STREAM_CODEC);

    private final MultiblockMachineBlockEntity machine;
    @Nullable
    private UUID viewerId;

    @Nullable
    private ShapeMatcher cachedMatcher;
    @Nullable
    private ShapeTemplate cachedShape;
    @Nullable
    private Direction cachedDirection;
    private Map<SimpleMember, Ingredient> cachedIngredients = new IdentityHashMap<>();

    public MultiblockBuild(MultiblockMachineBlockEntity machine) {
        this.machine = machine;
    }

    private static ItemStack findStack(Inventory inventory, Ingredient ingredient) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && ingredient.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static int countInInventory(@Nullable Inventory inventory, @Nullable Ingredient ingredient) {
        if (inventory == null || ingredient == null) {
            return 0;
        }

        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Same check as {@link ShapeMatcher#matches}, without its rematch side effects.
     */
    private static boolean isInPlace(ShapeMatcher matcher, Level level, BlockPos pos, SimpleMember member) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof HatchBlockEntity hatch) {
            HatchFlags flags = matcher.getHatchFlags(pos);
            if (flags != null && flags.allows(hatch.getHatchType())) {
                return true;
            }
        }

        BlockState templateState = ShapeMatcher.toTemplateState(level, pos, level.getBlockState(pos), matcher.controllerDirection);
        return member.matchesState(templateState, blockEntity);
    }

    @Override
    public void setupMenu(MenuFacade menu) {
        if (menu instanceof MachineMenuCommon machineMenu) {
            viewerId = machineMenu.getPlayer().getUUID();
        }
    }

    @Override
    public Unit params() {
        return Unit.INSTANCE;
    }

    @Override
    public Data extractData() {
        Level level = machine.getLevel();
        boolean shapeValid = machine.isShapeValid();
        Player viewer = getViewer();
        if (level == null || shapeValid || (viewer != null && viewer.isCreative())) {
            // Creative players have nothing to gather, so the panel stays empty.
            return new Data(shapeValid, 0, 0, List.of());
        }

        ShapeMatcher matcher = getMatcher();
        RegistryAccess registries = level.registryAccess();
        Inventory inventory = viewer == null ? null : viewer.getInventory();
        Set<BlockPos> positions = matcher.getPositions();
        Map<Item, Integer> needs = new HashMap<>();
        Map<Item, Ingredient> ingredients = new HashMap<>();
        int placed = 0;

        for (BlockPos pos : positions) {
            SimpleMember member = matcher.getSimpleMember(pos);
            if (isInPlace(matcher, level, pos, member)) {
                placed++;
                continue;
            }

            Item item = member.getPreviewState().getBlock().asItem();
            needs.merge(item, 1, Integer::sum);
            ingredients.computeIfAbsent(item, i -> getIngredient(member, registries));
        }

        List<Material> materials = new ArrayList<>(needs.size());
        needs.entrySet().stream()
                .sorted(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.getKey()).toString()))
                .forEach(entry -> materials.add(new Material(entry.getKey(), countInInventory(inventory, ingredients.get(entry.getKey())), entry.getValue())));

        return new Data(false, placed, positions.size(), materials);
    }

    @Override
    public Type<Unit, Data> getType() {
        return TYPE;
    }

    /**
     * Fills replaceable positions of the active shape, consuming one item per placed block except in creative.
     */
    public void build(Player player) {
        Level level = machine.getLevel();
        if (level == null || machine.isShapeValid()) {
            return;
        }

        ShapeMatcher matcher = machine.createShapeMatcher();
        boolean creative = player.isCreative();

        for (BlockPos pos : matcher.getPositions()) {
            if (matcher.matches(pos, level)) {
                continue;
            }

            BlockState currentState = level.getBlockState(pos);
            if (!ShapeMatcher.canReplaceForBuild(currentState)) {
                continue;
            }

            SimpleMember member = matcher.getSimpleMember(pos);
            BlockState placeState;
            ItemStack stack = ItemStack.EMPTY;
            if (creative) {
                placeState = member.getPreviewState();
            } else {
                stack = findStack(player.getInventory(), member.getItemPreviewState(level.registryAccess()));
                if (stack.isEmpty()) {
                    continue;
                }
                placeState = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            }

            if (level.setBlock(pos, ShapeMatcher.toWorldState(level, pos, placeState, matcher.controllerDirection), Block.UPDATE_ALL)
                    && !creative) {
                stack.shrink(1);
            }
        }
    }

    @Nullable
    private Player getViewer() {
        Level level = machine.getLevel();
        if (level == null || level.isClientSide || viewerId == null) {
            return null;
        }

        var server = level.getServer();
        return server == null ? null : server.getPlayerList().getPlayer(viewerId);
    }

    private ShapeMatcher getMatcher() {
        ShapeTemplate shape = machine.getActiveShape();
        Direction direction = machine.getOrientation().facingDirection;
        if (cachedMatcher == null || cachedShape != shape || cachedDirection != direction) {
            cachedMatcher = machine.createShapeMatcher();
            cachedShape = shape;
            cachedDirection = direction;
            cachedIngredients = new IdentityHashMap<>();
        }
        return cachedMatcher;
    }

    private Ingredient getIngredient(SimpleMember member, RegistryAccess registries) {
        return cachedIngredients.computeIfAbsent(member, m -> m.getItemPreviewState(registries));
    }

    public record Data(boolean shapeValid, int placed, int total, List<Material> materials) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                Data::shapeValid,
                ByteBufCodecs.VAR_INT,
                Data::placed,
                ByteBufCodecs.VAR_INT,
                Data::total,
                Material.STREAM_CODEC.apply(ByteBufCodecs.list()),
                Data::materials,
                Data::new);
    }

    public record Material(Item item, int have, int need) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Material> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM),
                Material::item,
                ByteBufCodecs.VAR_INT,
                Material::have,
                ByteBufCodecs.VAR_INT,
                Material::need,
                Material::new);
    }
}
