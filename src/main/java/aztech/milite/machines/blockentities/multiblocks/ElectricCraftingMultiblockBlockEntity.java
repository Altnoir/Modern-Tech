package aztech.milite.machines.blockentities.multiblocks;

import aztech.milite.api.machine.holder.EnergyListComponentHolder;
import aztech.milite.machines.BEP;
import aztech.milite.machines.components.OrientationComponent;
import aztech.milite.machines.guicomponents.RedstoneControlGui;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.multiblocks.ShapeTemplate;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.MI;
import net.minecraft.resources.ResourceLocation;

public class ElectricCraftingMultiblockBlockEntity extends AbstractElectricCraftingMultiblockBlockEntity implements EnergyListComponentHolder {
    public ElectricCraftingMultiblockBlockEntity(BEP bep, ResourceLocation blockId, ShapeTemplate shapeTemplate,
            MachineRecipeType recipeType) {
        this(bep, blockId, shapeTemplate, recipeType, MachineLevel.STAINLESS_STEEL);
    }

    public ElectricCraftingMultiblockBlockEntity(BEP bep, ResourceLocation blockId, ShapeTemplate shapeTemplate,
            MachineRecipeType recipeType, MachineLevel level) {
        super(bep, blockId, new OrientationComponent.Params(false, false, false), new ShapeTemplate[] { shapeTemplate });
        this.recipeType = recipeType;
        this.machineLevel = level;
        registerGuiComponent(new RedstoneControlGui(redstoneControl));
    }

    public ElectricCraftingMultiblockBlockEntity(BEP bep, String name, ShapeTemplate shapeTemplate, MachineRecipeType recipeType) {
        this(bep, MI.id(name), shapeTemplate, recipeType);
    }

    public ElectricCraftingMultiblockBlockEntity(BEP bep, String name, ShapeTemplate shapeTemplate, MachineRecipeType recipeType,
            MachineLevel level) {
        this(bep, MI.id(name), shapeTemplate, recipeType, level);
    }

    private final MachineRecipeType recipeType;
    private final MachineLevel machineLevel;

    @Override
    public MachineLevel getMachineLevel() {
        return machineLevel;
    }

    @Override
    public MachineRecipeType recipeType() {
        return recipeType;
    }

    /** A controller is twice as fast as a single block machine of the same level. */
    @Override
    public int getSpeedBonus() {
        return STANDARD_SPEED_BONUS;
    }
}
