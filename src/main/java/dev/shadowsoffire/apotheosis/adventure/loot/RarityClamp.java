package dev.shadowsoffire.apotheosis.adventure.loot;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/**
 * A Rarity clamp imposes min and/or max bounds on rarities.
 *
 * @see LootRarity#clamp(LootRarity, LootRarity)
 */
public interface RarityClamp {

    public static RarityClamp.Simple UNCLAMPED = new Simple(RarityRegistry.INSTANCE.emptyHolder(), RarityRegistry.INSTANCE.emptyHolder());

    @Nullable
    public LootRarity getMinRarity();

    @Nullable
    public LootRarity getMaxRarity();

    default LootRarity clamp(LootRarity rarity) {
        return rarity.clamp(this.getMinRarity(), this.getMaxRarity());
    }

    public static record Simple(DynamicHolder<LootRarity> min, DynamicHolder<LootRarity> max) implements RarityClamp {

        public static final Codec<Simple> STRING_CODEC = ExtraCodecs.lazyInitializedCodec(() -> Codec.STRING.xmap(s -> {
            DynamicHolder<LootRarity> rarity = RarityRegistry.INSTANCE.holder(new ResourceLocation(s));
            return new Simple(rarity, rarity);
        }, simple -> simple.min().getId().toString()));

        public static final Codec<Simple> MIN_MAX_CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(inst -> inst
            .group(
                RarityRegistry.INSTANCE.holderCodec().fieldOf("min").forGetter(Simple::min),
                RarityRegistry.INSTANCE.holderCodec().fieldOf("max").forGetter(Simple::max))
            .apply(inst, Simple::new)));

        /**
         * Acceps either a string as the rarity name or an object specifying "min" and "max" rarity names.
         */
        public static final Codec<Simple> CODEC = Codec.either(STRING_CODEC, MIN_MAX_CODEC).xmap(e -> e.map(Function.identity(), Function.identity()), Either::right);

        @Override
        public LootRarity getMinRarity() {
            return this.min.getOptional().orElse(null);
        }

        @Override
        public LootRarity getMaxRarity() {
            return this.max.getOptional().orElse(null);
        }

    }

}
