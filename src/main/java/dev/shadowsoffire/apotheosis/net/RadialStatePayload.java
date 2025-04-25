package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.util.RadialUtil;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialState;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload sent when the radial mining state changes for a given player.
 * <p>
 * When sent to the server, it toggles the radial mining state for the player, ignoring the {@link #state} param.
 * <p>
 * When sent to the client, it sets the radial mining state for the player to the given {@link #state}.
 */
public record RadialStatePayload(RadialState state) implements CustomPacketPayload {

    public static final Type<RadialStatePayload> TYPE = new Type<>(Apotheosis.loc("radial_state_change"));

    public static final StreamCodec<ByteBuf, RadialStatePayload> CODEC = RadialState.STREAM_CODEC.map(RadialStatePayload::new, RadialStatePayload::state);

    public RadialStatePayload() {
        this(RadialState.ENABLED); // When sent to the server, we don't care about the state.
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<RadialStatePayload> {

        @Override
        public Type<RadialStatePayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RadialStatePayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handle(RadialStatePayload msg, IPayloadContext ctx) {
            Player player = ctx.player();
            if (ctx.flow().isClientbound()) {
                RadialState.setState(player, msg.state);
            }
            else {
                RadialUtil.toggleRadialState(player);
            }
        }

        @Override
        public List<ConnectionProtocol> getSupportedProtocols() {
            return List.of(ConnectionProtocol.PLAY);
        }

        @Override
        public Optional<PacketFlow> getFlow() {
            return Optional.empty();
        }

        @Override
        public String getVersion() {
            return "2";
        }

    }

}
