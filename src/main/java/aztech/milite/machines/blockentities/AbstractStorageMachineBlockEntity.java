package aztech.milite.machines.blockentities;

import aztech.milite.api.machine.holder.EnergyComponentHolder;
import aztech.milite.inventory.MIInventory;
import aztech.milite.machines.BEP;
import aztech.milite.machines.components.EnergyComponent;
import aztech.milite.machines.components.OrientationComponent;
import aztech.milite.machines.components.RedstoneControlComponent;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.guicomponents.EnergyBar;
import aztech.milite.machines.guicomponents.RedstoneControlGui;
import aztech.milite.machines.helper.EnergyHelper;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.models.MachineModelClientData;
import aztech.milite.MICapabilities;
import aztech.milite.util.Simulation;
import aztech.milite.util.Tickable;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public abstract class AbstractStorageMachineBlockEntity extends MachineBlockEntity implements Tickable, EnergyComponentHolder {
    protected final EnergyComponent energy;
    private final RedstoneControlComponent redstoneControl;

    protected final IEnergyStorage insertable;
    protected final IEnergyStorage extractable;

    public AbstractStorageMachineBlockEntity(BEP bep, String name, long feCapacity) {
        super(bep, new MachineGuiParameters.Builder(name, false).build(), OrientationComponent.Params.noFacing(false, false));

        this.energy = new EnergyComponent(this, feCapacity);
        this.redstoneControl = new RedstoneControlComponent();

        this.insertable = energy.buildInsertable();
        this.extractable = new IEnergyStorage() {
            private final IEnergyStorage delegate = energy.buildExtractable();

            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return delegate.receiveEnergy(maxReceive, simulate);
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                if (!redstoneControl.doAllowNormalOperation(AbstractStorageMachineBlockEntity.this)) {
                    return 0;
                }
                return delegate.extractEnergy(maxExtract, simulate);
            }

            @Override
            public int getEnergyStored() {
                return delegate.getEnergyStored();
            }

            @Override
            public int getMaxEnergyStored() {
                return delegate.getMaxEnergyStored();
            }

            @Override
            public boolean canExtract() {
                return delegate.canExtract();
            }

            @Override
            public boolean canReceive() {
                return delegate.canReceive();
            }
        };

        this.registerComponents(energy, redstoneControl);

        EnergyBar.Params energyBarParams = new EnergyBar.Params(81, 39);
        registerGuiComponent(new EnergyBar(energyBarParams, energy::getFe, energy::getCapacity),
                new RedstoneControlGui(redstoneControl));

    }

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    public MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData();
        orientation.writeModelData(data);
        return data;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, bet, (be, direction) -> {
                AbstractStorageMachineBlockEntity abe = (AbstractStorageMachineBlockEntity) be;

                return abe.orientation.outputDirection == direction ? abe.extractable : abe.insertable;
            });
        });
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            EnergyHelper.autoOutput(this, orientation, extractable);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        var energyItem = player.getItemInHand(hand).getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyItem != null) {
            if (!player.level().isClientSide()) {
                exchangeEnergyWithItem(energyItem);
            }
            return ItemInteractionResult.sidedSuccess(player.level().isClientSide());
        }
        return super.useItemOn(player, hand, face);
    }

    private void exchangeEnergyWithItem(IEnergyStorage item) {
        int available = (int) Math.min(Integer.MAX_VALUE, energy.getFe());
        if (available > 0 && item.canReceive()) {
            int charged = item.receiveEnergy(available, false);
            if (charged > 0) {
                energy.consumeFe(charged, Simulation.ACT);
                return;
            }
        }

        int room = (int) Math.min(Integer.MAX_VALUE, energy.getRemainingCapacity());
        if (room > 0 && item.canExtract()) {
            int drained = item.extractEnergy(room, false);
            if (drained > 0) {
                energy.insertFe(drained, Simulation.ACT);
            }
        }
    }
}
