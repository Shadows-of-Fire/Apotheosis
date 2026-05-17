package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
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
        public void handleClient(WorldTierPayload msg, IPayloadContext ctx) {
            WorldTier.setTier(ctx.player(), msg.tier);
        }

        @Override
        public void handleServer(WorldTierPayload msg, IPayloadContext ctx) {
            Player player = ctx.player();
            if (AdventureConfig.enableManualWorldTierChanges) {
                if (WorldTier.isUnlocked(player, msg.tier)) {
                    WorldTier.setTier(player, msg.tier);
                }
            }
            else if (((ServerPlayer) player).getStats().getValue(Stats.CUSTOM.get(Apoth.Stats.WORLD_TIERS_ACTIVATED)) == 0) {
                // The way that we actually track if the player has completed the tutorial is through this stat counter.
                // So even when manual tier activation is disabled, we have to permit this to be sent once and disable the tutorial.
                player.awardStat(Apoth.Stats.WORLD_TIERS_ACTIVATED);
            }
            else {
                // Aside from that, the player is sending fradulent payloads. Deny those.
                ctx.connection().disconnect(Apotheosis.lang("disconnect", "tier_changes_disabled"));
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
