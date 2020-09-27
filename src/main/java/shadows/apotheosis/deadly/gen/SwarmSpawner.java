package shadows.apotheosis.deadly.gen;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IServerWorld;
import shadows.apotheosis.deadly.DeadlyLoot;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.config.DeadlyConstants;
import shadows.placebo.util.ChestBuilder;

public class SwarmSpawner extends WorldFeature {

	public static final ArrayList<SpawnerItem> SWARM_SPAWNERS = new ArrayList<>();

	@Override
	public boolean generate(IServerWorld world, int chunkX, int chunkZ, Random rand) {
		if (DeadlyConfig.swarmSpawnerChance <= rand.nextDouble()) return false;
		int x = (chunkX << 4) + MathHelper.nextInt(rand, 4, 12);
		int z = (chunkZ << 4) + MathHelper.nextInt(rand, 4, 12);
		int y = 15 + rand.nextInt(35);
		BlockPos.Mutable mPos = new BlockPos.Mutable(x, y, z);
		for (; y > 10; y--) {
			if (canBePlaced(world, mPos.setPos(x, y, z), rand)) {
				place(world, mPos.setPos(x, y, z), rand);
				WorldGenerator.setSuccess(world.getDimensionType(), chunkX, chunkZ);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canBePlaced(IServerWorld world, BlockPos pos, Random rand) {
		BlockState state = world.getBlockState(pos);
		BlockState downState = world.getBlockState(pos.down());
		BlockState upState = world.getBlockState(pos.up());
		return WorldGenerator.STONE_TEST.test(downState) && upState.isAir(world, pos.up()) && (state.isAir(world, pos) || WorldGenerator.STONE_TEST.test(state));
	}

	@Override
	public void place(IServerWorld world, BlockPos pos, Random rand) {
		ChestBuilder.place(world, rand, pos.down(), rand.nextInt(12) == 0 ? DeadlyLoot.CHEST_VALUABLE : DeadlyLoot.SPAWNER_SWARM);
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