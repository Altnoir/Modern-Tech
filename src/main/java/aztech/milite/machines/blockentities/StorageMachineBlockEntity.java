package aztech.milite.machines.blockentities;

import aztech.milite.machines.BEP;
import net.minecraft.util.Mth;

public class StorageMachineBlockEntity extends AbstractStorageMachineBlockEntity {
    public StorageMachineBlockEntity(BEP bep, String name, long feCapacity) {
        super(bep, name, feCapacity);
    }

    @Override
    protected boolean hasComparatorOutput() {
        return true;
    }

    @Override
    protected int getComparatorOutput() {
        double fillPercentage = (double) energy.getFe() / energy.getCapacity();
        return Mth.floor(fillPercentage * 14) + (energy.getFe() > 0 ? 1 : 0);
    }

}
