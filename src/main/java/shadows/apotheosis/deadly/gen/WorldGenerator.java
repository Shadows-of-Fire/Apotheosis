package shadows.apotheosis.deadly.gen;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;

@SuppressWarnings("deprecation")
public class WorldGenerator extends Feature<NoFeatureConfig> {

	public static final List<WorldFeature> FEATURES = new ArrayList<>();
	public static final BrutalSpawner BRUTAL_SPAWNER = new BrutalSpawner();
	public static final BossFeature BOSS_GENERATOR = new BossFeature();
	public static final SwarmSpawner SWARM_SPAWNER = new SwarmSpawner();
	private static final Map<DimensionType, LongSet> SUCCESSES = new HashMap<>();
	public static final Predicate<BlockState> STONE_TEST = FillerBlockType.NATURAL_STONE.func_214738_b();

	public WorldGenerator() {
		super(NoFeatureConfig.CODEC);
	}

	@Override
	public boolean generate(ISeedReader world, StructureManager struct, ChunkGenerator gen, Random rand, BlockPos pos, NoFeatureConfig config) {
		//if (DeadlyConfig.DIM_WHITELIST.contains(world.getDimension().getType().getRegistryName())) 
		for (WorldFeature feature : FEATURES) {
			ChunkPos cPos = new ChunkPos(pos);
			if (wasSuccess(world.getDimension(), cPos.x, cPos.z)) return false;
			if (feature.generate(world, cPos.x, cPos.z, rand)) return true;
		}
		return false;
	}

	public static void init() {
		if (BRUTAL_SPAWNER.isEnabled()) FEATURES.add(BRUTAL_SPAWNER);
		if (SWARM_SPAWNER.isEnabled()) FEATURES.add(SWARM_SPAWNER);
		if (BOSS_GENERATOR.isEnabled()) FEATURES.add(BOSS_GENERATOR);
		ConfiguredFeature<?, ?> gen = new ConfiguredFeature<>(ApotheosisObjects.DEADLY_WORLD_GEN, IFeatureConfig.NO_FEATURE_CONFIG);
		DeferredWorkQueue.runLater(() -> {
			for (Biome b : ForgeRegistries.BIOMES)
				if (!DeadlyConfig.BIOME_BLACKLIST.contains(b.getRegistryName())) b.addFeature(Decoration.UNDERGROUND_DECORATION, gen);
		});
	}

	public static void debugPillar(World world, BlockPos pos) {
		BlockPos.Mutable mPos = new BlockPos.Mutable();
		mPos.setPos(pos);
		DeadlyModule.LOGGER.info("Marking! " + pos.toString());
		while (mPos.getY() < 127)
			world.setBlockState(mPos.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ()), Blocks.GLASS.getDefaultState());
	}

	public static final boolean DEBUG = true;

	public static void debugLog(BlockPos pos, String name) {
		if (DEBUG) DeadlyModule.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void setSuccess(DimensionType dim, int x, int z) {
		SUCCESSES.computeIfAbsent(dim, i -> new LongOpenHashSet()).add(ChunkPos.asLong(x, z));
	}

	public static boolean wasSuccess(DimensionType dim, int x, int z) {
		return SUCCESSES.computeIfAbsent(dim, i -> new LongOpenHashSet()).contains(ChunkPos.asLong(x, z));
	}
}