package dev.shadowsoffire.apotheosis.tiers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * TieredWeights are item weights unique for each {@link WorldTier}.
 * <p>
 * If a weight is not provided for a specific world tier, then {@link Weight#ZERO} will be used for that tier.
 */
public record TieredWeights(Map<WorldTier, Weight> weights) {

    /**
     * TieredWeights can be represented in json as a single weight object, which applies for all tiers, or a map from WorldTier->Weight.
     * If opting to provide per-tier weights, weights must be provided for each tier.
     */
    public static final MapCodec<TieredWeights> CODEC = Codec.mapEither(Weight.CODEC,
        Codec.simpleMap(WorldTier.CODEC, Weight.CODEC.codec(), StringRepresentable.keys(WorldTier.values())))
        .xmap(e -> e.map(TieredWeights::fillAll, Function.identity()), TieredWeights::toEither)
        .xmap(TieredWeights::new, TieredWeights::weights);

    private static final StreamCodec<ByteBuf, Map<WorldTier, Weight>> MAP_STREAM_CODEC = ByteBufCodecs.map(IdentityHashMap::new, WorldTier.STREAM_CODEC, Weight.STREAM_CODEC, 5);
    public static final StreamCodec<ByteBuf, TieredWeights> STREAM_CODEC = MAP_STREAM_CODEC.map(TieredWeights::new, TieredWeights::weights);

    public static TieredWeights EMPTY = new TieredWeights(Map.of());

    public static record Weight(int weight, float quality) {

        public static Weight ZERO = new Weight(0, 0);

        public static MapCodec<Weight> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.intRange(0, 65536).fieldOf("weight").forGetter(Weight::weight),
            Codec.floatRange(-16, 16).optionalFieldOf("quality", 0F).forGetter(Weight::quality))
            .apply(inst, Weight::new));

        public static StreamCodec<ByteBuf, Weight> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Weight::weight,
            ByteBufCodecs.FLOAT, Weight::quality,
            Weight::new);

        public int getWeight(float luck) {
            return this.weight + Math.round(luck * this.quality);
        }

    }

    public int getWeight(GenContext ctx) {
        return this.getWeight(ctx.tier(), ctx.luck());
    }

    public int getWeight(WorldTier tier, float luck) {
        return this.weights.getOrDefault(tier, Weight.ZERO).getWeight(luck);
    }

    private static Map<WorldTier, Weight> fillAll(Weight weight) {
        return Arrays.stream(WorldTier.values()).collect(Collectors.toMap(Function.identity(), v -> weight));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TieredWeights onlyFor(WorldTier tier, int weight, float quality) {
        return new TieredWeights(Map.of(tier, new Weight(weight, quality)));
    }

    public static TieredWeights forTiersAbove(WorldTier tier, int weight, float quality) {
        Map<WorldTier, Weight> map = new HashMap<>();
        Weight _weight = new Weight(weight, quality);
        for (int i = tier.ordinal(); i < WorldTier.values().length; i++) {
            map.put(WorldTier.BY_ID.apply(i), _weight);
        }
        return new TieredWeights(map);
    }

    public static TieredWeights forAllTiers(int weight, float quality) {
        return new TieredWeights(fillAll(new Weight(weight, quality)));
    }

    /**
     * Alternative version of {@link Weighted#wrap(GenContext)} which is intended for use with {@link Stream#mapMulti(BiConsumer)} that
     * does not create a wrapper if the item's weight is zero.
     * <p>
     * Use this when you do not need to perform fallback handling if the total weight of the system is zero.
     */
    public static <T extends Weighted> BiConsumer<T, Consumer<net.minecraft.util.random.Weighted<T>>> wrapFilter(GenContext ctx) {
        return (obj, sink) -> {
            int weight = obj.weights().getWeight(ctx);
            if (weight > 0) {
                sink.accept(new net.minecraft.util.random.Weighted<>(obj, weight));
            }
        };
    }

    public static <T extends Weighted> BiConsumer<DynamicHolder<T>, Consumer<net.minecraft.util.random.Weighted<T>>> wrapFilterHolders(GenContext ctx) {
        return (holder, sink) -> {
            if (holder.isBound()) {
                int weight = holder.get().weights().getWeight(ctx);
                if (weight > 0) {
                    sink.accept(new net.minecraft.util.random.Weighted<>(holder.get(), weight));
                }
            }
        };
    }

    /**
     * Converts a weight map back into an {@link Either}.
     * <p>
     * If the weight map was created with {@link #forAllTiers(int, float)}, this will reduce to a single {@link Weight}.
     */
    private static Either<Weight, Map<WorldTier, Weight>> toEither(Map<WorldTier, Weight> value) {
        if (value.size() == 5) {
            Weight weight = value.getOrDefault(WorldTier.HAVEN, Weight.ZERO);
            if (value.values().stream().allMatch(weight::equals)) {
                return Either.left(weight);
            }
        }
        return Either.right(value);
    }

    public static interface Weighted {
        TieredWeights weights();
    }

    public static class Builder {
        ImmutableMap.Builder<WorldTier, Weight> mapBuilder = ImmutableMap.builder();

        public Builder() {}

        public Builder with(WorldTier tier, int weight, float quality) {
            return this.with(tier, new Weight(weight, quality));
        }

        public Builder with(WorldTier tier, Weight weight) {
            this.mapBuilder.put(tier, weight);
            return this;
        }

        public TieredWeights build() {
            return new TieredWeights(this.mapBuilder.build());
        }
    }
}
