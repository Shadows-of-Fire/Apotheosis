package dev.shadowsoffire.apotheosis.adventure.loot;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.util.ExtraCodecs;

/**
 * A Rarity clamp imposes min and/or max bounds on rarities.
 * 
 * @see LootRarity#clamp(LootRarity, LootRarity)
 */
public interface RarityClamp {

    public static RarityClamp.Simple EMPTY = new Simple(RarityRegistry.INSTANCE.emptyHolder(), RarityRegistry.INSTANCE.emptyHolder());

    @Nullable
    public LootRarity getMinRarity();

    @Nullable
    public LootRarity getMaxRarity();

    default LootRarity clamp(LootRarity rarity) {
        return rarity.clamp(this.getMinRarity(), this.getMaxRarity());
    }

    public static record Simple(DynamicHolder<LootRarity> min, DynamicHolder<LootRarity> max) implements RarityClamp {

        public static final Codec<Simple> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(inst -> inst
            .group(
                RarityRegistry.INSTANCE.holderCodec().fieldOf("min").forGetter(Simple::min),
                RarityRegistry.INSTANCE.holderCodec().fieldOf("max").forGetter(Simple::max))
            .apply(inst, Simple::new)));

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
