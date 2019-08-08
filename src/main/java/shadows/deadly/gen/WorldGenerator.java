package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.deadly.DeadlyModule;
import shadows.deadly.config.DeadlyConfig;

public class WorldGenerator extends Feature<NoFeatureConfig> {

	public static final List<WorldFeature> FEATURES = new ArrayList<>();
	public static final BrutalSpawner BRUTAL_SPAWNER = new BrutalSpawner();
	public static final BossFeature BOSS_GENERATOR = new BossFeature();
	public static final SwarmSpawner SWARM_SPAWNER = new SwarmSpawner();
	private static final Map<ResourceLocation, LongSet> SUCCESSES = new HashMap<>();
	public static final Predicate<BlockState> STONE_TEST = FillerBlockType.NATURAL_STONE.func_214738_b();

	public WorldGenerator() {
		super(NoFeatureConfig::deserialize);
	}

	@Override
	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
		if (DeadlyConfig.DIM_WHITELIST.contains(world.getDimension().getType().getRegistryName())) for (WorldFeature feature : FEATURES) {
			ChunkPos cPos = new ChunkPos(pos);
			if (wasSuccess(world.getDimension().getType().getRegistryName(), cPos.x, cPos.z)) return false;
			if (feature.generate(world, cPos.x, cPos.z, rand)) return true;
		}
		return false;
	}

	public static void init() {
		if (BRUTAL_SPAWNER.isEnabled()) FEATURES.add(BRUTAL_SPAWNER);
		if (SWARM_SPAWNER.isEnabled()) FEATURES.add(SWARM_SPAWNER);
		if (BOSS_GENERATOR.isEnabled()) FEATURES.add(BOSS_GENERATOR);
		ConfiguredFeature<?> gen = new ConfiguredFeature<>(new WorldGenerator(), IFeatureConfig.NO_FEATURE_CONFIG);
		for (Biome b : ForgeRegistries.BIOMES)
			b.addFeature(Decoration.UNDERGROUND_DECORATION, gen);
	}

	public static void debugPillar(World world, BlockPos pos) {
		MutableBlockPos mPos = new MutableBlockPos(pos);
		DeadlyModule.LOGGER.info("Marking! " + pos.toString());
		while (mPos.getY() < 127)
			world.setBlockState(mPos.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ()), Blocks.GLASS.getDefaultState());
	}

	public static final boolean DEBUG = false;

	public static void debugLog(BlockPos pos, String name) {
		if (DEBUG) DeadlyModule.LOGGER.info("Generated a {} at {}", name, pos);
	}

	public static void setSuccess(ResourceLocation dim, int x, int z) {
		SUCCESSES.computeIfAbsent(dim, i -> new LongOpenHashSet()).add(ChunkPos.asLong(x, z));
	}

	public static boolean wasSuccess(ResourceLocation dim, int x, int z) {
		return SUCCESSES.computeIfAbsent(dim, i -> new LongOpenHashSet()).contains(ChunkPos.asLong(x, z));
	}
}