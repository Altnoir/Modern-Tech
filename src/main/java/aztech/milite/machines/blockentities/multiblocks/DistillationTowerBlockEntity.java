package aztech.milite.machines.blockentities.multiblocks;

import aztech.milite.api.machine.holder.EnergyListComponentHolder;
import aztech.milite.compat.rei.machines.ReiMachineRecipes;
import aztech.milite.config.MIStartupConfig;
import aztech.milite.machines.BEP;
import aztech.milite.machines.components.OrientationComponent;
import aztech.milite.machines.guicomponents.ShapeSelection;
import aztech.milite.machines.guicomponents.RedstoneControlGui;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.init.MIMachineRecipeTypes;
import aztech.milite.machines.models.MachineCasings;
import aztech.milite.machines.multiblocks.HatchFlags;
import aztech.milite.machines.multiblocks.HatchTypes;
import aztech.milite.machines.multiblocks.ShapeTemplate;
import aztech.milite.machines.multiblocks.SimpleMember;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.MI;
import aztech.milite.MIBlock;
import aztech.milite.MIText;
import net.minecraft.network.chat.Component;

import java.util.stream.IntStream;

public class DistillationTowerBlockEntity extends AbstractElectricCraftingMultiblockBlockEntity implements EnergyListComponentHolder {
    private static final int MAX_HEIGHT = MIStartupConfig.INSTANCE.maxDistillationTowerHeight.getAsInt();
    private static final ShapeTemplate[] shapeTemplates;

    public DistillationTowerBlockEntity(BEP bep) {
        super(bep, "distillation_tower", 200, new OrientationComponent.Params(false, false, false), shapeTemplates);
        registerGuiComponent(new RedstoneControlGui(redstoneControl));

        registerGuiComponent(new ShapeSelection(new ShapeSelection.Behavior() {
            @Override
            public void handleClick(int clickedLine, int delta) {
                activeShape.incrementShape(DistillationTowerBlockEntity.this, delta);
            }

            @Override
            public int getCurrentIndex(int line) {
                return activeShape.getActiveShapeIndex();
            }
        }, new ShapeSelection.LineInfo(
                IntStream.range(1, MAX_HEIGHT + 1).<Component>mapToObj(i -> MIText.ShapeTextHeight.text()).toList(),
                IntStream.range(1, MAX_HEIGHT + 1).boxed().toList())));
    }

    @Override
    public MachineRecipeType recipeType() {
        return MIMachineRecipeTypes.DISTILLATION_TOWER;
    }

    @Override
    public MachineLevel getMachineLevel() {
        return MachineLevel.STAINLESS_STEEL;
    }

    @Override
    public int getSpeedBonus() {
        return STANDARD_SPEED_BONUS;
    }

    @Override
    public int getMaxFluidOutputs() {
        return activeShape.getActiveShapeIndex() + 1;
    }

    @Override
    public ShapeTemplate getBigShape() {
        return shapeTemplates[shapeTemplates.length - 1];
    }

    public static void registerReiShapes() {
        for (int i = 0; i < shapeTemplates.length; ++i) {
            ReiMachineRecipes.registerMultiblockShape("distillation_tower", shapeTemplates[i], "" + i);
        }
    }

    static {
        shapeTemplates = new ShapeTemplate[MAX_HEIGHT];

        SimpleMember casing = SimpleMember.forBlock(MIBlock.BLOCK_DEFINITIONS.get(MI.id("clean_stainless_steel_machine_casing")));
        HatchFlags bottom = new HatchFlags.Builder().with(HatchTypes.ENERGY_INPUT, HatchTypes.FLUID_INPUT).build();
        HatchFlags layer = new HatchFlags.Builder().with(HatchTypes.FLUID_OUTPUT).build();
        for (int i = 0; i < MAX_HEIGHT; ++i) {
            ShapeTemplate.Builder builder = new ShapeTemplate.Builder(MachineCasings.CLEAN_STAINLESS_STEEL);
            for (int y = 0; y <= i + 1; ++y) {
                builder.add3by3(y, casing, y != 0, y == 0 ? bottom : layer);
            }
            shapeTemplates[i] = builder.build();
        }
    }
}
