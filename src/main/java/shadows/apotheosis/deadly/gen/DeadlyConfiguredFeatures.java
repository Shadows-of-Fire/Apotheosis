package shadows.apotheosis.deadly.gen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.config.DeadlyConfig;

import java.util.Arrays;

public final class DeadlyConfiguredFeatures {
    private DeadlyConfiguredFeatures() {}


//    public static final ConfiguredFeature<?, ?> BOSS_DUNGEON = register(BossDungeonFeature.INSTANCE.configured(IFeatureConfig.NONE).range(80).squared().count(DeadlyConfig.bossDungeonAttempts), "boss_dungeon");
//    public static final ConfiguredFeature<?, ?> BOSS_DUNGEON_2 = register(BossDungeonFeature2.INSTANCE.configured(IFeatureConfig.NONE).range(80).squared().count(DeadlyConfig.bossDungeonAttempts), "boss_dungeon_2");
//    public static final ConfiguredFeature<?, ?> ROGUE_SPAWNER = register(RogueSpawnerFeature.INSTANCE.configured(IFeatureConfig.NONE).range(70).squared().count(DeadlyConfig.rogueSpawnerAttempts), "rogue_spawner");
//    public static final ConfiguredFeature<?, ?> ORE_TROVE = register(TroveFeature.INSTANCE.configured(IFeatureConfig.NONE).range(64).squared().count(DeadlyConfig.troveAttempts), "ore_trove");


    public static final ConfiguredFeature<?, ?> TOME_TOWER_CONFIGURED = new ConfiguredFeature<>(DeadlyFeatures.TOME_TOWER.get(), NoneFeatureConfiguration.INSTANCE);
    public static final PlacedFeature TOME_TOWER_PLACED = new PlacedFeature(Holder.direct(TOME_TOWER_CONFIGURED),  Arrays.asList(RarityFilter.onAverageOnceEvery(DeadlyConfig.tomeTowerChance), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE));


    public static void registerConfiguredFeatures() {
        Registry<ConfiguredFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_FEATURE;

        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "tome_tower"), TOME_TOWER_CONFIGURED);
    }

    public static void registerPlacedFeatures() {
        Registry<PlacedFeature> registry = BuiltinRegistries.PLACED_FEATURE;

        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "tome_tower"), TOME_TOWER_PLACED);
    }
}