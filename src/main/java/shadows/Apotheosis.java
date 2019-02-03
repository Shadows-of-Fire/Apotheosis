package shadows;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import shadows.anvil.AnvilModule;
import shadows.deadly.DeadlyModule;
import shadows.ench.EnchModule;
import shadows.potion.PotionModule;
import shadows.reeds.InfiniteReeds;
import shadows.spawn.SpawnerModule;

@Mod(modid = Apotheosis.MODID, name = Apotheosis.MODNAME, version = Apotheosis.Version, dependencies = "required-after:placebo@[1.5.1,)", acceptableRemoteVersions = "*")
public class Apotheosis {

	public static final String MODID = "apotheosis";
	public static final String MODNAME = "Apotheosis";
	public static final String Version = "1.0.0";

	public static File configDir;
	public static Configuration config;
	public static boolean enableSpawner = true;
	public static boolean enableReed = true;
	public static boolean enableDeadly = true;
	public static boolean enableEnch = true;
	public static boolean enablePotion = true;
	public static boolean enableAnvil = true;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		configDir = new File(e.getModConfigurationDirectory(), MODID);
		config = new Configuration(new File(configDir, MODID + ".cfg"));

		ApotheosisCore.enableAnvil = config.getBoolean("Enable Anvil Cap Removal", "asm", true, "If the anvil cap remover tweak is enabled.");
		ApotheosisCore.enableInvis = config.getBoolean("Enable Invisibility Tweak", "asm", true, "If potion effects are removed for invisibility.");
		ApotheosisCore.enableEnch = config.getBoolean("Enable Enchantment Cap Removal", "asm", true, "If the enchantment cap remover tweak is enabled.");
		ApotheosisCore.enablePInf = config.getBoolean("Enable Potion Infinity", "asm", true, "If the potion infinity enchantment actually works.");

		enableEnch = config.getBoolean("Enable Enchantment Module", "general", true, "If the enchantment module is enabled.");
		if (enableEnch) MinecraftForge.EVENT_BUS.register(new EnchModule());

		enableSpawner = config.getBoolean("Enable Spawner Module", "general", true, "If the spawner module is enabled.");
		if (enableSpawner) MinecraftForge.EVENT_BUS.register(new SpawnerModule());

		enableReed = config.getBoolean("Enable Infinite Sugarcane", "general", true, "If sugarcane will grow infinitely.");
		if (enableReed) MinecraftForge.EVENT_BUS.register(new InfiniteReeds());

		enableDeadly = config.getBoolean("Enable Deadly Module", "general", true, "If the deadly module is loaded.");
		if (enableDeadly) MinecraftForge.EVENT_BUS.register(new DeadlyModule());

		enablePotion = config.getBoolean("Enable Potion Module", "general", true, "If the potion module is loaded.");
		if (enablePotion) MinecraftForge.EVENT_BUS.register(new PotionModule());

		enableAnvil = config.getBoolean("Enable Anvil Module", "general", true, "If the anvil module is loaded.");
		if (enableAnvil) MinecraftForge.EVENT_BUS.register(new AnvilModule());

		if (config.hasChanged()) config.save();
		MinecraftForge.EVENT_BUS.post(new ApotheosisPreInit(e));
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.post(new ApotheosisInit(e));
	}

	public static void registerOverrideBlock(IForgeRegistry<Block> reg, Block b, String modid) {
		reg.register(b);
		ForgeRegistries.ITEMS.register(new ItemBlock(b) {
			@Override
			public String getCreatorModId(ItemStack itemStack) {
				return modid;
			}
		}.setRegistryName(b.getRegistryName()));
	}

	public static class ApotheosisPreInit extends Event {
		public FMLPreInitializationEvent ev;

		private ApotheosisPreInit(FMLPreInitializationEvent ev) {
			this.ev = ev;
		}
	}

	public static class ApotheosisInit extends Event {
		public FMLInitializationEvent ev;

		private ApotheosisInit(FMLInitializationEvent ev) {
			this.ev = ev;
		}
	}

}
