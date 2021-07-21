package shadows.apotheosis.deadly;

import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features.Placements;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.gen.BossDungeonFeature;
import shadows.apotheosis.deadly.gen.BossDungeonFeature2;
import shadows.apotheosis.deadly.gen.RogueSpawnerFeature;
import shadows.apotheosis.deadly.gen.TomeTowerFeature;
import shadows.apotheosis.deadly.gen.TroveFeature;

public class DeadlyWorldGen {
	public static final ConfiguredFeature<?, ?> BOSS_DUNGEON = BossDungeonFeature.INSTANCE.configured(IFeatureConfig.NONE).range(128).squared().count(DeadlyConfig.bossDungeonAttempts);
	public static final ConfiguredFeature<?, ?> BOSS_DUNGEON_2 = BossDungeonFeature2.INSTANCE.configured(IFeatureConfig.NONE).range(128).squared().count(DeadlyConfig.bossDungeonAttempts);
	public static final ConfiguredFeature<?, ?> ROGUE_SPAWNER = RogueSpawnerFeature.INSTANCE.configured(IFeatureConfig.NONE).range(256).squared().count(DeadlyConfig.rogueSpawnerAttempts);
	public static final ConfiguredFeature<?, ?> ORE_TROVE = TroveFeature.INSTANCE.configured(IFeatureConfig.NONE).range(64).squared().count(DeadlyConfig.troveAttempts);
	public static final ConfiguredFeature<?, ?> TOME_TOWER = TomeTowerFeature.INSTANCE.configured(IFeatureConfig.NONE).decorated(Placements.HEIGHTMAP_WORLD_SURFACE).chance(DeadlyConfig.tomeTowerChance);

	public static void init() {
		registerAll(BOSS_DUNGEON, BOSS_DUNGEON_2, ROGUE_SPAWNER, ORE_TROVE, TOME_TOWER);
	}

	private static void registerAll(ConfiguredFeature<?, ?>... feats) {
		for (ConfiguredFeature<?, ?> f : feats) {
			WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_FEATURE, f.feature.getRegistryName(), f);
		}
	}

	/**
	 * Self notes on World Generation:
	 * -Placement configs operate right-to-left, which means the last call operates first.
	 * -Everything else operates on the entire stream produced by the outermost config.
	 * -Thus feat.range(x).square().count(y) provides y copies that get randomized in the chunk, with a y level between 0 and x.
	 * -However, feat.count(y).range(x).square() would just produce y copies of the exact same randomized blockpos.
	 * -With no configs you just get the chunk's corner and y=0
	 */
	public static void onBiomeLoad(BiomeLoadingEvent e) {
		if (!DeadlyConfig.BIOME_BLACKLIST.contains(e.getName())) {
			e.getGeneration().addFeature(Decoration.UNDERGROUND_STRUCTURES, BOSS_DUNGEON).addFeature(Decoration.UNDERGROUND_STRUCTURES, BOSS_DUNGEON_2).addFeature(Decoration.UNDERGROUND_STRUCTURES, ROGUE_SPAWNER);
			e.getGeneration().addFeature(Decoration.UNDERGROUND_STRUCTURES, ORE_TROVE);
			if (Apotheosis.enableEnch && DeadlyConfig.tomeTowerChance > 0) e.getGeneration().addFeature(Decoration.SURFACE_STRUCTURES, TOME_TOWER);
		}
	}

}
