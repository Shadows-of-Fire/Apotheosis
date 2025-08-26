package dev.shadowsoffire.apotheosis.tiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Predicates;

import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;

public abstract class TieredDynamicRegistry<V extends CodecProvider<? super V> & Weighted> extends DynamicRegistry<V> {

    public TieredDynamicRegistry(Logger logger, String path, boolean synced, boolean subtypes) {
        super(logger, path, synced, subtypes);
    }

    /**
     * Gets a random item from the registry, re-calculating the weights based on luck.
     * <p>
     * Returns null if all elements have an effective weight of zero, or if the registry is empty.
     */
    @Nullable
    public V getRandomItem(GenContext ctx) {
        return this.getRandomItem(ctx, Predicates.alwaysTrue());
    }

    /**
     * Gets a random item from the registry, re-calculating the weights based on luck and omitting items based on a filter.
     * <p>
     * Returns null if all elements have an effective weight of zero, if all elements are filtered out, or if the registry is empty.
     */
    @Nullable
    @SafeVarargs
    public final V getRandomItem(GenContext ctx, Predicate<? super V>... filters) {
        List<Wrapper<V>> list = new ArrayList<>(this.registry.size());
        var stream = this.registry.values().stream();
        for (Predicate<? super V> filter : filters) {
            stream = stream.filter(filter);
        }
        stream.map(l -> l.<V>wrap(ctx.tier(), ctx.luck())).forEach(list::add);
        return WeightedRandom.getRandomItem(ctx.rand(), list).map(Wrapper::data).orElse(null);
    }

    /**
     * Returns a random element from the registry, filtering the available items to only those in the given pool.
     * <p>
     * If the pool is empty, the entire registry is used.
     * <p>
     * As opposed to {@link #getRandomItem(GenContext, Predicate...)}, this method is not nullable, and will always return an item from the pool or the registry.
     */
    public final V getRandomItem(GenContext ctx, Set<V> pool) {
        V v = this.getRandomItem(ctx, pool.isEmpty() ? s -> true : pool::contains);
        if (v == null) {
            return ApothMiscUtil.getRandomElement(pool.isEmpty() ? this.getValues() : pool, ctx.rand());
        }
        return v;
    }

    /**
     * Similar to {@link #getRandomItem(GenContext, Set)}, but uses a set of {@link DynamicHolder} objects instead of the actual items.
     */
    public final V getRandomItemFromHolders(GenContext ctx, Set<DynamicHolder<V>> pool) {
        return getRandomItem(ctx, pool.stream().filter(DynamicHolder::isBound).map(DynamicHolder::get).collect(Collectors.toSet()));
    }

}
