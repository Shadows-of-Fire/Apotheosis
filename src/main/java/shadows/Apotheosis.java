package shadows;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

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
		enableSpawner = config.getBoolean("Enable Spawner Management", "general", true, "If spawner management is enabled.");
		enableReed = config.getBoolean("Enable Infinite Sugarcane", "general", true, "If sugarcane will grow infinitely.");
		if (config.hasChanged()) config.save();
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

}
