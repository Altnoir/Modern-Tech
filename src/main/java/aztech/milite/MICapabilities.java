package aztech.milite;

import aztech.milite.api.energy.EnergyStorage;
import aztech.milite.blocks.storage.waterbarrel.WaterBarrelBlockEntity;
import aztech.milite.thirdparty.fabrictransfer.api.bridge.SlotFluidHandler;
import aztech.milite.thirdparty.fabrictransfer.api.bridge.SlotItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;

public class MICapabilities {
    private static final List<Consumer<RegisterCapabilitiesEvent>> processors = new ArrayList<>();

    public static void init(RegisterCapabilitiesEvent event) {
        // Fluids
        for (var fluid : MIFluids.FLUID_DEFINITIONS.values()) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), fluid.getBucket());
        }

        processors.forEach(c -> c.accept(event));

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MIRegistries.CREATIVE_BARREL_BE.get(), (be, side) -> new SlotItemHandler(be));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MIRegistries.CREATIVE_TANK_BE.get(), (be, side) -> new SlotFluidHandler(be));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MIRegistries.WATER_BARREL_BE.get(), WaterBarrelBlockEntity::getDrainHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MIRegistries.CREATIVE_STORAGE_UNIT_BE.get(),
                (be, side) -> EnergyStorage.CREATIVE);
    }

    public static void onEvent(Consumer<RegisterCapabilitiesEvent> consumer) {
        processors.add(consumer);
    }
}
