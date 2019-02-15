package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import shadows.deadly.DeadlyModule;
import shadows.deadly.config.DeadlyConfig;

public class WorldGenerator implements IWorldGenerator {

	public static final List<WorldFeature> FEATURES = new ArrayList<>();
	public static final BrutalSpawner BRUTAL_SPAWNER = new BrutalSpawner();
	public static final BossFeature BOSS_GENERATOR = new BossFeature();
	public static final SwarmSpawner SWARM_SPAWNER = new SwarmSpawner();
	public static final LongList SUCCESSES = new LongArrayList();

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (!world.isRemote && DeadlyConfig.DIM_WHITELIST.contains(world.provider.getDimension())) WorldGenerator.run(world, new BlockPos(chunkX << 4, 0, chunkZ << 4));
	}

	static Random rand = new Random();

	public static void run(World world, BlockPos pos) {
		for (WorldFeature feature : FEATURES) {
			if (SUCCESSES.contains(pos.toLong())) return;
			feature.generate(world, pos, world.rand);
		}
	}

	/**
	 * Builds a glass pillar from the given location up to layer 127.
	 */
	public static void debugPillar(World world, BlockPos pos) {
		MutableBlockPos mPos = new MutableBlockPos(pos);
		DeadlyModule.LOGGER.info("Marking! " + pos.toString());
		while (mPos.getY() < 127)
			world.setBlockState(mPos.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ()), Blocks.GLASS.getDefaultState());
	}

	public static void init() {
		if (BRUTAL_SPAWNER.isEnabled()) FEATURES.add(BRUTAL_SPAWNER);
		if (SWARM_SPAWNER.isEnabled()) FEATURES.add(SWARM_SPAWNER);
		if (BOSS_GENERATOR.isEnabled()) FEATURES.add(BOSS_GENERATOR);
	}
}