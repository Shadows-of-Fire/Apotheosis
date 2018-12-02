package shadows;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Apotheosis.MODID, name = Apotheosis.MODNAME, version = Apotheosis.Version, dependencies = "required-after:placebo@[1.5.1,)", acceptableRemoteVersions = "*")
public class Apotheosis {

	public static final String MODID = "apotheosis";
	public static final String MODNAME = "Apotheosis";
	public static final String Version = "1.0.0";

	public static File configDir;
	public static Configuration config;
	public static boolean enableSpawner = true;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		configDir = new File(e.getModConfigurationDirectory(), MODID);
		config = new Configuration(new File(configDir, MODID + ".cfg"));
		ApotheosisCore.enableAnvil = config.getBoolean("Enable Anvil Cap Removal", "asm", true, "If the anvil cap remover tweak is enabled.");
		enableSpawner = config.getBoolean("Enable Spawner Management", "general", true, "If spawner management is enabled.");
		if (config.hasChanged()) config.save();
	}

}
