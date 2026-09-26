package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.effect.AttributeToggleAffix;
import dev.shadowsoffire.apotheosis.attachments.AttributeToggles;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload for {@link AttributeToggles} state.
 * <p>
 * When sent to the server, it toggles every attribute provided by the player's worn {@link AttributeToggleAffix}es, ignoring {@link #toggles}.
 * <p>
 * When sent to the client, it replaces the player's toggle state with {@link #toggles}.
 */
public record AttributeTogglesPayload(AttributeToggles toggles) implements CustomPacketPayload {

    public static final Type<AttributeTogglesPayload> TYPE = new Type<>(Apotheosis.loc("attribute_toggles"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeTogglesPayload> CODEC = AttributeToggles.STREAM_CODEC.map(AttributeTogglesPayload::new, AttributeTogglesPayload::toggles);

    public AttributeTogglesPayload() {
        this(AttributeToggles.EMPTY); // When sent to the server, the payload contents are ignored.
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<AttributeTogglesPayload> {

        @Override
        public Type<AttributeTogglesPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, AttributeTogglesPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handle(AttributeTogglesPayload msg, IPayloadContext ctx) {
            if (ctx.flow().isClientbound()) {
                AttributeToggleAffix.setToggles(ctx.player(), msg.toggles);
            }
            else if (ctx.player() instanceof ServerPlayer sp) {
                AttributeToggleAffix.handleToggle(sp);
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
