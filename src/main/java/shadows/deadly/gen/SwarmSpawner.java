package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSandStone.EnumType;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import shadows.deadly.DeadlyLoot;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.config.DeadlyConstants;
import shadows.util.ChestBuilder;

public class SwarmSpawner extends WorldFeature {

	public static final ArrayList<SpawnerItem> SWARM_SPAWNERS = new ArrayList<>();

	@Override
	public void generate(World world, int chunkX, int chunkZ, Random rand) {
		if (DeadlyConfig.swarmSpawnerChance <= rand.nextDouble()) return;
		int x = (chunkX << 4) + MathHelper.getInt(rand, 4, 12);
		int z = (chunkZ << 4) + MathHelper.getInt(rand, 4, 12);
		int y = 15 + rand.nextInt(35);
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		for (; y > 10; y--) {
			if (canBePlaced(world, mPos.setPos(x, y, z), rand)) {
				place(world, mPos.setPos(x, y, z), rand);
				WorldGenerator.setSuccess(world.provider.getDimension(), chunkX, chunkZ);
				return;
			}
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos, Random rand) {
		return world.getBlockState(pos.down()).isSideSolid(world, pos, EnumFacing.UP) && WorldGenerator.STONE_TEST.apply(world.getBlockState(pos)) && world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up());
	}

	@Override
	public void place(World world, BlockPos pos, Random rand) {
		ChestBuilder.place(world, rand, pos.down(), rand.nextInt(12) == 0 ? DeadlyLoot.CHEST_VALUABLE : DeadlyLoot.SPAWNER_SWARM);
		WeightedRandom.getRandomItem(rand, SWARM_SPAWNERS).place(world, pos);
		world.setBlockState(pos.up(), Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, EnumType.SMOOTH), 2);
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