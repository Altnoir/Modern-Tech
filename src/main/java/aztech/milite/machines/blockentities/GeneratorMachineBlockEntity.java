package aztech.milite.machines.blockentities;

import static aztech.milite.machines.components.FluidItemConsumerComponent.NumberOfFuel.*;

import aztech.milite.MICapabilities;
import aztech.milite.api.machine.holder.EnergyComponentHolder;
import aztech.milite.definition.FluidDefinition;
import aztech.milite.inventory.ConfigurableFluidStack;
import aztech.milite.inventory.ConfigurableItemStack;
import aztech.milite.inventory.MIInventory;
import aztech.milite.inventory.SlotPositions;
import aztech.milite.machines.BEP;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.components.*;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.guicomponents.EnergyBar;
import aztech.milite.machines.guicomponents.RedstoneControlGui;
import aztech.milite.machines.helper.EnergyHelper;
import aztech.milite.machines.models.MachineModelClientData;
import aztech.milite.util.Simulation;
import aztech.milite.util.Tickable;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class GeneratorMachineBlockEntity extends MachineBlockEntity implements Tickable, EnergyComponentHolder {
    private final IEnergyStorage extractable;
    private final RedstoneControlComponent redstoneControl;

    protected final MIInventory inventory;
    protected EnergyComponent energy;
    protected IsActiveComponent isActiveComponent;

    protected FluidItemConsumerComponent fluidItemConsumer;

    public GeneratorMachineBlockEntity(BEP bep,
            String name,
            boolean hasFacing,
            long energyCapacity,
            long fluidCapacity,
            FluidItemConsumerComponent fluidItemConsumer) {
        super(bep, new MachineGuiParameters.Builder(name, fluidItemConsumer.doAllowMoreThanOne()).build(),
                hasFacing ? new OrientationComponent.Params(true, false, false) : OrientationComponent.Params.noFacing(false, false));

        this.energy = new EnergyComponent(this, energyCapacity);
        this.extractable = energy.buildExtractable();
        this.isActiveComponent = new IsActiveComponent();
        this.fluidItemConsumer = fluidItemConsumer;
        this.redstoneControl = new RedstoneControlComponent();

        EnergyBar.Params energyBarParams = new EnergyBar.Params(76, 39);
        registerGuiComponent(new EnergyBar(energyBarParams, energy::getFe, energy::getCapacity));

        List<ConfigurableItemStack> itemStacks;
        List<ConfigurableFluidStack> fluidStacks;

        SlotPositions itemPositions = SlotPositions.empty();
        SlotPositions fluidPositions = SlotPositions.empty();

        var numberOfFluid = fluidItemConsumer.fluidFEProductionMap.getNumberOfFuel();
        var numberOfItem = fluidItemConsumer.itemFEProductionMap.getNumberOfFuel();

        if (numberOfFluid == NONE
                && numberOfItem == NONE) {
            throw new IllegalArgumentException(
                    String.format("GeneratorMachineBlockEntity %s must accept at least one item or fluid", name));
        }

        if (numberOfItem != NONE) {
            if (numberOfItem == MANY) {
                itemStacks = List.of(ConfigurableItemStack.standardInputSlot());
            } else {
                itemStacks = List.of(ConfigurableItemStack.lockedInputSlot(
                        fluidItemConsumer.itemFEProductionMap.getAllAccepted().iterator().next()));
            }
        } else {
            itemStacks = Collections.emptyList();
            itemPositions = SlotPositions.empty();
            fluidPositions = new SlotPositions.Builder().addSlot(25, 38).build();
        }

        if (numberOfFluid != NONE) {

            if (fluidCapacity == 0) {
                throw new IllegalArgumentException(
                        String.format("GeneratorMachineBlockEntity %s must have a fluid capacity > 0 has it accepts fluids", name));
            }

            if (numberOfFluid == MANY) {
                fluidStacks = List.of(ConfigurableFluidStack.standardInputSlot(fluidCapacity));
            } else {
                fluidStacks = List.of(ConfigurableFluidStack.lockedInputSlot(fluidCapacity,
                        fluidItemConsumer.fluidFEProductionMap.getAllAccepted().iterator().next()));
            }

            if (numberOfItem != NONE) {
                itemPositions = new SlotPositions.Builder().addSlot(15, 38).build();
                fluidPositions = new SlotPositions.Builder().addSlot(35, 38).build();
            }

        } else {
            fluidStacks = Collections.emptyList();
            fluidPositions = SlotPositions.empty();
            itemPositions = new SlotPositions.Builder().addSlot(25, 38).build();
        }

        inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);

        this.registerComponents(energy, isActiveComponent, inventory, fluidItemConsumer, redstoneControl);
        this.registerGuiComponent(new RedstoneControlGui(redstoneControl));
    }

    public GeneratorMachineBlockEntity(BEP bep,
            String name,
            boolean hasFacing,
            long energyCapacity,
            long fluidCapacity,
            long maxEnergyOutput,
            FluidDefinition acceptedFluid,
            long fluidFEperMb) {
        this(bep, name, hasFacing, energyCapacity, fluidCapacity,
                FluidItemConsumerComponent.ofSingleFluid(
                        maxEnergyOutput,
                        acceptedFluid,
                        fluidFEperMb));
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    public MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData();
        data.isActive = isActiveComponent.isActive;
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide)
            return;

        if (!redstoneControl.doAllowNormalOperation(this)) {
            isActiveComponent.updateActive(false, this);
        } else {
            long feProduced = fluidItemConsumer.getFeProduction(inventory.getFluidStacks(),
                    inventory.getItemStacks(),
                    energy.getRemainingCapacity());

            energy.insertFe(feProduced, Simulation.ACT);
            isActiveComponent.updateActive(0 != feProduced, this);
        }
        EnergyHelper.autoOutput(this, orientation, extractable);
        setChanged();
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, bet, (be, direction) -> {
                GeneratorMachineBlockEntity abe = (GeneratorMachineBlockEntity) be;
                if (abe.orientation.outputDirection == direction) {
                    return abe.extractable;
                } else {
                    return null;
                }
            });
        });
    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        return super.useItemOn(player, hand, face);
    }

    @Override
    public List<Component> getTooltips(Item.TooltipContext context) {
        return fluidItemConsumer.getTooltips();
    }

}
