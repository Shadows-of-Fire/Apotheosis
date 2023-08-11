package dev.shadowsoffire.apotheosis.adventure.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;

public record BlacklistModifier(HolderSet<Biome> blacklistedBiomes, Holder<PlacedFeature> feature) implements BiomeModifier {

    public static final Codec<BlacklistModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        Biome.LIST_CODEC.fieldOf("blacklisted_biomes").forGetter(BlacklistModifier::blacklistedBiomes),
        PlacedFeature.CODEC.fieldOf("feature").forGetter(BlacklistModifier::feature))
        .apply(builder, BlacklistModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, Builder builder) {
        if (phase == Phase.ADD && !this.blacklistedBiomes.contains(biome)) {
            builder.getGenerationSettings().addFeature(Decoration.UNDERGROUND_STRUCTURES, this.feature);
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }

}
