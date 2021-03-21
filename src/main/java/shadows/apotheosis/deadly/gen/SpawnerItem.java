package shadows.apotheosis.deadly.gen;

import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import shadows.apotheosis.deadly.config.DeadlyConstants;
import shadows.apotheosis.deadly.gen.WeightedGenerator.WorldFeatureItem;
import shadows.apotheosis.util.SpawnerStats;
import shadows.apotheosis.util.TagBuilder;
import shadows.placebo.util.SpawnerBuilder;

public class SpawnerItem extends WorldFeatureItem {

	protected final SpawnerBuilder spawner;

	public SpawnerItem(SpawnerBuilder spawner, int weight) {
		super(weight);
		this.spawner = spawner;
	}

	@Override
	public void place(IServerWorld world, BlockPos pos, Random rand) {
		world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 2);
		this.spawner.build(world, pos);
	}

	public SpawnerBuilder getSpawner() {
		return this.spawner;
	}

	/**
	 * Parses SpawnerItems with the provided stats and mob pairs.
	 * @param items The destination list for the created items.
	 * @param stats The spawner stats for these spawners.
	 * @param weightMobPairs The weight-entity pairs to use for each spawner item.
	 */
	public static void rebuildItems(List<SpawnerItem> items, SpawnerStats stats, Object2IntMap<ResourceLocation> weightMobPairs) {
		items.clear();
		for (Entry<ResourceLocation> pair : weightMobPairs.object2IntEntrySet()) {
			SpawnerBuilder builder = new SpawnerBuilder();
			builder.setType(pair.getKey());
			if (pair.getKey().equals(DeadlyConstants.RANDOM)) builder = TagBuilder.createMobSpawnerRandom();
			stats.apply(builder);
			TagBuilder.checkForSkeleton(builder.getSpawnData());
			items.add(new SpawnerItem(builder, pair.getIntValue()));
		}
	}
}