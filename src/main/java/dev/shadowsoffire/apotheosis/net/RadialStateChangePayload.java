package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.util.RadialUtil;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RadialStateChangePayload implements CustomPacketPayload {

    public static final RadialStateChangePayload INSTANCE = new RadialStateChangePayload();

    public static final Type<RadialStateChangePayload> TYPE = new Type<>(Apotheosis.loc("radial_state_change"));

    public static final StreamCodec<ByteBuf, RadialStateChangePayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<RadialStateChangePayload> {

        @Override
        public Type<RadialStateChangePayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RadialStateChangePayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handle(RadialStateChangePayload msg, IPayloadContext ctx) {
            Player player = ctx.player();
            RadialUtil.toggleRadialState(player);
        }

        @Override
        public List<ConnectionProtocol> getSupportedProtocols() {
            return List.of(ConnectionProtocol.PLAY);
        }

        @Override
        public Optional<PacketFlow> getFlow() {
            return Optional.of(PacketFlow.SERVERBOUND);
        }

        @Override
        public String getVersion() {
            return "1";
        }

    }

}
