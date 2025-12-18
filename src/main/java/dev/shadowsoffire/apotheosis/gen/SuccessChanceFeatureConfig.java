package dev.shadowsoffire.apotheosis.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * @param successChance The chance (from 0 to 1) that an attempted placement will actually attempt to place the feature.
 */
public record SuccessChanceFeatureConfig(float successChance) implements FeatureConfiguration {

    public static final Codec<SuccessChanceFeatureConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.floatRange(0, 1).fieldOf("success_chance").forGetter(SuccessChanceFeatureConfig::successChance))
        .apply(inst, SuccessChanceFeatureConfig::new));
}
