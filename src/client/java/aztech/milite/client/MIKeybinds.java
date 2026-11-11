package aztech.milite.client;

import aztech.milite.items.ActivatableItem;
import aztech.milite.MI;
import aztech.milite.MIItem;
import aztech.milite.network.armor.ActivateItemPacket;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Set;

public class MIKeybinds {
    private static final Set<Keybind> MAPPINGS = Sets.newHashSet();

    public static Set<Keybind> getMappings() {
        return Collections.unmodifiableSet(MAPPINGS);
    }

    public static void init(RegisterKeyMappingsEvent event) {
        MAPPINGS.forEach(m -> event.register(m.holder().get()));
    }

    public static final String CATEGORY = Util.makeDescriptionId("key.categories", MI.id(MI.ID));

    public static final Keybind TOGGLE_FLIGHT = create(
            "toggle_flight",
            id -> new KeyMapping(
                    id,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    CATEGORY),
            toggleableItemAction(
                    EquipmentSlot.CHEST,
                    i -> i.is(MIItem.DIESEL_JETPACK.asItem()) || i.is(MIItem.GRAVICHESTPLATE.asItem()),
                    (player, activated) -> {}));

    private static Keybind create(String id, Function<String, KeyMapping> creator, Runnable action) {
        String descriptionId = Util.makeDescriptionId("key", MI.id(id));
        Keybind keybind = new Keybind(Lazy.of(() -> creator.apply(descriptionId)), action);
        MAPPINGS.add(keybind);
        return keybind;
    }

    private static Runnable toggleableItemAction(EquipmentSlot slot, Predicate<ItemStack> filter, BiConsumer<Player, Boolean> after) {
        return () -> {
            Player player = Minecraft.getInstance().player;
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof ActivatableItem item && filter.test(stack)) {
                boolean activated = !item.isActivated(stack);
                item.setActivated(stack, activated);
                new ActivateItemPacket(slot, activated).sendToServer();
                after.accept(player, activated);
            }
        };
    }

    public record Keybind(Lazy<KeyMapping> holder, Runnable action) {}
}
