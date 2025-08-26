package dev.shadowsoffire.apotheosis.compat.gateways.tiered_gate;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.gate.Gateway.Size;
import dev.shadowsoffire.gateways.gate.SpawnAlgorithms;
import dev.shadowsoffire.gateways.gate.SpawnAlgorithms.SpawnAlgorithm;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;

/**
 * Basic settings for a {@link TieredGateway} packed into a record for convenience.
 * 
 * @param tier       The {@link WorldTier} of the gate. If the summoner's tier does not match, the gate will not open.
 *                   Additionally, if the summoner's tier changes while the gate is open, it will close.
 * @param size       The {@link Size} of the gate. Controls the bounding box and pearl texture.
 * @param color      The {@link TextColor} of the gate. Used for the gate, boss bar, name, and pearl.
 * @param soundtrack The soundtrack played while the gate is open. Optional.
 * @param lives      The number of lives the player has in the gate. If they run out, the gate closes.
 * @param spawnAlgo  The {@link SpawnAlgorithm} used for placing wave entities in the gate.
 */
public record TieredGateSettings(WorldTier tier, Size size, TextColor color, Holder<SoundEvent> soundtrack, SpawnAlgorithm spawnAlgo) {

    public static final Codec<TieredGateSettings> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            WorldTier.CODEC.fieldOf("tier").forGetter(TieredGateSettings::tier),
            Size.CODEC.fieldOf("size").forGetter(TieredGateSettings::size),
            TextColor.CODEC.fieldOf("color").forGetter(TieredGateSettings::color),
            SoundEvent.CODEC.optionalFieldOf("soundtrack", GatewayObjects.GATE_AMBIENT).forGetter(TieredGateSettings::soundtrack),
            SpawnAlgorithms.CODEC.optionalFieldOf("spawn_algorithm", SpawnAlgorithms.OPEN_FIELD).forGetter(TieredGateSettings::spawnAlgo))
        .apply(inst, TieredGateSettings::new));

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private WorldTier tier = null;
        private Size size = null;
        private TextColor color = TextColor.fromRgb(0xFFFFFF);
        private Holder<SoundEvent> soundtrack = GatewayObjects.GATE_AMBIENT;
        private SpawnAlgorithm spawnAlgo = SpawnAlgorithms.OPEN_FIELD;

        public Builder tier(WorldTier tier) {
            this.tier = tier;
            return this;
        }

        public Builder size(Size size) {
            this.size = size;
            return this;
        }

        public Builder color(TextColor color) {
            this.color = color;
            return this;
        }

        public Builder color(int color) {
            this.color = TextColor.fromRgb(color);
            return this;
        }

        public Builder soundtrack(Holder<SoundEvent> soundtrack) {
            this.soundtrack = soundtrack;
            return this;
        }

        public Builder spawnAlgo(SpawnAlgorithm spawnAlgo) {
            this.spawnAlgo = spawnAlgo;
            return this;
        }

        public TieredGateSettings build() {
            Preconditions.checkNotNull(tier, "Tier must be set");
            Preconditions.checkNotNull(size, "Size must be set");
            return new TieredGateSettings(tier, size, color, soundtrack, spawnAlgo);
        }
    }
}
