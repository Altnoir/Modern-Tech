package aztech.milite.machines.init;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.Map;

/**
 * The tier ladder of the mod. A recipe declares the level it needs, and a machine declares the level it is, so a recipe
 * may only run on a machine of its level or above.
 *
 * <p>
 * The order of the constants is the order of the ladder, and {@link #ordinal()} is used to compare two levels. Inserting
 * a constant in the middle therefore changes the meaning of every saved level, so new levels must be appended at the end
 * or the whole ladder must be migrated.
 *
 * <p>
 * Energy buffer sizes come from the machine's casing or dedicated storage configuration.
 */
public enum MachineLevel implements StringRepresentable {
    BRONZE("bronze", 1),
    STEEL("steel", 2),
    ELECTRIC("electric", 4),
    ALUMINUM("aluminum_alloy", 8),
    STAINLESS_STEEL("stainless_steel", 16),
    TITANIUM("titanium", 32),
    IRIDIUM("iridium", 64);

    public static final Codec<MachineLevel> CODEC = StringRepresentable.fromEnum(MachineLevel::values);

    /**
     * The speed index of this level. A recipe uses its own level as the reference, so its speed depends on the ratio
     * between the machine's index and the recipe's index.
     */
    private final int speedMultiplier;

    /** The lower case name used in ids and translation keys, for example {@code "stainless_steel"}. */
    private final String levelName;

    MachineLevel(String name, int speedMultiplier) {
        this.levelName = name;
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * @return the speed index relative to {@link #BRONZE}
     */
    public int speedMultiplier() {
        return speedMultiplier;
    }

    public int speedMultiplierFrom(MachineLevel required) {
        return Math.max(1, speedMultiplier / required.speedMultiplier);
    }

    @Override
    public String getSerializedName() {
        return levelName;
    }

    @Override
    public String toString() {
        return levelName;
    }

    /**
     * @return true if a machine of this level may craft a recipe of {@code required}
     */
    public boolean canCraft(MachineLevel required) {
        return required.ordinal() <= ordinal();
    }

    public String getTranslationKey() {
        return "machine_level.moderntech." + levelName;
    }

    /**
     * The English name, with every word capitalised, for example {@code "Stainless Steel"}. The serialised name is not
     * suitable on its own because it is lower case and uses underscores.
     */
    public String getEnglishName() {
        StringBuilder englishName = new StringBuilder();
        for (String word : levelName.split("_")) {
            if (!englishName.isEmpty()) {
                englishName.append(' ');
            }
            englishName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return englishName.toString();
    }

    /**
     * The English name followed by a space, ready to be put in front of a machine name.
     */
    public String getEnglishPrefix() {
        return getEnglishName() + " ";
    }

    /**
     * The name shown to the player, for example on a recipe in the viewer, where it replaces the energy the recipe used
     * to be labelled with.
     */
    public MutableComponent getDisplayName() {
        return Component.translatable(getTranslationKey());
    }

    public static MachineLevel byName(String name) {
        MachineLevel level = byNameOrNull(name);
        if (level == null) {
            throw new IllegalArgumentException("Unknown machine level " + name);
        }
        return level;
    }

    /**
     * Names that are not the serialised name of a level but that still name the material its structures are built from.
     * The aluminum alloy machines are made of aluminum, and the level itself was called {@code "aluminum"} before the
     * alloy took that name over, so a block built from aluminum has to find the level under either name.
     */
    private static final Map<String, MachineLevel> MATERIAL_ALIASES = Map.of("aluminum", ALUMINUM);

    /**
     * The level of the given name, or null when the name is not a level at all, which is the case for every other
     * material a machine structure can be made of.
     */
    public static MachineLevel byNameOrNull(String name) {
        String lowerName = name.toLowerCase(Locale.ROOT);
        for (MachineLevel level : values()) {
            if (level.levelName.equals(lowerName)) {
                return level;
            }
        }
        return MATERIAL_ALIASES.get(lowerName);
    }
}
