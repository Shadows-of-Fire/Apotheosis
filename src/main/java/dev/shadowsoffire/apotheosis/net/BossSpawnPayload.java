package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableInt;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BossSpawnPayload(BlockPos pos, DynamicHolder<LootRarity> rarity) implements CustomPacketPayload {

    public static final Type<BossSpawnPayload> TYPE = new Type<>(Apotheosis.loc("boss_spawn"));

    public static final StreamCodec<ByteBuf, BossSpawnPayload> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BossSpawnPayload::pos,
        RarityRegistry.INSTANCE.holderStreamCodec(), BossSpawnPayload::rarity,
        BossSpawnPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<BossSpawnPayload> {

        @Override
        public Type<BossSpawnPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, BossSpawnPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handle(BossSpawnPayload msg, IPayloadContext ctx) {
            AdventureModuleClient.onBossSpawn(msg.pos, msg.rarity);
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
            return "2";
        }

    }

    public static record BossSpawnData(BlockPos pos, LootRarity rarity, MutableInt ticks) {

    }

}
