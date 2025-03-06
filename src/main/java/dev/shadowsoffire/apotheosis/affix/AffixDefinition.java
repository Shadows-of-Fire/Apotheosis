package dev.shadowsoffire.apotheosis.affix;

import java.util.LinkedHashSet;
import java.util.Set;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;

// TODO: Replace AffixType with a data-driven grouping system for more expressive LootRules.
// Maybe tags, but that requires leaving DynamicRegistry and moving to a vanilla Data Registry.
public record AffixDefinition(AffixType type, Set<DynamicHolder<Affix>> exclusiveSet, TieredWeights weights) {

    public static final Codec<AffixDefinition> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(inst -> inst.group(
        AffixType.CODEC.fieldOf("affix_type").forGetter(AffixDefinition::type),
        PlaceboCodecs.setOf(AffixRegistry.INSTANCE.holderCodec()).fieldOf("exclusive_set").forGetter(AffixDefinition::exclusiveSet),
        TieredWeights.CODEC.fieldOf("weights").forGetter(AffixDefinition::weights))
        .apply(inst, AffixDefinition::new)));

    public static Builder builder(AffixType type) {
        return new Builder(type);
    }

    public static class Builder {
        private final AffixType type;
        private final Set<DynamicHolder<Affix>> exclusiveSet = new LinkedHashSet<>();
        private TieredWeights weights;

        public Builder(AffixType type) {
            this.type = type;
        }

        public Builder exclusiveWith(DynamicHolder<Affix> affix) {
            this.exclusiveSet.add(affix);
            return this;
        }

        public Builder weights(TieredWeights weights) {
            this.weights = weights;
            return this;
        }

        public AffixDefinition build() {
            Preconditions.checkNotNull(this.weights);
            return new AffixDefinition(this.type, this.exclusiveSet, this.weights);
        }
    }

}
