package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import shadows.deadly.DeadlyLoot;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.config.DeadlyConstants;
import shadows.util.ChestBuilder;

public class SwarmSpawner extends WorldFeature {

	public static final ArrayList<SpawnerItem> SWARM_SPAWNERS = new ArrayList<>();

	@Override
	public boolean generate(IWorld world, int chunkX, int chunkZ, Random rand) {
		if (DeadlyConfig.swarmSpawnerChance <= rand.nextDouble()) return false;
		int x = (chunkX << 4) + MathHelper.nextInt(rand, 4, 12);
		int z = (chunkZ << 4) + MathHelper.nextInt(rand, 4, 12);
		int y = 15 + rand.nextInt(35);
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		for (; y > 10; y--) {
			if (canBePlaced(world, mPos.setPos(x, y, z), rand)) {
				place(world, mPos.setPos(x, y, z), rand);
				WorldGenerator.setSuccess(world.getDimension().getType().getRegistryName(), chunkX, chunkZ);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canBePlaced(IWorld world, BlockPos pos, Random rand) {
		return Block.func_220055_a(world, pos, Direction.UP) && WorldGenerator.STONE_TEST.test(world.getBlockState(pos));
	}

	@Override
	public void place(IWorld world, BlockPos pos, Random rand) {
		ChestBuilder.place((World) world, rand, pos.down(), rand.nextInt(12) == 0 ? DeadlyLoot.CHEST_VALUABLE : DeadlyLoot.SPAWNER_SWARM);
		WeightedRandom.getRandomItem(rand, SWARM_SPAWNERS).place(world, pos);
		world.setBlockState(pos.up(), Blocks.SMOOTH_SANDSTONE.getDefaultState(), 2);
		WorldGenerator.debugLog(pos, "Swarm Spawner");
	}

	@Override
	public boolean isEnabled() {
		return !SWARM_SPAWNERS.isEmpty() && DeadlyConfig.swarmSpawnerChance > 0;
	}

	public static void init() {
		SpawnerItem.addItems(SWARM_SPAWNERS, DeadlyConstants.SWARM_SPAWNER_STATS, DeadlyConfig.SWARM_MOBS);
	}
}