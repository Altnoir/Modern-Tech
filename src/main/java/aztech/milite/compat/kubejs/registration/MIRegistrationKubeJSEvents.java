package aztech.milite.compat.kubejs.registration;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface MIRegistrationKubeJSEvents {
    EventGroup EVENT_GROUP = EventGroup.of("MIRegistrationEvents");

    EventHandler REGISTER_ENERGY_TIERS = EVENT_GROUP.startup("registerEnergyTiers", () -> RegisterEnergyTiersEventJS.class);
    EventHandler REGISTER_FLUIDS = EVENT_GROUP.startup("registerFluids", () -> RegisterFluidsEventJS.class);
    EventHandler REGISTER_FLUID_NEUTRON_INTERACTIONS = EVENT_GROUP.startup("registerFluidNeutronInteractions",
            () -> RegisterFluidNeutronInteractionsEventJS.class);
}
