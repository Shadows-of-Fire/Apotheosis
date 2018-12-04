package shadows;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import shadows.ench.EnchantabilityTweaker;
import shadows.reeds.InfiniteReeds;

@Mod(modid = Apotheosis.MODID, name = Apotheosis.MODNAME, version = Apotheosis.Version, dependencies = "required-after:placebo@[1.5.1,)", acceptableRemoteVersions = "*")
public class Apotheosis {

	public static final String MODID = "apotheosis";
	public static final String MODNAME = "Apotheosis";
	public static final String Version = "1.0.0";

	public static File configDir;
	public static Configuration config;
	public static boolean enableSpawner = true;
	public static boolean enableReed = true;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		configDir = new File(e.getModConfigurationDirectory(), MODID);
		config = new Configuration(new File(configDir, MODID + ".cfg"));
		ApotheosisCore.enableAnvil = config.getBoolean("Enable Anvil Cap Removal", "asm", true, "If the anvil cap remover tweak is enabled.");
		ApotheosisCore.enableEnch = config.getBoolean("Enable Enchantment Cap Removal", "asm", true, "If the enchantment cap remover tweak is enabled.");
		if (ApotheosisCore.enableEnch) MinecraftForge.EVENT_BUS.register(new EnchantabilityTweaker());
		enableSpawner = config.getBoolean("Enable Spawner Management", "general", true, "If spawner management is enabled.");
		enableReed = config.getBoolean("Enable Infinite Sugarcane", "general", true, "If sugarcane will grow infinitely.");
		if (enableReed) MinecraftForge.EVENT_BUS.register(new InfiniteReeds());
		if (config.hasChanged()) config.save();
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

	public static class ApotheosisInit extends Event {
		public FMLPreInitializationEvent ev;

		private ApotheosisInit(FMLPreInitializationEvent ev) {
			this.ev = ev;
		}
	}

}
