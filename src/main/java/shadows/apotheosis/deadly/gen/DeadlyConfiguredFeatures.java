package shadows.apotheosis.deadly.gen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.config.DeadlyConfig;

import java.util.List;

public final class DeadlyConfiguredFeatures {
    private DeadlyConfiguredFeatures() {}


//    public static final ConfiguredFeature<?, ?> ORE_TROVE = register(TroveFeature.INSTANCE.configured(IFeatureConfig.NONE).range(64).squared().count(DeadlyConfig.troveAttempts), "ore_trove");

    public static final ConfiguredFeature<?, ?> ORE_TROVE_CONFIGURED = new ConfiguredFeature<>(DeadlyFeatures.ORE_TROVE.get(), NoneFeatureConfiguration.INSTANCE);
    public static final PlacedFeature ORE_TROVE_PLACED = new PlacedFeature(
            Holder.direct(ORE_TROVE_CONFIGURED),
            List.of(InSquarePlacement.spread(),
                    CountPlacement.of(DeadlyConfig.troveAttempts),
                    HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(20), VerticalAnchor.absolute(64))
            )
    );

    public static final ConfiguredFeature<?, ?> BOSS_DUNGEON_CONFIGURED = new ConfiguredFeature<>(DeadlyFeatures.BOSS_DUNGEON.get(), NoneFeatureConfiguration.INSTANCE);
    public static final PlacedFeature BOSS_DUNGEON_PLACED = new PlacedFeature(
            Holder.direct(BOSS_DUNGEON_CONFIGURED),
            List.of(InSquarePlacement.spread(),
                    CountPlacement.of(DeadlyConfig.bossDungeonAttempts),
                    HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(20), VerticalAnchor.belowTop(20))
            )
    );

    public static final ConfiguredFeature<?, ?> BOSS_DUNGEON_2_CONFIGURED = new ConfiguredFeature<>(DeadlyFeatures.BOSS_DUNGEON_2.get(), NoneFeatureConfiguration.INSTANCE);
    public static final PlacedFeature BOSS_DUNGEON_2_PLACED = new PlacedFeature(
            Holder.direct(BOSS_DUNGEON_2_CONFIGURED),
            List.of(InSquarePlacement.spread(),
                    CountPlacement.of(DeadlyConfig.bossDungeon2Attempts),
                    HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(20), VerticalAnchor.belowTop(20))
            )
    );

    public static final ConfiguredFeature<?, ?> ROGUE_SPAWNER_CONFIGURED = new ConfiguredFeature<>(DeadlyFeatures.ROGUE_SPAWNER.get(), NoneFeatureConfiguration.INSTANCE);
    public static final PlacedFeature ROGUE_SPAWNER_PLACED = new PlacedFeature(
            Holder.direct(ROGUE_SPAWNER_CONFIGURED),
            List.of(InSquarePlacement.spread(),
                    CountPlacement.of(DeadlyConfig.rogueSpawnerAttempts),
                    SurfaceRelativeThresholdFilter.of(Heightmap.Types.WORLD_SURFACE, -100, -5),
                    HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(20), VerticalAnchor.belowTop(20))
            )
    );

    public static final ConfiguredFeature<?, ?> TOME_TOWER_CONFIGURED = new ConfiguredFeature<>(DeadlyFeatures.TOME_TOWER.get(), NoneFeatureConfiguration.INSTANCE);
    public static final PlacedFeature TOME_TOWER_PLACED = new PlacedFeature(
            Holder.direct(TOME_TOWER_CONFIGURED),
            List.of(InSquarePlacement.spread(),
                    RarityFilter.onAverageOnceEvery(DeadlyConfig.tomeTowerChance),
                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE
            )
    );


    public static void registerConfiguredFeatures() {
        Registry<ConfiguredFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_FEATURE;

        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "tome_tower"), TOME_TOWER_CONFIGURED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "rogue_spawner"), ROGUE_SPAWNER_CONFIGURED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "boss_dungeon"), BOSS_DUNGEON_CONFIGURED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "boss_dungeon_2"), BOSS_DUNGEON_2_CONFIGURED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "ore_trove"), ORE_TROVE_CONFIGURED);
    }

    public static void registerPlacedFeatures() {
        Registry<PlacedFeature> registry = BuiltinRegistries.PLACED_FEATURE;

        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "tome_tower"), TOME_TOWER_PLACED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "rogue_spawner"), ROGUE_SPAWNER_PLACED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "boss_dungeon"), BOSS_DUNGEON_PLACED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "boss_dungeon_2"), BOSS_DUNGEON_2_PLACED);
        Registry.register(registry, new ResourceLocation(Apotheosis.MODID, "ore_trove"), ORE_TROVE_PLACED);
    }
}