package shadows.apotheosis.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
		MinecraftForge.EVENT_BUS.addListener(this::onBiomeLoad);
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
		e.getRegistry().register(WorldGenerator.INSTANCE.feature.setRegistryName("deadly_world_gen"));
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, "apotheosis:deadly_module", WorldGenerator.INSTANCE);
	}

	public void reloads(AddReloadListenerEvent e) {
		e.addListener(LootManager.INSTANCE);
	}

	public void onBiomeLoad(BiomeLoadingEvent e) {
		if (!DeadlyConfig.BIOME_BLACKLIST.contains(e.getName())) e.getGeneration().getFeatures(Decoration.UNDERGROUND_DECORATION).add(() -> WorldGenerator.INSTANCE);
	}

}