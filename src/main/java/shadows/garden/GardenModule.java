package shadows.garden;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisConstruction;
import shadows.Apotheosis.ApotheosisRecipeEvent;
import shadows.ApotheosisObjects;
import shadows.placebo.config.Configuration;

public class GardenModule {

	public static int maxCactusHeight = 5;
	public static int maxReedHeight = 255;

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		Configuration c = new Configuration(new File(Apotheosis.configDir, "garden.cfg"));
		maxCactusHeight = c.getInt("Cactus Height", "general", maxCactusHeight, 1, 255, "The max height a stack of cacti may grow to.");
		maxReedHeight = c.getInt("Reed Height", "general", maxReedHeight, 1, 255, "The max height a stack of reeds may grow to.");
		if (c.hasChanged()) c.save();
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Apotheosis.registerOverrideBlock(e.getRegistry(), new BlockCactusExt(), Apotheosis.MODID);
		Apotheosis.registerOverrideBlock(e.getRegistry(), new BlockReedExt(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().register(new EnderLeashItem());
	}

	@SubscribeEvent
	public void recipes(ApotheosisRecipeEvent e) {
		e.helper.addShapeless(ApotheosisObjects.FARMERS_LEASH, Items.ENDER_PEARL, Items.LEAD, "ingotGold");
	}

}
