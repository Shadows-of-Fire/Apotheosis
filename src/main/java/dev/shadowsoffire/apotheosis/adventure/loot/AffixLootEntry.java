package dev.shadowsoffire.apotheosis.adventure.loot;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public final class AffixLootEntry implements CodecProvider<AffixLootEntry>, ILuckyWeighted, IDimensional, RarityClamp, IStaged {

    public static final Codec<AffixLootEntry> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
            PlaceboCodecs.nullableField(Codec.floatRange(0, Float.MAX_VALUE), "quality", 0F).forGetter(ILuckyWeighted::getQuality),
            ItemAdapter.CODEC.fieldOf("stack").forGetter(a -> a.stack),
            PlaceboCodecs.setOf(ResourceLocation.CODEC).fieldOf("dimensions").forGetter(a -> a.dimensions),
            RarityRegistry.INSTANCE.holderCodec().fieldOf("min_rarity").forGetter(a -> a.minRarity),
            RarityRegistry.INSTANCE.holderCodec().fieldOf("max_rarity").forGetter(a -> a.maxRarity),
            PlaceboCodecs.nullableField(PlaceboCodecs.setOf(Codec.STRING), "stages").forGetter(a -> Optional.ofNullable(a.stages)))
        .apply(inst, AffixLootEntry::new));

    protected final int weight;
    protected final float quality;
    protected final ItemStack stack;
    protected final Set<ResourceLocation> dimensions;
    protected final DynamicHolder<LootRarity> minRarity;
    protected final DynamicHolder<LootRarity> maxRarity;
    protected final @Nullable Set<String> stages;

    public AffixLootEntry(int weight, float quality, ItemStack stack, Set<ResourceLocation> dimensions, DynamicHolder<LootRarity> min, DynamicHolder<LootRarity> max, Optional<Set<String>> stages) {
        this.weight = weight;
        this.quality = quality;
        this.stack = Objects.requireNonNull(stack);
        this.dimensions = dimensions;
        this.minRarity = min;
        this.maxRarity = max;
        this.stages = stages.orElse(null);
    }

    public AffixLootEntry(int weight, float quality, ItemStack stack, Set<ResourceLocation> dimensions, DynamicHolder<LootRarity> min, DynamicHolder<LootRarity> max) {
        this(weight, quality, stack, dimensions, min, max, Optional.empty());
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public float getQuality() {
        return this.quality;
    }

    public ItemStack getStack() {
        return this.stack.copy();
    }

    @Override
    public Set<ResourceLocation> getDimensions() {
        return this.dimensions;
    }

    @Override
    public LootRarity getMinRarity() {
        return this.minRarity.get();
    }

    @Override
    public LootRarity getMaxRarity() {
        return this.maxRarity.get();
    }

    public LootCategory getType() {
        return LootCategory.forItem(this.stack);
    }

    @Override
    public Set<String> getStages() {
        return this.stages;
    }

    @Override
    public Codec<? extends AffixLootEntry> getCodec() {
        return CODEC;
    }

}
