package aztech.milite.client.compat.guideme.recipe.item;

import aztech.milite.util.TextHelper;
import guideme.document.interaction.ItemTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MIGuideItemTooltip extends ItemTooltip {
    private final float probability;
    private final boolean input;
    private final boolean reusable;

    public MIGuideItemTooltip(ItemStack stack, float probability, boolean input, boolean reusable) {
        super(stack);
        this.probability = probability;
        this.input = input;
        this.reusable = reusable;
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        List<ClientTooltipComponent> lines = new ArrayList<>(super.getLines());
        if (reusable) {
            lines.add(ClientTooltipComponent.create(TextHelper.getNotConsumedTooltip().getVisualOrderText()));
        } else if (probability != 1) {
            lines.add(ClientTooltipComponent.create(TextHelper.getProbabilityTooltip(probability, input).getVisualOrderText()));
        }
        return lines;
    }
}
