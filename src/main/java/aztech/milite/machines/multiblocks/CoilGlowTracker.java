package aztech.milite.machines.multiblocks;

import aztech.milite.network.machines.CoilGlowPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Server-owned, transient glow positions, grouped by controller for overlapping structures. */
public final class CoilGlowTracker {
    private static final Map<ServerLevel, Map<BlockPos, Set<BlockPos>>> LEVELS = new IdentityHashMap<>();

    private CoilGlowTracker() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ChunkWatchEvent.Sent.class, event -> {
            CoilGlowPacket packet = snapshot(event.getLevel(), event.getPos());
            if (!packet.positions().isEmpty()) PacketDistributor.sendToPlayer(event.getPlayer(), packet);
        });
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> LEVELS.clear());
    }

    public static void update(ServerLevel level, BlockPos controller, Set<BlockPos> positions) {
        Map<BlockPos, Set<BlockPos>> owners = LEVELS.computeIfAbsent(level, ignored -> new HashMap<>());
        Set<BlockPos> previous = owners.getOrDefault(controller, Set.of());
        if (previous.equals(positions)) {
            if (owners.isEmpty()) LEVELS.remove(level);
            return;
        }
        if (positions.isEmpty()) owners.remove(controller);
        else owners.put(controller.immutable(), Set.copyOf(positions));
        Set<ChunkPos> affected = new HashSet<>();
        previous.forEach(pos -> affected.add(new ChunkPos(pos)));
        positions.forEach(pos -> affected.add(new ChunkPos(pos)));
        for (ChunkPos chunk : affected) {
            PacketDistributor.sendToPlayersTrackingChunk(level, chunk, snapshot(level, chunk));
        }
        if (owners.isEmpty()) LEVELS.remove(level);
    }

    private static CoilGlowPacket snapshot(ServerLevel level, ChunkPos chunk) {
        Set<BlockPos> positions = new HashSet<>();
        for (Set<BlockPos> owner : LEVELS.getOrDefault(level, Map.of()).values()) {
            for (BlockPos pos : owner) {
                if (new ChunkPos(pos).equals(chunk)) positions.add(pos);
            }
        }
        return new CoilGlowPacket(level.dimension().location(), chunk, List.copyOf(positions));
    }
}
