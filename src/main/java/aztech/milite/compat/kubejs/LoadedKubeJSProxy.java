package aztech.milite.compat.kubejs;

import aztech.milite.compat.kubejs.machine.*;
import aztech.milite.compat.kubejs.material.AddMaterialsEventJS;
import aztech.milite.compat.kubejs.material.MIMaterialKubeJSEvents;
import aztech.milite.compat.kubejs.material.ModifyMaterialEventJS;
import aztech.milite.compat.kubejs.recipe.CustomConditionEventJS;
import aztech.milite.compat.kubejs.recipe.MIRecipeKubeJSEvents;
import aztech.milite.compat.kubejs.registration.MIRegistrationKubeJSEvents;
import aztech.milite.compat.kubejs.registration.RegisterEnergyTiersEventJS;
import aztech.milite.compat.kubejs.registration.RegisterFluidNeutronInteractionsEventJS;
import aztech.milite.compat.kubejs.registration.RegisterFluidsEventJS;
import aztech.milite.inventory.SlotPositions;
import aztech.milite.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.milite.materials.MaterialBuilder;

import java.util.function.Consumer;

public class LoadedKubeJSProxy extends KubeJSProxy {
    @Override
    public void fireAddMaterialsEvent() {
        MIMaterialKubeJSEvents.ADD_MATERIALS.post(new AddMaterialsEventJS());
    }

    @Override
    public void fireModifyMaterialEvent(MaterialBuilder materialBuilder) {
        MIMaterialKubeJSEvents.MODIFY_MATERIAL.post(new ModifyMaterialEventJS(materialBuilder), materialBuilder.getMaterialName());
    }

    @Override
    public void fireRegisterFluidsEvent() {
        MIRegistrationKubeJSEvents.REGISTER_FLUIDS.post(new RegisterFluidsEventJS());
    }

    @Override
    public void fireRegisterRecipeTypesEvent() {
        MIMachineKubeJSEvents.REGISTER_RECIPE_TYPES.post(new RegisterRecipeTypesEventJS());
    }

    @Override
    public void fireRegisterMachineCasingsEvent() {
        MIMachineKubeJSEvents.REGISTER_CASINGS.post(new RegisterCasingsEventJS());
    }

    @Override
    public void fireRegisterMachinesEvent() {
        MIMachineKubeJSEvents.REGISTER_MACHINES.post(new RegisterMachinesEventJS());
    }

    @Override
    public void fireRegisterHatchesEvent() {
        MIMachineKubeJSEvents.REGISTER_HATCHES.post(new RegisterHatchesEventJS());
    }

    @Override
    public void fireAddMultiblockSlotsEvent(String category, SlotPositions.Builder itemInputs, SlotPositions.Builder itemOutputs,
            SlotPositions.Builder fluidInputs, SlotPositions.Builder fluidOutputs) {
        var event = new AddMultiblockSlotsEventJS(itemInputs, itemOutputs, fluidInputs, fluidOutputs);
        MIMachineKubeJSEvents.ADD_MULTIBLOCK_SLOTS.post(event, category);
    }

    @Override
    public void fireAddEbfTiersEvent(Consumer<ElectricBlastFurnaceBlockEntity.Tier> tierConsumer) {
        MIMachineKubeJSEvents.ADD_EBF_TIERS.post(new AddEbfTiersEventJS(tierConsumer));
    }

    @Override
    public void fireCustomConditionEvent() {
        MIRecipeKubeJSEvents.CUSTOM_CONDITION.post(new CustomConditionEventJS());
    }

    @Override
    public void fireEnergyTiersEvent() {
        MIRegistrationKubeJSEvents.REGISTER_ENERGY_TIERS.post(new RegisterEnergyTiersEventJS());
    }

    @Override
    public void fireRegisterFluidNeutronInteractionsEvent() {
        MIRegistrationKubeJSEvents.REGISTER_FLUID_NEUTRON_INTERACTIONS.post(new RegisterFluidNeutronInteractionsEventJS());
    }
}
