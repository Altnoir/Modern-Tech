package aztech.milite.machines.blockentities;

import aztech.milite.api.machine.holder.CrafterComponentHolder;
import aztech.milite.inventory.MIInventory;
import aztech.milite.machines.BEP;
import aztech.milite.machines.components.CrafterComponent;
import aztech.milite.machines.components.IsActiveComponent;
import aztech.milite.machines.components.MachineInventoryComponent;
import aztech.milite.machines.components.OrientationComponent;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.guicomponents.AutoExtract;
import aztech.milite.machines.guicomponents.MachineProblemsDisplay;
import aztech.milite.machines.guicomponents.ProgressBar;
import aztech.milite.machines.guicomponents.ReiSlotLocking;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.MachineBlockEntity;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.util.Tickable;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public abstract class AbstractCraftingMachineBlockEntity extends MachineBlockEntity implements CrafterComponent.Behavior, Tickable,
        CrafterComponentHolder {
    public AbstractCraftingMachineBlockEntity(BEP bep, MachineRecipeType recipeType, MachineInventoryComponent inventory,
            MachineGuiParameters guiParams, ProgressBar.Params progressBarParams, MachineLevel level) {
        super(bep, guiParams, new OrientationComponent.Params(true, inventory.itemOutputCount > 0, inventory.fluidOutputCount > 0));
        this.inventory = inventory;
        this.crafter = new CrafterComponent(this, inventory, this);
        this.type = recipeType;
        this.machineLevel = level;
        this.isActiveComponent = new IsActiveComponent();
        registerGuiComponent(new AutoExtract(orientation));
        registerGuiComponent(new ProgressBar(progressBarParams, crafter::getProgress));
        registerGuiComponent(new MachineProblemsDisplay(crafter::matchesMultipleRecipes));
        registerGuiComponent(new ReiSlotLocking(crafter::lockRecipe, () -> true));
        this.registerComponents(crafter, this.inventory, isActiveComponent);
    }

    private final MachineInventoryComponent inventory;
    protected final CrafterComponent crafter;

    private final MachineRecipeType type;
    private final MachineLevel machineLevel;
    protected IsActiveComponent isActiveComponent;

    @Override
    public MachineLevel getMachineLevel() {
        return machineLevel;
    }

    @Override
    public MachineRecipeType recipeType() {
        return type;
    }

    @Override
    public CrafterComponent getCrafterComponent() {
        return crafter;
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            boolean newActive = crafter.tickRecipe();
            isActiveComponent.updateActive(newActive, this);
            if (orientation.extractItems) {
                inventory.inventory.autoExtractItems(level, worldPosition, orientation.outputDirection);
            }
            if (orientation.extractFluids) {
                inventory.inventory.autoExtractFluids(level, worldPosition, orientation.outputDirection);
            }
            setChanged();
        }
    }

    @Override
    public MIInventory getInventory() {
        return inventory.inventory;
    }

    @Override
    public ServerLevel getCrafterWorld() {
        return (ServerLevel) level;
    }

    @Override
    @Nullable
    public UUID getOwnerUuid() {
        return placedBy.placerId;
    }
}
