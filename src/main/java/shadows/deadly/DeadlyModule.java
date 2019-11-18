package shadows.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import shadows.deadly.loot.affix.AffixEvents;
import shadows.deadly.loot.affix.Affixes;

public class DeadlyModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		DeadlyConfig.config = new Configuration(new File(Apotheosis.configDir, "deadly.cfg"));
		MinecraftForge.EVENT_BUS.register(new AffixEvents());
	}

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		DeadlyConfig.init();
		BrutalSpawner.init();
		BossFeature.init();
		DeadlyLoot.init();
		SwarmSpawner.init();
		WorldGenerator.init();
		Affixes.init();
		MinecraftForge.EVENT_BUS.register(new WorldGenerator());
	}

}