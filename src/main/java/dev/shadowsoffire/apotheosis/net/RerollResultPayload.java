package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingScreen;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RerollResultPayload(DynamicHolder<Affix> newAffix) implements CustomPacketPayload {

    public static final Type<RerollResultPayload> TYPE = new Type<>(Apotheosis.loc("reroll_result"));

    public static final StreamCodec<ByteBuf, RerollResultPayload> CODEC = AffixRegistry.INSTANCE.holderStreamCodec().map(RerollResultPayload::new, RerollResultPayload::newAffix);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<RerollResultPayload> {

        @Override
        public Type<RerollResultPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RerollResultPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handleClient(RerollResultPayload msg, IPayloadContext ctx) {
            AugmentingScreen.handleRerollResult(msg.newAffix());
        }

        @Override
        public List<ConnectionProtocol> getSupportedProtocols() {
            return List.of(ConnectionProtocol.PLAY);
        }

        @Override
        public Optional<PacketFlow> getFlow() {
            return Optional.of(PacketFlow.CLIENTBOUND);
        }

        @Override
        public String getVersion() {
            return "1";
        }

    }

}
