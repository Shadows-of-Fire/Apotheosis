package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * Boss Stats, aka everything that a boss might need to buff itself.
 *
 * @param enchantChance Specifies the chance that boss items (aside from the affix item) are enchanted.
 * @param enchLevels    The enchantment levels to use for the boss's items.
 * @param effects       List of effects that could be applied to this boss. May be empty, but may not be null.
 * @param modifiers     List of attribute modifiers to apply to this boss when spawned. May be empty, but may not be null.
 */
public record BossStats(float enchantChance, EnchantmentLevels enchLevels, List<ChancedEffectInstance> effects, List<RandomAttributeModifier> modifiers) {

    public static final Identifier MODIFIER_BASE = Apotheosis.loc("boss_stats");

    public static final Codec<BossStats> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.FLOAT.fieldOf("enchant_chance").forGetter(BossStats::enchantChance),
            EnchantmentLevels.CODEC.fieldOf("enchantment_levels").forGetter(BossStats::enchLevels),
            ChancedEffectInstance.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(BossStats::effects),
            RandomAttributeModifier.generatedCodec(MODIFIER_BASE).listOf().optionalFieldOf("attribute_modifiers", List.of()).forGetter(BossStats::modifiers))
        .apply(inst, BossStats::new));

    /**
     * Enchantment levels for boss equipment.
     *
     * @param primary   The enchantment level to use for the primary (affixed) item.
     * @param secondary The enchantment level to use for all other items.
     */
    public static record EnchantmentLevels(int primary, int secondary) {

        public static final Codec<EnchantmentLevels> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.INT.fieldOf("primary").forGetter(EnchantmentLevels::primary),
                Codec.INT.fieldOf("secondary").forGetter(EnchantmentLevels::secondary))
            .apply(inst, EnchantmentLevels::new));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float enchantChance = 0F;
        private EnchantmentLevels enchLevels;
        private List<ChancedEffectInstance> effects = new ArrayList<>();
        private List<RandomAttributeModifier> modifiers = new ArrayList<>();

        public Builder enchantChance(float enchantChance) {
            this.enchantChance = enchantChance;
            return this;
        }

        public Builder enchLevels(int primary, int secondary) {
            this.enchLevels = new EnchantmentLevels(primary, secondary);
            return this;
        }

        public Builder effect(ChancedEffectInstance effect) {
            this.effects.add(effect);
            return this;
        }

        public Builder effect(float chance, Holder<MobEffect> effect) {
            return this.effect(chance, effect, StepFunction.constant(1));
        }

        public Builder effect(float chance, Holder<MobEffect> effect, StepFunction amplifier) {
            return this.effect(new ChancedEffectInstance(chance, effect, amplifier, true, false));
        }

        public Builder modifier(Holder<Attribute> attribute, Operation operation, float min, float max) {
            return this.modifier(attribute, operation, StepFunction.fromBounds(min, max));
        }

        public Builder modifier(Holder<Attribute> attribute, Operation operation, StepFunction value) {
            this.modifiers.add(RandomAttributeModifier.generated(attribute, operation, value, MODIFIER_BASE));
            return this;
        }

        public BossStats build() {
            if (this.enchLevels == null) {
                throw new IllegalStateException("EnchantmentLevels must be set");
            }
            return new BossStats(this.enchantChance, this.enchLevels, this.effects, this.modifiers);
        }
    }

}
