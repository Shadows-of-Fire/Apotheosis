package shadows.apotheosis.adventure;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.boss.BossDungeonFeature;
import shadows.apotheosis.adventure.boss.BossDungeonFeature2;
import shadows.apotheosis.adventure.spawner.RogueSpawnerFeature;

public class AdventureGeneration {
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> CF_BOSS_DUNGEON = register(BossDungeonFeature.INSTANCE, "boss_dungeon");
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> CF_BOSS_DUNGEON_2 = register(BossDungeonFeature2.INSTANCE, "boss_dungeon_2");
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> CF_ROGUE_SPAWNER = register(RogueSpawnerFeature.INSTANCE, "rogue_spawner");
	//public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> ORE_TROVE = register(TroveFeature.INSTANCE, "ore_trove");
	//public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> TOME_TOWER = register(TomeTowerFeature.INSTANCE, "tome_tower");

	public static final Holder<PlacedFeature> BOSS_DUNGEON = register("boss_dungeon", CF_BOSS_DUNGEON, CountPlacement.of(AdventureConfig.bossDungeonAttempts), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.top()), BiomeFilter.biome());
	public static final Holder<PlacedFeature> BOSS_DUNGEON_DEEP = register("boss_dungeon_deep", CF_BOSS_DUNGEON, CountPlacement.of(AdventureConfig.bossDungeonAttempts / 2), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(-1)), BiomeFilter.biome());
	public static final Holder<PlacedFeature> BOSS_DUNGEON_2 = register("boss_dungeon_2", CF_BOSS_DUNGEON_2, CountPlacement.of(AdventureConfig.bossDungeon2Attempts), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.top()), BiomeFilter.biome());
	public static final Holder<PlacedFeature> BOSS_DUNGEON_2_DEEP = register("boss_dungeon_2_deep", CF_BOSS_DUNGEON_2, CountPlacement.of(AdventureConfig.bossDungeon2Attempts / 2), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(-1)), BiomeFilter.biome());
	public static final Holder<PlacedFeature> ROGUE_SPAWNER = register("rogue_spawner", CF_ROGUE_SPAWNER, CountPlacement.of(AdventureConfig.rogueSpawnerAttempts), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.top()), BiomeFilter.biome());
	public static final Holder<PlacedFeature> ROGUE_SPAWNER_DEEP = register("rogue_spawner_deep", CF_ROGUE_SPAWNER, CountPlacement.of(AdventureConfig.rogueSpawnerAttempts / 2), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(-1)), BiomeFilter.biome());

	private static final List<Holder<PlacedFeature>> FEATS = ImmutableList.of(BOSS_DUNGEON, BOSS_DUNGEON_DEEP, BOSS_DUNGEON_2, BOSS_DUNGEON_2_DEEP, ROGUE_SPAWNER, ROGUE_SPAWNER_DEEP);

	static Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> register(Feature<NoneFeatureConfiguration> feat, String id) {
		return FeatureUtils.register(Apotheosis.MODID + ":" + id, feat);
	}

	static Holder<PlacedFeature> register(String id, Holder<? extends ConfiguredFeature<?, ?>> feat, PlacementModifier... modifs) {
		return PlacementUtils.register(Apotheosis.MODID + ":" + id, feat, modifs);
	}

	/**
	 * Self notes on World Generation:
	 * -Placement configs operate right-to-left, which means the last call operates first.
	 * -Everything else operates on the entire stream produced by the outermost config.
	 * -Thus feat.range(x).square().count(y) provides y copies that get randomized in the chunk, with a y level between 0 and x.
	 * -However, feat.count(y).range(x).square() would just produce y copies of the exact same randomized blockpos.
	 * -With no configs you just get the chunk's corner and y=0
	 *
	 * Probably not valid in 1.18+
	 */
	@SubscribeEvent
	public static void onBiomeLoad(BiomeLoadingEvent e) {
		if (!AdventureConfig.BIOME_BLACKLIST.contains(e.getName())) {
			for (Holder<PlacedFeature> f : FEATS)
				e.getGeneration().addFeature(Decoration.UNDERGROUND_STRUCTURES, f);
		}
	}

}
