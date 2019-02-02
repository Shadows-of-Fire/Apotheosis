package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.deadly.DeadlyLoot;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.config.DeadlyConstants;
import shadows.placebo.util.PlaceboUtil;
import shadows.util.ChestBuilder;

public class SwarmSpawner extends WorldFeature {

	public static final ArrayList<SpawnerItem> SWARM_SPAWNERS = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos, Random rand) {
		if (DeadlyConfig.swarmSpawnerChance <= rand.nextDouble()) return;
		int x = pos.getX() + rand.nextInt(16);
		int z = pos.getZ() + rand.nextInt(16);
		int y = rand.nextInt(40) + 11;
		for (byte state = 0; y > 4; y--) {
			if (world.isBlockNormalCube(new BlockPos(x, y, z), true)) {
				if (state == 0) {
					if (this.canBePlaced(world, new BlockPos(x, y + 1, z), rand)) {
						this.place(world, new BlockPos(x, y + 1, z), rand);
						WorldGenerator.SUCCESSES.add(pos.toLong());
						return;
					}
					state = -1;
				}
			} else {
				state = 0;
			}
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos, Random rand) {
		return world.isAirBlock(pos) && world.isAirBlock(pos.up());
	}

	@Override
	public void place(World world, BlockPos pos, Random rand) {
		ChestBuilder.place(world, rand, pos.down(), rand.nextInt(12) == 0 ? DeadlyLoot.CHEST_VALUABLE : DeadlyLoot.SPAWNER_SWARM);
		WeightedRandom.getRandomItem(rand, SWARM_SPAWNERS).place(world, pos);
		PlaceboUtil.setBlockWithMeta(world, pos.up(), Blocks.SANDSTONE, 1, 2);
	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.swarmSpawnerChance > 0;
	}

	public static void init() {
		SpawnerItem.addItems(SWARM_SPAWNERS, DeadlyConstants.SWARM_SPAWNER_STATS, DeadlyConfig.swarmWeightedMobs);
	}
}