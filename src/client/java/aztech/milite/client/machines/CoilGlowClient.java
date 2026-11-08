package aztech.milite.client.machines;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CoilGlowClient {
    private static volatile Map<ChunkPos, Set<BlockPos>> chunks = Map.of();

    private CoilGlowClient() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ChunkEvent.Unload.class, event -> {
            if (event.getLevel().isClientSide()) replace(event.getChunk().getPos(), List.of());
        });
        NeoForge.EVENT_BUS.addListener(LevelEvent.Unload.class, event -> {
            if (event.getLevel().isClientSide()) chunks = Map.of();
        });
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> chunks = Map.of());
    }

    public static boolean active(BlockPos pos) {
        return chunks.getOrDefault(new ChunkPos(pos), Set.of()).contains(pos);
    }

    public static void replace(ChunkPos chunk, List<BlockPos> positions) {
        Set<BlockPos> next = Set.copyOf(positions);
        Set<BlockPos> previous = chunks.getOrDefault(chunk, Set.of());
        if (previous.equals(next)) return;
        Map<ChunkPos, Set<BlockPos>> copy = new HashMap<>(chunks);
        if (next.isEmpty()) copy.remove(chunk);
        else copy.put(chunk, next);
        chunks = Map.copyOf(copy);
        Set<SectionPos> sections = new HashSet<>();
        previous.forEach(pos -> sections.add(SectionPos.of(pos)));
        next.forEach(pos -> sections.add(SectionPos.of(pos)));
        var renderer = Minecraft.getInstance().levelRenderer;
        sections.forEach(section -> renderer.setSectionDirtyWithNeighbors(section.x(), section.y(), section.z()));
    }
}
