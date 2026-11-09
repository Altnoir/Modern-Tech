package aztech.milite.compat.kubejs.machine;

import aztech.milite.api.energy.EnergyTier;
import aztech.milite.machines.init.MultiblockHatches;
import aztech.milite.machines.models.MachineCasings;
import dev.latvian.mods.kubejs.event.KubeEvent;

public class RegisterHatchesEventJS implements KubeEvent {
    public void energy(String energyTier) {
        MultiblockHatches.registerEnergyHatches(EnergyTier.getTier(energyTier));
    }

    public void fluid(String englishPrefix, String prefix, String casing, int bucketCapacity) {
        MultiblockHatches.registerFluidHatches(englishPrefix, prefix, MachineCasings.get(casing), bucketCapacity);
    }

    public void item(String englishPrefix, String prefix, String casing, int rows, int columns, int xStart, int yStart) {
        MultiblockHatches.registerItemHatches(englishPrefix, prefix, MachineCasings.get(casing), rows, columns, xStart, yStart);
    }
}
