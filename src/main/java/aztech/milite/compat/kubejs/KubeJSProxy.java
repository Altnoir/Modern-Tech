package aztech.milite.compat.kubejs;

import aztech.milite.inventory.SlotPositions;
import aztech.milite.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.milite.materials.MaterialBuilder;
import net.neoforged.fml.ModList;

import java.util.function.Consumer;

public class KubeJSProxy {
    public static void checkThatKubeJsIsLoaded() {
        if (ModList.get().isLoaded("kubejs") && instance.getClass() == KubeJSProxy.class) {
            throw new IllegalStateException("KubeJS should have initialized before ModernTech!");
        }
    }

    public static KubeJSProxy instance = new KubeJSProxy();

    public void fireAddMaterialsEvent() {}

    public void fireModifyMaterialEvent(MaterialBuilder materialBuilder) {}

    public void fireRegisterFluidsEvent() {}

    public void fireRegisterRecipeTypesEvent() {}

    public void fireRegisterMachineCasingsEvent() {}

    public void fireRegisterMachinesEvent() {}

    public void fireRegisterHatchesEvent() {}

    public void fireAddMultiblockSlotsEvent(String category, SlotPositions.Builder itemInputs, SlotPositions.Builder itemOutputs,
            SlotPositions.Builder fluidInputs, SlotPositions.Builder fluidOutputs) {}

    public void fireAddEbfTiersEvent(Consumer<ElectricBlastFurnaceBlockEntity.Tier> tierConsumer) {}

    public void fireCustomConditionEvent() {}

    public void fireEnergyTiersEvent() {}

    public void fireRegisterFluidNeutronInteractionsEvent() {}
}
