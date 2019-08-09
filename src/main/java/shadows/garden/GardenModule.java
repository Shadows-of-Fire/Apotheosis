package shadows.garden;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisSetup;
import shadows.ApotheosisObjects;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;

public class GardenModule {

	public static int maxCactusHeight = 5;
	public static int maxReedHeight = 255;

	@SubscribeEvent
	public void preInit(ApotheosisSetup e) {
		Configuration c = new Configuration(new File(Apotheosis.configDir, "garden.cfg"));
		maxCactusHeight = c.getInt("Cactus Height", "general", maxCactusHeight, 1, 255, "The max height a stack of cacti may grow to.");
		maxReedHeight = c.getInt("Reed Height", "general", maxReedHeight, 1, 255, "The max height a stack of reeds may grow to.");
		if (c.hasChanged()) c.save();
		Apotheosis.HELPER.addShapeless(ApotheosisObjects.FARMERS_LEASH, Items.ENDER_PEARL, Items.LEAD, Items.GOLD_INGOT);
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		PlaceboUtil.registerOverrideBlock(new BlockCactusExt(), Apotheosis.MODID);
		PlaceboUtil.registerOverrideBlock(new BlockReedExt(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().register(new EnderLeashItem().setRegistryName("farmers_leash"));
	}

}
