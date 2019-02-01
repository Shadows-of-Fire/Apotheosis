package shadows.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisPreInit;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.feature.BossFeature;
import shadows.deadly.feature.BrutalSpawner;
import shadows.deadly.feature.SwarmSpawner;
import shadows.deadly.feature.WorldGenerator;
import shadows.deadly.util.ChestBuilder;

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
		ChestBuilder.init();
		SwarmSpawner.init();
		WorldGenerator.init();
		GameRegistry.registerWorldGenerator(new WorldGenerator(), 255);
	}
}