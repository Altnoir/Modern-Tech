package aztech.milite.api.energy;

import aztech.milite.MI;
import aztech.milite.MIBlockKeys;
import aztech.milite.compat.kubejs.KubeJSProxy;
import aztech.milite.machines.models.MachineCasing;
import aztech.milite.machines.models.MachineCasings;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Selects a machine casing and sizes its energy buffers independently of recipe eligibility. */
public final class EnergyTier implements Comparable<EnergyTier> {
    public static final EnergyTier ELECTRIC = new EnergyTier("electric", "Electric", 32, null);
    public static final EnergyTier ALUMINUM = new EnergyTier("aluminum_alloy", "Aluminum Alloy", 128,
            MIBlockKeys.ALUMINUM_MACHINE_HULL.location());
    public static final EnergyTier STAINLESS_STEEL = new EnergyTier("stainless_steel", "Stainless Steel", 1024,
            MIBlockKeys.STAINLESS_STEEL_MACHINE_HULL.location());
    public static final EnergyTier TITANIUM = new EnergyTier("titanium", "Titanium", 8192,
            MIBlockKeys.TITANIUM_MACHINE_HULL.location());
    public static final EnergyTier IRIDIUM = new EnergyTier("iridium", "Iridium", 128000000,
            MIBlockKeys.IRIDIUM_MACHINE_HULL.location());

    private static final Map<String, EnergyTier> tiers = new HashMap<>();
    private static long maxMachineBufferCapacity;

    static {
        addTier(ELECTRIC);
        addTier(ALUMINUM);
        addTier(STAINLESS_STEEL);
        addTier(TITANIUM);
        addTier(IRIDIUM);

        KubeJSProxy.instance.fireEnergyTiersEvent();
    }

    public final String name;
    public final String englishName;
    /** The reference capacity in FE before applying the buffer's size multiplier. */
    public final long baseCapacity;
    @Nullable
    public final ResourceLocation itemKey;
    @ApiStatus.Internal
    public final MachineCasing casing;

    @ApiStatus.Internal
    public EnergyTier(String name, String englishName, long baseCapacity, @Nullable ResourceLocation itemKey) {
        if (baseCapacity < 0) {
            throw new IllegalArgumentException("Negative energy tier capacity: " + baseCapacity);
        }
        this.name = name;
        this.englishName = englishName;
        this.baseCapacity = baseCapacity;
        this.itemKey = itemKey;
        this.casing = MachineCasings.create(MI.id(name), englishName);
    }

    @ApiStatus.Internal
    public static void addTier(EnergyTier tier) {
        for (var existingTier : tiers.values()) {
            if (existingTier.name.equals(tier.name)) {
                throw new IllegalArgumentException("Tier " + tier + " already exists!");
            }
            if (existingTier.baseCapacity == tier.baseCapacity) {
                throw new IllegalArgumentException("A tier with capacity " + tier.baseCapacity + " already exists!");
            }
            if (Objects.equals(existingTier.itemKey, tier.itemKey)) {
                throw new IllegalArgumentException("A tier with block key " + tier.itemKey + " already exists!");
            }
        }

        tiers.put(tier.name, tier);
        maxMachineBufferCapacity = Math.max(maxMachineBufferCapacity, tier.scaledCapacity(100));
    }

    public static EnergyTier getTier(String name) {
        EnergyTier existing = tiers.get(name);
        if (existing == null) {
            throw new NoSuchElementException("No such energy tier: " + name);
        }
        return existing;
    }

    public static List<EnergyTier> allTiers() {
        return tiers.values().stream().sorted().toList();
    }

    private long scaledCapacity(long multiplier) {
        return baseCapacity > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : baseCapacity * multiplier;
    }

    public long getMachineBufferCapacity(int multiplier) {
        return Math.min(scaledCapacity(100L * Math.max(1, multiplier)), maxMachineBufferCapacity);
    }

    public long getHatchBufferCapacity() {
        return scaledCapacity(4800);
    }

    public long getStorageUnitCapacity() {
        return Math.min(scaledCapacity(100000), Integer.MAX_VALUE);
    }

    public long getBatteryCapacity() {
        return Math.min(scaledCapacity(10000), Integer.MAX_VALUE);
    }

    @Override
    public int compareTo(EnergyTier other) {
        return Long.compare(baseCapacity, other.baseCapacity);
    }

    @Override
    public String toString() {
        return name;
    }
}
