package dev.shadowsoffire.apotheosis.socket.gem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.color.GradientColor;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;

/**
 * Purity represents a fixed set of gem tiers. Gems are expected to have increasingly powerful stats with each purity level.
 */
public enum Purity implements StringRepresentable, TieredWeights.Weighted {
    CRACKED("cracked", 0x808080),
    CHIPPED("chipped", 0x33FF33),
    FLAWED("flawed", 0x5555FF),
    NORMAL("normal", 0xBB00BB),
    FLAWLESS("flawless", 0xED7014),
    PERFECT("perfect", GradientColor.RAINBOW);

    public static final IntFunction<Purity> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<Purity> CODEC = StringRepresentable.fromValues(Purity::values);
    public static final StreamCodec<ByteBuf, Purity> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    public static final Set<Purity> ALL_PURITIES = ApothMiscUtil.linkedSet(CRACKED, CHIPPED, FLAWED, NORMAL, FLAWLESS, PERFECT);

    private final String name;
    private final TextColor color;

    private Purity(String name, TextColor color) {
        this.name = name;
        this.color = color;
    }

    private Purity(String name, int color) {
        this(name, TextColor.fromRgb(color));
    }

    public String getName() {
        return this.name;
    }

    public TextColor getColor() {
        return this.color;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public TieredWeights weights() {
        return PurityWeightsRegistry.getWeights().get(this);
    }

    public Purity next() {
        return this == PERFECT ? this : BY_ID.apply(this.ordinal() + 1);
    }

    public boolean isAtLeast(Purity other) {
        return this.ordinal() >= other.ordinal();
    }

    public MutableComponent toComponent() {
        return Apotheosis.lang("purity", this.getSerializedName()).withStyle(Style.EMPTY.withColor(this.color));
    }

    public static Purity max(Purity p1, Purity p2) {
        return BY_ID.apply(Math.max(p1.ordinal(), p2.ordinal()));
    }

    public static Purity random(GenContext ctx) {
        return random(ctx, ALL_PURITIES);
    }

    /**
     * Returns a random purity from the given pool, or from all purities if the pool is empty.
     * <p>
     * If the effective weights of all given purities are zero, a random purity is selected from the pool uniformly.
     */
    public static Purity random(GenContext ctx, Set<Purity> pool) {
        if (pool.isEmpty()) {
            pool = ALL_PURITIES;
        }

        List<Wrapper<Purity>> list = pool.stream().map(l -> l.<Purity>wrap(ctx.tier(), ctx.luck())).toList();
        return WeightedRandom.getRandomItem(ctx.rand(), list).map(Wrapper::data).orElse(ApothMiscUtil.getRandomElement(pool, ctx.rand()));
    }

    public static <T> MapCodec<Map<Purity, T>> mapCodec(Codec<T> elementCodec) {
        return Codec.simpleMap(Purity.CODEC, elementCodec,
            Keyable.forStrings(() -> Arrays.stream(Purity.values()).map(StringRepresentable::getSerializedName)));
    }
}
