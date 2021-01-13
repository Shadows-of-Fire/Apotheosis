package shadows.apotheosis.deadly.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;

@SuppressWarnings("deprecation")
public class DeadlyFeature extends Feature<NoFeatureConfig> {

	public static final List<WeightedGenerator> GENERATORS = new ArrayList<>();
	public static final BrutalSpawnerGenerator BRUTAL_SPAWNER = new BrutalSpawnerGenerator();
	public static final SwarmSpawnerGenerator SWARM_SPAWNER = new SwarmSpawnerGenerator();
	private static final Map<DimensionType, LongSet> SUCCESSES = new HashMap<>();
	public static final Predicate<BlockState> STONE_TEST = b -> FillerBlockType.BASE_STONE_OVERWORLD.test(b, ThreadLocalRandom.current());

	public static final ConfiguredFeature<?, ?> INSTANCE = new ConfiguredFeature<>(new DeadlyFeature(), IFeatureConfig.NO_FEATURE_CONFIG);

	public DeadlyFeature() {
		super(NoFeatureConfig.field_236558_a_);
	}

	@Override
	public boolean generate(ISeedReader world, ChunkGenerator gen, Random rand, BlockPos pos, NoFeatureConfig config) {
		if (!DeadlyConfig.DIM_WHITELIST.contains(world.getWorld().getDimensionKey().getLocation())) return false;
		for (WeightedGenerator generator : GENERATORS) {
			ChunkPos cPos = new ChunkPos(pos);
			if (wasSuccess(world.getDimensionType(), cPos.x, cPos.z)) return false;
			if (generator.generate(world, cPos.x, cPos.z, rand)) return true;
		}
		return false;
	}

	public static void enableGenerators() {
		GENERATORS.clear();
		if (BRUTAL_SPAWNER.isEnabled()) GENERATORS.add(BRUTAL_SPAWNER);
		if (SWARM_SPAWNER.isEnabled()) GENERATORS.add(SWARM_SPAWNER);
	}

	public static void debugPillar(World world, BlockPos pos) {
		BlockPos.Mutable mPos = new BlockPos.Mutable();
		mPos.setPos(pos);
		DeadlyModule.LOGGER.info("Marking! " + pos.toString());
		while (mPos.getY() < 127)
			world.setBlockState(mPos.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ()), Blocks.GLASS.getDefaultState());
	}

	public static final boolean DEBUG = false;

	public static void debugLog(BlockPos pos, String name) {
		if (true) DeadlyModule.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void setSuccess(DimensionType dim, int x, int z) {
		SUCCESSES.computeIfAbsent(dim, i -> new LongOpenHashSet()).add(ChunkPos.asLong(x, z));
	}

	public static boolean wasSuccess(DimensionType dim, int x, int z) {
		return SUCCESSES.computeIfAbsent(dim, i -> new LongOpenHashSet()).contains(ChunkPos.asLong(x, z));
	}
}