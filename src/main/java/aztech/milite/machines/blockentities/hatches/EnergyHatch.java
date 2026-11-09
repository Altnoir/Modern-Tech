package aztech.milite.machines.blockentities.hatches;

import aztech.milite.MICapabilities;
import aztech.milite.api.energy.EnergyTier;
import aztech.milite.api.machine.holder.EnergyComponentHolder;
import aztech.milite.inventory.MIInventory;
import aztech.milite.machines.BEP;
import aztech.milite.machines.components.EnergyComponent;
import aztech.milite.machines.components.OrientationComponent;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.guicomponents.EnergyBar;
import aztech.milite.machines.helper.EnergyHelper;
import aztech.milite.machines.multiblocks.HatchBlockEntity;
import aztech.milite.machines.multiblocks.HatchType;
import aztech.milite.machines.multiblocks.HatchTypes;
import java.util.List;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyHatch extends HatchBlockEntity implements EnergyComponentHolder {
    public EnergyHatch(BEP bep, MachineGuiParameters guiParams, boolean input, EnergyTier tier) {
        super(bep, guiParams, input ? OrientationComponent.Params.noFacingNoOutput() : OrientationComponent.Params.noFacing(false, false));

        this.input = input;

        this.energy = new EnergyComponent(this, tier.getHatchBufferCapacity());
        insertable = energy.buildInsertable();
        extractable = energy.buildExtractable();
        EnergyBar.Params energyBarParams = new EnergyBar.Params(81, 32);
        registerGuiComponent(new EnergyBar(energyBarParams, energy::getFe, energy::getCapacity));

        this.registerComponents(energy);
    }

    private final boolean input;

    private final EnergyComponent energy;
    private final IEnergyStorage insertable;
    private final IEnergyStorage extractable;

    @Override
    public HatchType getHatchType() {
        return input ? HatchTypes.ENERGY_INPUT : HatchTypes.ENERGY_OUTPUT;
    }

    @Override
    public boolean upgradesToSteel() {
        return false;
    }

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    @Override
    public void appendEnergyInputs(List<EnergyComponent> list) {
        if (input) {
            list.add(energy);
        }
    }

    @Override
    public void appendEnergyOutputs(List<EnergyComponent> list) {
        if (!input) {
            list.add(energy);
        }
    }

    @Override
    protected void tickTransfer() {
        if (!input) {
            EnergyHelper.autoOutput(this, orientation, extractable);
        }
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, bet, (be, direction) -> {
                EnergyHatch eh = (EnergyHatch) be;
                if (eh.input) {
                    return eh.insertable;
                } else {
                    if (eh.orientation.outputDirection == direction) {
                        return eh.extractable;
                    } else {
                        return null;
                    }
                }
            });
        });
    }
}
