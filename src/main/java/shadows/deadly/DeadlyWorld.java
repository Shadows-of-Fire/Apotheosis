package shadows.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisPreInit;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.feature.BossFeature;
import shadows.deadly.feature.PotionTrap;
import shadows.deadly.feature.RogueSpawner;
import shadows.deadly.feature.SilverNest;
import shadows.deadly.feature.WorldGenerator;
import shadows.deadly.feature.spawners.BrutalSpawner;
import shadows.deadly.feature.spawners.SwarmSpawner;
import shadows.deadly.util.ChestBuilder;
import shadows.deadly.util.DungeonRemover;

public class DeadlyWorld {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		DeadlyConfig.config = new Configuration(new File(Apotheosis.configDir, "deadly.cfg"));
	}

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		DeadlyConfig.init();
		//DeadlyWorldDungeon.init();
		BrutalSpawner.init();
		BossFeature.init();
		RogueSpawner.init();
		SilverNest.init();
		ChestBuilder.init();
		SwarmSpawner.init();
		PotionTrap.init();

		WorldGenerator.init();
		GameRegistry.registerWorldGenerator(new WorldGenerator(), 255);
		MinecraftForge.TERRAIN_GEN_BUS.register(new DungeonRemover());
	}
}