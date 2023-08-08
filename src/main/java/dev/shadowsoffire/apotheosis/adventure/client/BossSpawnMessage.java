package dev.shadowsoffire.apotheosis.adventure.client;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableInt;

import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.network.MessageProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;

public class BossSpawnMessage {

    private final BlockPos pos;
    private final int color;

    public BossSpawnMessage(BlockPos pos, int color) {
        this.pos = pos;
        this.color = color;
    }

    public static class Provider implements MessageProvider<BossSpawnMessage> {

        @Override
        public Class<?> getMsgClass() {
            return BossSpawnMessage.class;
        }

        @Override
        public void write(BossSpawnMessage msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeInt(msg.color);
        }

        @Override
        public BossSpawnMessage read(FriendlyByteBuf buf) {
            return new BossSpawnMessage(buf.readBlockPos(), buf.readInt());
        }

        @Override
        public void handle(BossSpawnMessage msg, Supplier<Context> ctx) {
            MessageHelper.handlePacket(() -> {
                AdventureModuleClient.onBossSpawn(msg.pos, toFloats(msg.color));
            }, ctx);
        }

        @Override
        public Optional<NetworkDirection> getNetworkDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_CLIENT);
        }

        private static float[] toFloats(int color) {
            float[] arr = new float[3];
            arr[0] = (color >> 16 & 0xFF) / 255F;
            arr[1] = (color >> 8 & 0xFF) / 255F;
            arr[2] = (color & 0xFF) / 255F;
            return arr;
        }

    }

    public static record BossSpawnData(BlockPos pos, float[] color, MutableInt ticks) {

    }

}
