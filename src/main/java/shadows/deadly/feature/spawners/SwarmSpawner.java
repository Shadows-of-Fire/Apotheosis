package shadows.deadly.feature.spawners;

import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.feature.SpawnerItem;
import shadows.deadly.feature.WorldFeature;
import shadows.deadly.util.ChestBuilder;
import shadows.deadly.util.DeadlyConstants;
import shadows.placebo.util.PlaceboUtil;

public class SwarmSpawner extends WorldFeature {

	public static final ArrayList<SpawnerItem> SWARM_SPAWNERS = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos) {
		if (DeadlyConfig.swarmSpawnerChance <= world.rand.nextDouble()) return;
		int x = pos.getX() + world.rand.nextInt(16);
		int z = pos.getZ() + world.rand.nextInt(16);
		int y = world.rand.nextInt(40) + 11;
		for (byte state = 0; y > 4; y--) {
			if (world.isBlockNormalCube(new BlockPos(x, y, z), true)) {
				if (state == 0) {
					if (this.canBePlaced(world, new BlockPos(x, y + 1, z))) {
						this.place(world, new BlockPos(x, y + 1, z));
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
	public boolean canBePlaced(World world, BlockPos pos) {
		return world.isAirBlock(pos) && world.isAirBlock(pos.up());
	}

	@Override
	public void place(World world, BlockPos pos) {
		ChestBuilder.place(world, world.rand, pos.down(), ChestBuilder.SPAWNER_SWARM);
		WeightedRandom.getRandomItem(world.rand, SWARM_SPAWNERS).place(world, pos);
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