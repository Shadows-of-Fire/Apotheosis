package shadows.apotheosis.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisSetup;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.gen.BossFeature;
import shadows.apotheosis.deadly.gen.BrutalSpawner;
import shadows.apotheosis.deadly.gen.SwarmSpawner;
import shadows.apotheosis.deadly.gen.WorldGenerator;
import shadows.apotheosis.deadly.loot.LootManager;
import shadows.apotheosis.deadly.loot.affix.AffixEvents;
import shadows.placebo.config.Configuration;

public class DeadlyModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		DeadlyConfig.config = new Configuration(new File(Apotheosis.configDir, "deadly.cfg"));
		MinecraftForge.EVENT_BUS.register(new AffixEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reloads);
	}

	@SubscribeEvent
	public void init(ApotheosisSetup e) {
		DeadlyConfig.init();
		BrutalSpawner.init();
		BossFeature.init();
		DeadlyLoot.init();
		SwarmSpawner.init();
		WorldGenerator.init();
	}

	@SubscribeEvent
	public void register(Register<Feature<?>> e) {
		e.getRegistry().register(new WorldGenerator().setRegistryName("deadly_world_gen"));
	}

	@SubscribeEvent
	public void registerSounds(Register<SoundEvent> e) {
		e.getRegistry().register(new SoundEvent(new ResourceLocation(Apotheosis.MODID, "chain_zap")).setRegistryName("chain_zap"));
	}

	public void reloads(FMLServerAboutToStartEvent e) {
		e.getServer().getResourceManager().addReloadListener(LootManager.INSTANCE);
	}

}