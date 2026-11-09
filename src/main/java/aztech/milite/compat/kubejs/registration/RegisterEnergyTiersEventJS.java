package aztech.milite.compat.kubejs.registration;

import aztech.milite.api.energy.EnergyTier;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;

/**
 * Registers energy buffer and casing tiers for custom machines.
 */
public final class RegisterEnergyTiersEventJS implements KubeEvent {
    // useful primarily for setting FE based off of a previous tier's value.
    @Info("""
            Gets a previously registered energy tier by name.
            """)
    public EnergyTier get(String name) {
        return EnergyTier.getTier(name);
    }

    @SuppressWarnings("unused") // shh, intellij
    @Info("""
            Registers a new energy tier.
            """)
    public EnergyTier register(String name, String englishName, long baseCapacity, ResourceLocation itemKey) {
        EnergyTier tier = new EnergyTier(name, englishName, baseCapacity, itemKey);
        EnergyTier.addTier(tier);
        return tier;
    }
}
