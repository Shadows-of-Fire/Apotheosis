package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.safe.GemSafeMenu;
import dev.shadowsoffire.apotheosis.socket.gem.safe.GemSafeScreen;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Communicates the selected gem in the Gem Safe between client and server.
 * <p>
 * When the client makes a selection, the client sends this payload to the server. The server will reply with the same payload if it accepts the change.
 */
public record GemSafePayload(DynamicHolder<Gem> gem) implements CustomPacketPayload {

    public static final Type<GemSafePayload> TYPE = new Type<>(Apotheosis.loc("gem_safe_select"));

    public static final StreamCodec<ByteBuf, GemSafePayload> CODEC = GemRegistry.INSTANCE.holderStreamCodec().map(GemSafePayload::new, GemSafePayload::gem);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<GemSafePayload> {

        @Override
        public Type<GemSafePayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, GemSafePayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handle(GemSafePayload msg, IPayloadContext ctx) {
            if (ctx.flow().isClientbound()) {
                GemSafeScreen.handleSelectedGem(msg.gem());
            }
            else {
                if (ctx.player().containerMenu instanceof GemSafeMenu menu) {
                    menu.setSelectedGem(msg.gem());
                }
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
            return "1";
        }

    }

}
