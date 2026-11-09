package aztech.milite.materials.part;

import aztech.milite.api.energy.EnergyTier;
import aztech.milite.items.PortableStorageUnit;

public class BatteryPart implements PartKeyProvider {
    @Override
    public PartKey key() {
        return new PartKey("battery");
    }

    public PartTemplate of(long batteryCapacity) {
        return new PartTemplate("Battery", "battery").withRegister(
                (partContext, part, itemPath, itemId, itemTag, englishName) -> {
                    var item = PartTemplate.createSimpleItem(englishName, itemPath, partContext, part);
                    item.withItemRegistrationEvent(i -> {
                        PortableStorageUnit.CAPACITY_PER_BATTERY.put(i, batteryCapacity);
                    });
                });
    }

    public PartTemplate of(EnergyTier tier) {
        // A factor of 60*20*8 = 9600 would be one minute at the max tier power. 10000 is nicer in the tooltips.
        // Clamped so that the capacity still fits in NeoForge's int-based energy API.
        return of(tier.getBatteryCapacity());
    }
}
