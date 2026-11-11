package aztech.milite.sounds;

import aztech.milite.MI;
import aztech.milite.config.MIServerConfig;
import aztech.milite.machines.MachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

public final class MISounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MI.ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_BARREL_OPEN = register("block/water_barrel/open");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_BARREL_CLOSE = register("block/water_barrel/close");

    private static final Map<String, MachineSound> SOUNDS = new HashMap<>();
    private static final Map<ResourceLocation, String> MACHINE_SOUNDS = Map.ofEntries(
            Map.entry(MI.id("electric_blast_furnace"), "electric_blast_furnace"),
            Map.entry(MI.id("nuclear_reactor"), "fission_reactor"),
            Map.entry(MI.id("stainless_steel_lithography"), "assembler"),
            Map.entry(MI.id("titanium_lithography"), "assembler"),
            Map.entry(MI.id("iridium_lithography"), "assembler"));

    static {
        sound("assembler", 39);
        sound("blast_furnace", 31);
        sound("boiler", 66);
        sound("centrifuge", 39);
        sound("chemical_reactor", 80);
        sound("coke_oven", 34);
        sound("compressor", 30);
        sound("cutting_machine", 68);
        sound("diesel", 9);
        sound("distillation_tower", 109);
        sound("distillery", 71);
        sound("electric_blast_furnace", 61);
        sound("electrolyzer", 7);
        sound("fission_reactor", 60);
        sound("furnace", 38);
        sound("fusion_reactor", 67);
        sound("heat_exchanger", 71);
        sound("implosion_compressor", 70);
        sound("macerator", 16);
        sound("mixer", 27);
        sound("oil_drilling_rig", 29);
        sound("packer", 23);
        sound("polarizer", 29);
        sound("pressurizer", 89);
        sound("quarry", 61);
        sound("replicator", 20);
        sound("turbine", 36);
        sound("unpacker", 23);
        sound("vacuum_freezer", 62);
        sound("wiremill", 45);
    }

    public static void init(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }

    public static void play(MachineBlockEntity machine) {
        var level = machine.getLevel();
        if (level == null || level.isClientSide || !MIServerConfig.INSTANCE.machineSounds.getAsBoolean())
            return;
        var sound = forMachine(machine.guiParams.blockId);
        if (sound == null)
            return;
        long gameTime = level.getGameTime();
        if (gameTime - machine.lastSoundTick < sound.duration())
            return;
        machine.lastSoundTick = gameTime;
        level.playSound(null, machine.getBlockPos(), sound.event().get(), SoundSource.BLOCKS,
                (float) MIServerConfig.INSTANCE.machineSoundVolume.getAsDouble(), 1.0F);
    }

    private static MachineSound forMachine(ResourceLocation machineId) {
        String name = MACHINE_SOUNDS.get(machineId);
        if (name == null) {
            String path = machineId.getPath();
            if (path.endsWith("_diesel_generator")) {
                name = "diesel";
            } else {
                for (var candidate : SOUNDS.keySet()) {
                    if ((path.equals(candidate) || path.endsWith("_" + candidate))
                            && (name == null || candidate.length() > name.length())) {
                        name = candidate;
                    }
                }
            }
        }
        return name == null ? null : SOUNDS.get(name);
    }

    private static void sound(String name, int duration) {
        var path = "machine/" + name;
        var event = register(path);
        SOUNDS.put(name, new MachineSound(event, duration));
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(MI.id(path)));
    }

    public record MachineSound(DeferredHolder<SoundEvent, SoundEvent> event, int duration) {
    }
}
