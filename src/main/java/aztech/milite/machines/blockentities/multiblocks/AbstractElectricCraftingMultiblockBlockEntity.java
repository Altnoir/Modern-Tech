package aztech.milite.machines.blockentities.multiblocks;

import aztech.milite.api.machine.holder.EnergyListComponentHolder;
import aztech.milite.machines.BEP;
import aztech.milite.machines.components.*;
import aztech.milite.machines.guicomponents.CraftingMultiblockGui;
import aztech.milite.machines.multiblocks.HatchBlockEntity;
import aztech.milite.machines.multiblocks.ShapeMatcher;
import aztech.milite.machines.multiblocks.ShapeTemplate;
import aztech.milite.MI;
import aztech.milite.util.Simulation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractElectricCraftingMultiblockBlockEntity extends AbstractCraftingMultiblockBlockEntity
        implements EnergyListComponentHolder, CrafterComponent.Behavior {
    public static final int STANDARD_SPEED_BONUS = 2;

    public AbstractElectricCraftingMultiblockBlockEntity(BEP bep, ResourceLocation blockId, int guiHeight,
            OrientationComponent.Params orientationParams,
            ShapeTemplate[] shapeTemplates) {
        super(bep, blockId, guiHeight, orientationParams, shapeTemplates);

        this.redstoneControl = new RedstoneControlComponent();
        registerGuiComponent(new CraftingMultiblockGui(() -> shapeValid.shapeValid, crafter::getProgress, crafter, () -> 0));
        registerComponents(redstoneControl);
    }

    public AbstractElectricCraftingMultiblockBlockEntity(BEP bep, ResourceLocation blockId,
            OrientationComponent.Params orientationParams,
            ShapeTemplate[] shapeTemplates) {
        this(bep, blockId, 200, orientationParams, shapeTemplates);
    }

    public AbstractElectricCraftingMultiblockBlockEntity(BEP bep, String name, int guiHeight, OrientationComponent.Params orientationParams,
            ShapeTemplate[] shapeTemplates) {
        this(bep, MI.id(name), guiHeight, orientationParams, shapeTemplates);
    }

    public AbstractElectricCraftingMultiblockBlockEntity(BEP bep, String name, OrientationComponent.Params orientationParams,
            ShapeTemplate[] shapeTemplates) {
        this(bep, MI.id(name), orientationParams, shapeTemplates);
    }

    protected final RedstoneControlComponent redstoneControl;
    protected final List<EnergyComponent> energyInputs = new ArrayList<>();

    @Override
    public List<EnergyComponent> getEnergyComponents() {
        return energyInputs;
    }

    @Override
    protected void onRematch(ShapeMatcher shapeMatcher) {
        super.onRematch(shapeMatcher);
        if (shapeMatcher.isMatchSuccessful()) {
            energyInputs.clear();
            for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
                hatch.appendEnergyInputs(energyInputs);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        return super.useItemOn(player, hand, face);
    }

    @Override
    protected final CrafterComponent.Behavior getBehavior() {
        return this;
    }

    @Override
    public final boolean isEnabled() {
        return redstoneControl.doAllowNormalOperation(this);
    }

    @Override
    public final long consumeFe(long max, Simulation simulation) {
        long total = 0;

        for (EnergyComponent energyComponent : energyInputs) {
            total += energyComponent.consumeFe(max - total, simulation);
        }

        return total;
    }

    @Override
    public final ServerLevel getCrafterWorld() {
        return (ServerLevel) level;
    }

    @Override
    @Nullable
    public final UUID getOwnerUuid() {
        return placedBy.placerId;
    }
}
