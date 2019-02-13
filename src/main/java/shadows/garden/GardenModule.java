package shadows.garden;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisPreInit;

public class GardenModule {

	public static int maxCactusHeight = 5;
	public static int maxReedHeight = 255;

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		Configuration c = new Configuration(new File(Apotheosis.configDir, "garden.cfg"));
		maxCactusHeight = c.getInt("Cactus Height", "general", maxCactusHeight, 1, 255, "The max height a stack of cacti may grow to.");
		maxReedHeight = c.getInt("Reed Height", "general", maxReedHeight, 1, 255, "The max height a stack of reeds may grow to.");
		if (c.hasChanged()) c.save();
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Apotheosis.registerOverrideBlock(e.getRegistry(), new BlockReedExt(), Apotheosis.MODID);
		Apotheosis.registerOverrideBlock(e.getRegistry(), new BlockCactusExt(), Apotheosis.MODID);
	}

}
