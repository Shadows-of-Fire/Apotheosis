package shadows.apotheosis.deadly.gen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.config.DeadlyConfig;

import java.util.List;

public final class DeadlyConfiguredFeatures {
    private DeadlyConfiguredFeatures() {}


//    public static final ConfiguredFeature<?, ?> BOSS_DUNGEON = register(BossDungeonFeature.INSTANCE.configured(IFeatureConfig.NONE).range(80).squared().count(DeadlyConfig.bossDungeonAttempts), "boss_dungeon");
//    public static final ConfiguredFeature<?, ?> BOSS_DUNGEON_2 = register(BossDungeonFeature2.INSTANCE.configured(IFeatureConfig.NONE).range(80).squared().count(DeadlyConfig.bossDungeonAttempts), "boss_dungeon_2");
//    public static final ConfiguredFeature<?, ?> ORE_TROVE = register(TroveFeature.INSTANCE.configured(IFeatureConfig.NONE).range(64).squared().count(DeadlyConfig.troveAttempts), "ore_trove");

    public static final ConfiguredFeature<?, ?> ROGUE_SPAWNER_CONFIGURED = new ConfiguredFeature<>(DeadlyFeatures.ROGUE_SPAWNER.get(), NoneFeatureConfiguration.INSTANCE);
    public static final PlacedFeature ROGUE_SPAWNER_PLACED = new PlacedFeature(
            Holder.direct(ROGUE_SPAWNER_CONFIGURED),
            List.of(InSquarePlacement.spread(),
                    CountPlacement.of(DeadlyConfig.rogueSpawnerAttempts),
//                    SurfaceRelativeThresholdFilter.of(Heightmap.Types.WORLD_SURFACE, -50, -5),
                    HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(20), VerticalAnchor.belowTop(20))));

    public static final ConfiguredFeature<?, ?> TOME_TOWER_CONFIGURED = new ConfiguredFeature<>(DeadlyFeatures.TOME_TOWER.get(), NoneFeatureConfiguration.INSTANCE);
    public static final PlacedFeature TOME_TOWER_PLACED = new PlacedFeature(
            Holder.direct(TOME_TOWER_CONFIGURED),
            List.of(InSquarePlacement.spread(),
                    RarityFilter.onAverageOnceEvery(DeadlyConfig.tomeTowerChance),
                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE));


    public static void registerConfiguredFeatures() {
        Registry<ConfiguredFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_FEATURE;

        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "tome_tower"), TOME_TOWER_CONFIGURED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "rogue_spawner"), ROGUE_SPAWNER_CONFIGURED);
    }

    public static void registerPlacedFeatures() {
        Registry<PlacedFeature> registry = BuiltinRegistries.PLACED_FEATURE;

        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "tome_tower"), TOME_TOWER_PLACED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "rogue_spawner"), ROGUE_SPAWNER_PLACED);
    }
}