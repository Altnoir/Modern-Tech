package aztech.milite.machines.blockentities;

import aztech.milite.machines.BEP;
import aztech.milite.machines.components.MachineInventoryComponent;
import aztech.milite.machines.components.OverclockComponent;
import aztech.milite.machines.gui.MachineGuiParameters;
import aztech.milite.machines.guicomponents.GunpowderOverclockGui;
import aztech.milite.machines.guicomponents.ProgressBar;
import aztech.milite.machines.helper.SteamHelper;
import aztech.milite.machines.init.MachineLevel;
import aztech.milite.machines.models.MachineModelClientData;
import aztech.milite.machines.recipe.MachineRecipeType;
import aztech.milite.util.Simulation;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;

import java.util.List;

public class SteamCraftingMachineBlockEntity extends AbstractCraftingMachineBlockEntity {
    private final OverclockComponent overclockComponent;

    public SteamCraftingMachineBlockEntity(BEP bep, MachineRecipeType recipeType, MachineInventoryComponent inventory, MachineGuiParameters guiParams,
            ProgressBar.Params progressBarParams, MachineLevel level, List<OverclockComponent.Catalyst> overclockCatalysts) {
        super(bep, recipeType, inventory, guiParams, progressBarParams, level);
        this.overclockComponent = new OverclockComponent(overclockCatalysts);

        GunpowderOverclockGui.Params gunpowderOverclockGuiParams = new GunpowderOverclockGui.Params(progressBarParams.renderX(),
                progressBarParams.renderY() + 20);
        registerGuiComponent(new GunpowderOverclockGui(gunpowderOverclockGuiParams, overclockComponent::getTicks));
        this.registerComponents(overclockComponent);
    }

    @Override
    public long consumeFe(long max, Simulation simulation) {
        return SteamHelper.consumeSteamFe(getInventory().getFluidStacks(), max, simulation);
    }

    @Override
    public MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData();
        orientation.writeModelData(data);
        data.isActive = isActiveComponent.isActive;
        return data;
    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        var result = super.useItemOn(player, hand, face);
        if (!result.consumesAction()) {
            return overclockComponent.onUse(this, player, hand);
        }
        return result;
    }

    /**
     * Steam machines get their speed from the gunpowder overclock, since their FE supply is fixed by the boiler.
     */
    @Override
    public int getSpeedBonus() {
        return (int) Math.max(1, overclockComponent.getRecipeFe(1));
    }

    public void tick() {
        super.tick();
        overclockComponent.tick(this);
    }

    @Override
    public List<Component> getTooltips(Item.TooltipContext context) {
        return overclockComponent.getTooltips();
    }
}
