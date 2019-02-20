package shadows.deadly;

import java.io.File;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisPreInit;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.gen.BossFeature;
import shadows.deadly.gen.BrutalSpawner;
import shadows.deadly.gen.SwarmSpawner;
import shadows.deadly.gen.WorldGenerator;
import shadows.util.ArmorSet;

public class DeadlyModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		DeadlyConfig.config = new Configuration(new File(Apotheosis.configDir, "deadly.cfg"));
	}

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		DeadlyConfig.init();
		BrutalSpawner.init();
		BossFeature.init();
		DeadlyLoot.init();
		SwarmSpawner.init();
		WorldGenerator.init();
		MinecraftForge.EVENT_BUS.register(new WorldGenerator());
		ArmorSet.sortSets();
	}

	/**
	 * Injects a custom spawner into a dungeon.
	 * @param w The world
	 * @param rand The random
	 * @param p The blockpos
	 */
	public static void setDungeonMobSpawner(Object w, Random rand, Object p) {
		World world = (World) w;
		BlockPos pos = (BlockPos) p;
		if (rand.nextFloat() <= DeadlyConfig.dungeonBrutalChance) {
			WorldGenerator.BRUTAL_SPAWNER.place(world, pos, rand);
		} else if (rand.nextFloat() <= DeadlyConfig.dungeonSwarmChance) {
			WorldGenerator.SWARM_SPAWNER.place(world, pos, rand);
		}
	}
}