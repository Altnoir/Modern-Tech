package aztech.milite.network.machines;

import aztech.milite.MICommonProxy;
import aztech.milite.network.BasePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public record CoilGlowPacket(ResourceLocation dimension, ChunkPos chunk, List<BlockPos> positions) implements BasePacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, CoilGlowPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeResourceLocation(packet.dimension);
                buf.writeInt(packet.chunk.x);
                buf.writeInt(packet.chunk.z);
                buf.writeVarInt(packet.positions.size());
                packet.positions.forEach(buf::writeBlockPos);
            }, buf -> {
                ResourceLocation dimension = buf.readResourceLocation();
                ChunkPos chunk = new ChunkPos(buf.readInt(), buf.readInt());
                int count = buf.readVarInt();
                if (count < 0 || count > 65536) {
                    throw new IllegalArgumentException("Invalid coil glow position count: " + count);
                }
                List<BlockPos> positions = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    BlockPos pos = buf.readBlockPos();
                    if (!new ChunkPos(pos).equals(chunk)) {
                        throw new IllegalArgumentException("Coil glow position outside chunk");
                    }
                    positions.add(pos);
                }
                return new CoilGlowPacket(dimension, chunk, List.copyOf(positions));
            });

    @Override
    public void handle(Context ctx) {
        ctx.assertOnClient();
        MICommonProxy.INSTANCE.updateCoilGlow(dimension, chunk, positions);
    }
}
