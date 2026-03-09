package dev.shadowsoffire.apotheosis.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record LootRarity(TextColor color, Holder<Item> material, TieredWeights weights, List<LootRule> rules, int sortIndex, RarityRenderData renderData, SoundEvent invaderSound) implements CodecProvider<LootRarity>, Weighted {

    public static final Codec<LootRarity> LOAD_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        TextColor.CODEC.fieldOf("color").forGetter(LootRarity::color),
        ItemStack.ITEM_NON_AIR_CODEC.fieldOf("material").forGetter(LootRarity::material),
        TieredWeights.CODEC.fieldOf("weights").forGetter(Weighted::weights),
        LootRule.CODEC.listOf().fieldOf("rules").forGetter(LootRarity::rules),
        Codec.intRange(0, 2000).optionalFieldOf("sort_index", 1000).forGetter(LootRarity::sortIndex),
        RarityRenderData.CODEC.optionalFieldOf("render_data", RarityRenderData.DEFAULT).forGetter(LootRarity::renderData),
        BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("invader_sound", SoundEvents.END_PORTAL_SPAWN).forGetter(LootRarity::invaderSound))
        .apply(inst, LootRarity::new));

    /**
     * Direct resolution codec. Only for use in other datapack objects which load after the {@link RarityRegistry}.
     */
    public static final Codec<LootRarity> CODEC = Codec.lazyInitialized(() -> RarityRegistry.INSTANCE.holderCodec().xmap(DynamicHolder::get, RarityRegistry.INSTANCE::holder));

    public Item getMaterial() {
        return this.material.value();
    }

    public List<LootRule> getRules(LootCategory category) {
        RarityOverride overrides = RarityOverrideRegistry.INSTANCE.getOverride(category);
        if (overrides != null && overrides.hasRules(this)) {
            return overrides.getRules(this);
        }

        return this.rules;
    }

    public MutableComponent toComponent() {
        return Component.translatable("rarity." + RarityRegistry.INSTANCE.getKey(this)).withStyle(Style.EMPTY.withColor(this.color));
    }

    @Override
    public String toString() {
        return "LootRarity{" + RarityRegistry.INSTANCE.getKey(this) + "}";
    }

    @Override
    public Codec<LootRarity> getCodec() {
        return LOAD_CODEC;
    }

    @Nullable
    public static LootRarity random(GenContext ctx) {
        return RarityRegistry.INSTANCE.getRandomItem(ctx);
    }

    public static LootRarity random(GenContext ctx, Set<LootRarity> pool) {
        return RarityRegistry.INSTANCE.getRandomItem(ctx, pool);
    }

    public static LootRarity randomFromHolders(GenContext ctx, Set<DynamicHolder<LootRarity>> pool) {
        return RarityRegistry.INSTANCE.getRandomItemFromHolders(ctx, pool);
    }

    public static <T> Codec<Map<LootRarity, T>> mapCodec(Codec<T> codec) {
        return Codec.unboundedMap(LootRarity.CODEC, codec);
    }

    public static Builder builder(TextColor color, Holder<Item> material) {
        return new Builder(color, material);
    }

    public static class Builder {

        private final TextColor color;
        private final Holder<Item> material;
        private TieredWeights weights;
        private final List<LootRule> rules = new ArrayList<>();
        private int index = 1000;
        private RarityRenderData renderData = RarityRenderData.DEFAULT;
        private SoundEvent invaderSound = SoundEvents.END_PORTAL_SPAWN;

        public Builder(TextColor color, Holder<Item> material) {
            this.color = color;
            this.material = material;
        }

        public Builder weights(TieredWeights.Builder builder) {
            this.weights = builder.build();
            return this;
        }

        public Builder rule(LootRule rule) {
            this.rules.add(rule);
            return this;
        }

        public Builder sortIndex(int index) {
            this.index = index;
            return this;
        }

        public Builder renderData(UnaryOperator<RarityRenderData.Builder> config) {
            this.renderData = config.apply(new RarityRenderData.Builder()).build();
            return this;
        }

        public Builder invaderSound(SoundEvent sound) {
            this.invaderSound = sound;
            return this;
        }

        public LootRarity build() {
            Preconditions.checkNotNull(this.weights);
            Preconditions.checkArgument(this.rules.size() > 0);
            return new LootRarity(this.color, this.material, this.weights, this.rules, this.index, this.renderData, this.invaderSound);
        }

    }

}
