package shadows.apotheosis.garden;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.ApotheosisObjects;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;

public class GardenModule {

	public static int maxCactusHeight = 5;
	public static int maxReedHeight = 255;
	public static int maxBambooHeight = 32;

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent e) {
		this.reload(null);
		Apotheosis.HELPER.addShapeless(ApotheosisObjects.FARMERS_LEASH, Items.ENDER_PEARL, Items.LEAD, Items.GOLD_INGOT);
		MinecraftForge.EVENT_BUS.addListener(this::reload);
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		PlaceboUtil.registerOverride(new ApothCactusBlock(), Apotheosis.MODID);
		PlaceboUtil.registerOverride(new ApothSugarcaneBlock(), Apotheosis.MODID);
		PlaceboUtil.registerOverride(new ApothBambooBlock(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().register(new EnderLeashItem().setRegistryName("farmers_leash"));
		ComposterBlock.COMPOSTABLES.put(Blocks.CACTUS.asItem(), 0.5F);
		ComposterBlock.COMPOSTABLES.put(Blocks.SUGAR_CANE.asItem(), 0.5F);
	}

	public void reload(ApotheosisReloadEvent e) {
		Configuration c = new Configuration(new File(Apotheosis.configDir, "garden.cfg"));
		maxCactusHeight = c.getInt("Cactus Height", "general", maxCactusHeight, 1, 255, "The max height a stack of cacti may grow to.  Vanilla is 3.");
		maxReedHeight = c.getInt("Reed Height", "general", maxReedHeight, 1, 255, "The max height a stack of reeds may grow to.  Vanilla is 3.");
		maxBambooHeight = c.getInt("Bamboo Height", "general", maxBambooHeight, 1, 255, "The max height a stack of bamboo may grow to.  Vanilla is 16.");
		if (e == null && c.hasChanged()) c.save();
	}
}