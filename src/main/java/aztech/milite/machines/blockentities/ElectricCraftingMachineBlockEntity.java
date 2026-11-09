package aztech.milite.machines.blockentities;

import aztech.milite.api.machine.holder.EnergyComponentHolder;
import aztech.milite.machines.BEP;
import aztech.milite.machines.components.*;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.guicomponents.EnergyBar;
import aztech.milite.machines.guicomponents.ProgressBar;
import aztech.milite.machines.guicomponents.RedstoneControlGui;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.init.MachineRegistrationHelper;
import aztech.milite.machines.models.MachineModelClientData;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.MICapabilities;
import aztech.milite.util.Simulation;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ElectricCraftingMachineBlockEntity extends AbstractCraftingMachineBlockEntity implements EnergyComponentHolder {
    public ElectricCraftingMachineBlockEntity(BEP bep, MachineRecipeType recipeType, MachineInventoryComponent inventory,
            MachineGuiParameters guiParams, EnergyBar.Params energyBarParams, ProgressBar.Params progressBarParams,
            MachineLevel level) {
        super(bep, recipeType, inventory, guiParams, progressBarParams, level);
        this.redstoneControl = new RedstoneControlComponent();
        this.casing = new CasingComponent(MachineRegistrationHelper.casingTierOf(level), level.speedMultiplier());
        this.energy = new EnergyComponent(this, casing::getFeCapacity);
        this.insertable = energy.buildInsertable();
        registerGuiComponent(new EnergyBar(energyBarParams, energy::getFe, energy::getCapacity));
        registerGuiComponent(new RedstoneControlGui(redstoneControl));
        this.registerComponents(redstoneControl, casing, energy);
    }

    private final RedstoneControlComponent redstoneControl;
    private final CasingComponent casing;
    private final EnergyComponent energy;
    private final IEnergyStorage insertable;

    @Override
    public boolean isEnabled() {
        return redstoneControl.doAllowNormalOperation(this);
    }

    @Override
    public long consumeFe(long max, Simulation simulation) {
        return energy.consumeFe(max, simulation);
    }

    @Override
    public MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData(casing.getCasing());
        orientation.writeModelData(data);
        data.isActive = isActiveComponent.isActive;
        return data;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, bet, (be, direction) -> ((ElectricCraftingMachineBlockEntity) be).insertable);
        });
    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        return super.useItemOn(player, hand, face);
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

}
