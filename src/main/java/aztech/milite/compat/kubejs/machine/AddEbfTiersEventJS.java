package aztech.milite.compat.kubejs.machine;

import aztech.milite.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.milite.machines.init.MachineLevel;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class AddEbfTiersEventJS implements KubeEvent {
    private final Consumer<ElectricBlastFurnaceBlockEntity.Tier> tierConsumer;

    public AddEbfTiersEventJS(Consumer<ElectricBlastFurnaceBlockEntity.Tier> tierConsumer) {
        this.tierConsumer = tierConsumer;
    }

    /**
     * Adds a coil and recipe level to the electric blast furnace.
     *
     * @param id          the identifier of the coil tier
     * @param englishName the tier's English name
     * @param coilBlockId the coil its structure is built with
     * @param level       the machine level this coil grants, as one of the {@link MachineLevel} names such as
     *                    {@code "titanium"}
     */
    public void add(String id, String englishName, ResourceLocation coilBlockId, String level) {
        tierConsumer.accept(new ElectricBlastFurnaceBlockEntity.Tier(id, englishName, coilBlockId,
                MachineLevel.byName(level)));
    }
}
