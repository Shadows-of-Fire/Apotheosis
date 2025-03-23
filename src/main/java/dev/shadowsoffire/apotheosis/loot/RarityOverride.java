package dev.shadowsoffire.apotheosis.loot;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.codec.CodecProvider;

/**
 * A Rarity Rule Override is a map of {@link LootRarity} to a list of {@link LootRule} which will override the default rules for that rarity.
 * <p>
 * Each rule override is bound to a specific {@link LootCategory} and will only apply to that category.
 * <p>
 * The target loot category is determined by the path of the override in the data pack.
 * A file at /data/apotheosis/rarity_override/apotheosis/bow.json will apply to the "apotheosis:bow" category.
 * <p>
 * The loot category is included in this object only for posterity.
 */
public record RarityOverride(LootCategory category, Map<LootRarity, List<LootRule>> overrides) implements CodecProvider<RarityOverride> {

    public static final Codec<RarityOverride> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            LootCategory.CODEC.fieldOf("category").forGetter(RarityOverride::category),
            Codec.unboundedMap(LootRarity.CODEC, LootRule.CODEC.listOf()).fieldOf("overrides").forGetter(RarityOverride::overrides))
        .apply(inst, RarityOverride::new));

    @Override
    public Codec<? extends RarityOverride> getCodec() {
        return CODEC;
    }

    public boolean hasRules(LootRarity rarity) {
        return this.overrides.containsKey(rarity);
    }

    @Nullable
    public List<LootRule> getRules(LootRarity rarity) {
        return this.overrides.get(rarity);
    }

    public static Builder builder(LootCategory category) {
        return new Builder(category);
    }

    public static class Builder {

        private final LootCategory category;
        private final Map<LootRarity, List<LootRule>> overrides = new IdentityHashMap<>();

        public Builder(LootCategory category) {
            this.category = category;
        }

        public Builder override(LootRarity rarity, UnaryOperator<RuleListBuilder> config) {
            List<LootRule> list = new ArrayList<>();
            config.apply(new RuleListBuilder(){

                @Override
                public RuleListBuilder rule(LootRule rule) {
                    list.add(rule);
                    return this;
                }

            });
            this.overrides.put(rarity, list);
            return this;
        }

        public RarityOverride build() {
            return new RarityOverride(this.category, this.overrides);
        }

        public static interface RuleListBuilder {
            RuleListBuilder rule(LootRule rule);
        }
    }

}
