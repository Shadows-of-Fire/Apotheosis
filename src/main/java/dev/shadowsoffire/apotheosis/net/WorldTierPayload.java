package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WorldTierPayload(WorldTier tier) implements CustomPacketPayload {

    public static final Type<WorldTierPayload> TYPE = new Type<>(Apotheosis.loc("world_tier"));

    public static final StreamCodec<ByteBuf, WorldTierPayload> CODEC = WorldTier.STREAM_CODEC.map(WorldTierPayload::new, WorldTierPayload::tier);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<WorldTierPayload> {

        @Override
        public Type<WorldTierPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, WorldTierPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handle(WorldTierPayload msg, IPayloadContext ctx) {
            Player player = ctx.player();
            if (ctx.flow() == PacketFlow.CLIENTBOUND) {
                WorldTier.setTier(player, msg.tier);
            }
            else {
                if (AdventureConfig.enableManualWorldTierChanges) {
                    if (WorldTier.isUnlocked(player, msg.tier)) {
                        WorldTier.setTier(player, msg.tier);
                    }
                }
                else {
                    ctx.connection().disconnect(Apotheosis.lang("disconnect", "tier_changes_disabled"));
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
