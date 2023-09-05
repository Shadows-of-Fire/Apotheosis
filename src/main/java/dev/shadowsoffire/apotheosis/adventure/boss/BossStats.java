package dev.shadowsoffire.apotheosis.adventure.boss;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;

/**
 * Boss Stats, aka everything that a boss might need to buff itself.
 *
 * @param enchantChance Specifies the chance that boss items (aside from the affix item) are enchanted.
 * @param enchLevels    Array of enchantment levels to use for the boss's items. Order is {<Generic with EnchModule>, <Generic without>, <Affix with>, <Affix
 *                      without>}. Must have four entries.
 * @param effects       List of effects that could be applied to this boss. May be empty, but may not be null.
 * @param modifiers     List of attribute modifiers to apply to this boss when spawned. May be empty, but may not be null.
 */
public record BossStats(float enchantChance, int[] enchLevels, List<ChancedEffectInstance> effects, List<RandomAttributeModifier> modifiers) {

    public static final Codec<BossStats> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.FLOAT.fieldOf("enchant_chance").forGetter(BossStats::enchantChance),
            Codec.INT.listOf().xmap(l -> l.stream().mapToInt(Integer::intValue).toArray(), arr -> Arrays.stream(arr).boxed().toList()).fieldOf("enchantment_levels").forGetter(BossStats::enchLevels),
            ChancedEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(BossStats::effects),
            RandomAttributeModifier.CODEC.listOf().fieldOf("attribute_modifiers").forGetter(BossStats::modifiers))
        .apply(inst, BossStats::new));

}
