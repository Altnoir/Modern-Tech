package aztech.milite.client.machines;

import aztech.milite.MI;
import com.altnoir.abysslib.model.api.client.DynamicEmissive;

public final class RelinkCoilGlow {
    private RelinkCoilGlow() {}

    public static void register() {
        for (String name : new String[] {"cupronickel_coil", "kanthal_coil", "superconductor_coil"}) {
            DynamicEmissive.register(MI.id(name), (level, pos, state) -> CoilGlowClient.active(pos));
        }
    }
}
