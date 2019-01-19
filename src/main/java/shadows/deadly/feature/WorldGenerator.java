package shadows.deadly.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import shadows.deadly.DeadlyWorld;
import shadows.deadly.feature.spawners.BrutalSpawner;
import shadows.deadly.feature.spawners.SwarmSpawner;

public class WorldGenerator implements IWorldGenerator {

	public static final List<WorldFeature> FEATURES = new ArrayList<>();
	public static final BrutalSpawner BRUTAL_SPAWNER = new BrutalSpawner();
	public static final BossFeature BOSS_GENERATOR = new BossFeature();
	public static final SwarmSpawner SWARM_SPAWNER = new SwarmSpawner();

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (!world.isRemote && world.provider.getDimension() == 0) WorldGenerator.run(world, new BlockPos(chunkX << 4, 0, chunkZ << 4));
	}

	public static void run(World world, BlockPos pos) {
		for (WorldFeature feature : FEATURES)
			feature.generate(world, pos);
	}

	/**
	 * Builds a glass pillar from the given location up to layer 127.
	 */
	public static void debugPillar(World world, BlockPos pos) {
		MutableBlockPos mPos = new MutableBlockPos(pos);
		DeadlyWorld.LOGGER.info("Marking! " + pos.toString());
		while (mPos.getY() < 127)
			world.setBlockState(mPos.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ()), Blocks.GLASS.getDefaultState());
	}

	public static void init() {

		WorldFeature f;

		//f = new DeadlyWorldDungeon();
		//if (f.isEnabled()) FEATURES.add(f);

		if (BRUTAL_SPAWNER.isEnabled()) FEATURES.add(BRUTAL_SPAWNER);
		if (SWARM_SPAWNER.isEnabled()) FEATURES.add(SWARM_SPAWNER);

		f = new SilverNest();
		if (f.isEnabled()) FEATURES.add(f);

		f = new BrutalSpawner();
		if (f.isEnabled()) FEATURES.add(f);

		f = new SwarmSpawner();
		if (f.isEnabled()) FEATURES.add(f);

		f = new RogueSpawner();
		if (f.isEnabled()) FEATURES.add(f);

		f = new FireTrap();
		if (f.isEnabled()) FEATURES.add(f);

		f = new Mine();
		if (f.isEnabled()) FEATURES.add(f);

		f = new BossFeature();
		if (f.isEnabled()) FEATURES.add(f);

		f = new PotionTrap();
		if (f.isEnabled()) FEATURES.add(f);

		/*
		property = Properties.getDouble(Properties.FREQUENCY, "potion_trap");
		if (property > 0.0) {
			featuresList.add(new PotionTrap(property));
		}
		property = Properties.getDouble(Properties.FREQUENCY, "spawner_trap");
		if (property > 0.0) {
			featuresList.add(new SpawnerTrap(property));
		}
		
		property = Properties.getDouble(Properties.FREQUENCY, "tower");
		if (property > 0.0) {
			featuresList.add(new Tower(property));
		}
		property = Properties.getDouble(Properties.FREQUENCY, "chest");
		if (property > 0.0) {
			featuresList.add(new RogueChest(property));
		}
		
		property = Properties.getDouble(Properties.VEINS, "sand_count");
		if (property > 0.0) {
			featuresList.add(new VeinFeature(property, Blocks.sand, Properties.getInt(Properties.VEINS, "sand_size"), Properties.getInt(Properties.VEINS, "sand_min_height"), Properties.getInt(Properties.VEINS, "sand_max_height")));
		}
		property = Properties.getDouble(Properties.VEINS, "lava_count");
		if (property > 0.0) {
			featuresList.add(new VeinLiquid(property, Blocks.lava, Properties.getInt(Properties.VEINS, "lava_size"), Properties.getInt(Properties.VEINS, "lava_min_height"), Properties.getInt(Properties.VEINS, "lava_max_height")));
		}
		property = Properties.getDouble(Properties.VEINS, "water_count");
		if (property > 0.0) {
			featuresList.add(new VeinLiquid(property, Blocks.water, Properties.getInt(Properties.VEINS, "water_size"), Properties.getInt(Properties.VEINS, "water_min_height"), Properties.getInt(Properties.VEINS, "water_max_height")));
		}
		property = Properties.getDouble(Properties.VEINS, "spawner_count");
		if (property > 0.0) {
			featuresList.add(new VeinSpawner(property));
		}
		property = Properties.getDouble(Properties.VEINS, "silverfish_count");
		if (property > 0.0) {
			featuresList.add(new VeinFeature(property, Blocks.monster_egg, Properties.getInt(Properties.VEINS, "silverfish_size"), Properties.getInt(Properties.VEINS, "silverfish_min_height"), Properties.getInt(Properties.VEINS, "silverfish_max_height")));
		}*/
	}
}