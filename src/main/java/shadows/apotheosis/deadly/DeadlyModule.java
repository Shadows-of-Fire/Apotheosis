package shadows.apotheosis.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.deadly.affix.AffixEvents;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.gen.BossGenerator;
import shadows.apotheosis.deadly.gen.BrutalSpawnerGenerator;
import shadows.apotheosis.deadly.gen.DeadlyFeature;
import shadows.apotheosis.deadly.gen.SwarmSpawnerGenerator;
import shadows.apotheosis.deadly.loot.BossArmorManager;
import shadows.apotheosis.deadly.loot.LootManager;
import shadows.apotheosis.deadly.objects.BossSummonerItem;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.config.Configuration;

public class DeadlyModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		MinecraftForge.EVENT_BUS.register(new AffixEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reloads);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onBiomeLoad);
		MinecraftForge.EVENT_BUS.addListener(this::reload);
	}

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		reload(null);
		DeadlyLoot.init();
	}

	@SubscribeEvent
	public void register(Register<Feature<?>> e) {
		e.getRegistry().register(DeadlyFeature.INSTANCE.feature.setRegistryName("deadly_world_gen"));
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, "apotheosis:deadly_module", DeadlyFeature.INSTANCE);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().register(new BossSummonerItem(new Item.Properties().maxStackSize(1).group(ItemGroup.MISC)).setRegistryName("boss_summoner"));
	}

	public void reloads(AddReloadListenerEvent e) {
		e.addListener(LootManager.INSTANCE);
		e.addListener(BossArmorManager.INSTANCE);
	}

	public void onBiomeLoad(BiomeLoadingEvent e) {
		if (!DeadlyConfig.BIOME_BLACKLIST.contains(e.getName())) e.getGeneration().getFeatures(Decoration.UNDERGROUND_DECORATION).add(() -> DeadlyFeature.INSTANCE);
	}

	/**
	 * Loads all configurable data for the deadly module.
	 */
	public void reload(ApotheosisReloadEvent e) {
		DeadlyConfig.config = new Configuration(new File(Apotheosis.configDir, "deadly.cfg"));
		Configuration nameConfig = new Configuration(new File(Apotheosis.configDir, "names.cfg"));
		DeadlyConfig.loadConfigs();
		NameHelper.load(nameConfig);
		BrutalSpawnerGenerator.reload();
		SwarmSpawnerGenerator.init();
		BossGenerator.rebuildBossItems();
		DeadlyFeature.enableGenerators();
		if (e == null && DeadlyConfig.config.hasChanged()) DeadlyConfig.config.save();
		if (e == null && nameConfig.hasChanged()) nameConfig.save();
	}

}