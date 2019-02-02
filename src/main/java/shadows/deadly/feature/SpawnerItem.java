package shadows.deadly.feature;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConstants;
import shadows.deadly.config.SpawnerStats;
import shadows.placebo.util.SpawnerBuilder;
import shadows.util.TagBuilder;

public class SpawnerItem extends WorldFeatureItem {

	protected final SpawnerBuilder spawner;

	public SpawnerItem(SpawnerBuilder spawner, int weight) {
		super(weight);
		this.spawner = spawner;
	}

	public static void addItems(List<SpawnerItem> items, SpawnerStats stats, List<Pair<Integer, ResourceLocation>> weightMobPairs) {
		for (Pair<Integer, ResourceLocation> pair : weightMobPairs) {
			SpawnerBuilder builder = new SpawnerBuilder();
			builder.setType(pair.getRight());
			if (pair.getRight().equals(DeadlyConstants.RANDOM)) builder = TagBuilder.createMobSpawnerRandom();
			stats.apply(builder);
			TagBuilder.checkForSkeleton(builder.getSpawnData());
			items.add(new SpawnerItem(builder, pair.getLeft()));
		}
	}

	@Override
	public void place(World world, BlockPos pos) {
		world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
		world.setTileEntity(pos, spawner.build(world, pos));
	}

	public SpawnerBuilder getSpawner() {
		return spawner;
	}
}