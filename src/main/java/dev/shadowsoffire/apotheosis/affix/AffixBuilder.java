package dev.shadowsoffire.apotheosis.affix;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.function.TriFunction;
import org.spongepowered.include.com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.placebo.util.StepFunction;

/**
 * Base builder class for {@link Affix affixes}.
 */
@SuppressWarnings("unchecked")
public class AffixBuilder<T extends AffixBuilder<T>> {

    protected AffixDefinition definition;

    public T definition(AffixType type, UnaryOperator<AffixDefinition.Builder> op) {
        this.definition = op.apply(new AffixDefinition.Builder(type)).build();
        return (T) this;
    }

    public T definition(AffixType type, int weight, float quality) {
        this.definition = new AffixDefinition(type, Set.of(), TieredWeights.forAllTiers(weight, quality));
        return (T) this;
    }

    public static <T extends Affix> SimpleAffixBuilder<T> simple(BiFunction<AffixDefinition, Map<LootRarity, StepFunction>, T> factory) {
        return new SimpleAffixBuilder<>(factory);
    }

    public static <T extends Affix> CategorizedAffixBuilder<T> categorized(TriFunction<AffixDefinition, Set<LootCategory>, Map<LootRarity, StepFunction>, T> factory) {
        return new CategorizedAffixBuilder<>(factory);
    }

    public static class ValuedAffixBuilder<T extends ValuedAffixBuilder<T>> extends AffixBuilder<T> {
        protected final Map<LootRarity, StepFunction> values = new LinkedHashMap<>();
        protected float step = 0.01F;

        public T step(float step) {
            this.step = step;
            return (T) this;
        }

        public T value(LootRarity rarity, float min, float max) {
            return this.value(rarity, StepFunction.fromBounds(min, max, this.step));
        }

        public T value(LootRarity rarity, float value) {
            return this.value(rarity, StepFunction.constant(value));
        }

        public T value(LootRarity rarity, StepFunction function) {
            this.values.put(rarity, function);
            return (T) this;
        }

    }

    public static class SimpleAffixBuilder<T extends Affix> extends ValuedAffixBuilder<SimpleAffixBuilder<T>> {

        private final BiFunction<AffixDefinition, Map<LootRarity, StepFunction>, T> factory;

        public SimpleAffixBuilder(BiFunction<AffixDefinition, Map<LootRarity, StepFunction>, T> factory) {
            this.factory = factory;
        }

        public T build() {
            Preconditions.checkArgument(this.definition != null);
            Preconditions.checkArgument(!this.values.isEmpty());
            return this.factory.apply(this.definition, this.values);
        }

    }

    public static class CategorizedAffixBuilder<T extends Affix> extends ValuedAffixBuilder<CategorizedAffixBuilder<T>> {

        private final TriFunction<AffixDefinition, Set<LootCategory>, Map<LootRarity, StepFunction>, T> factory;
        private final Set<LootCategory> categories = new LinkedHashSet<>();

        public CategorizedAffixBuilder(TriFunction<AffixDefinition, Set<LootCategory>, Map<LootRarity, StepFunction>, T> factory) {
            this.factory = factory;
        }

        public CategorizedAffixBuilder<T> category(LootCategory category) {
            this.categories.add(category);
            return this;
        }

        public CategorizedAffixBuilder<T> categories(LootCategory... categories) {
            for (LootCategory category : categories) {
                this.categories.add(category);
            }
            return this;
        }

        public T build() {
            Preconditions.checkArgument(this.definition != null);
            Preconditions.checkArgument(!this.values.isEmpty());
            Preconditions.checkArgument(!this.categories.isEmpty());
            return this.factory.apply(this.definition, this.categories, this.values);
        }

    }

}
