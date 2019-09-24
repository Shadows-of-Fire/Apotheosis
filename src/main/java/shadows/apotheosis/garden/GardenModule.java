package shadows.apotheosis.garden;

import java.io.File;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.IProperty;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisSetup;
import shadows.apotheosis.ApotheosisObjects;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;

public class GardenModule {

	public static int maxCactusHeight = 5;
	public static int maxReedHeight = 255;
	public static int maxBambooHeight = 32;

	@SubscribeEvent
	public void setup(ApotheosisSetup e) {
		Configuration c = new Configuration(new File(Apotheosis.configDir, "garden.cfg"));
		maxCactusHeight = c.getInt("Cactus Height", "general", maxCactusHeight, 1, 255, "The max height a stack of cacti may grow to.  Vanilla is 3.");
		maxReedHeight = c.getInt("Reed Height", "general", maxReedHeight, 1, 255, "The max height a stack of reeds may grow to.  Vanilla is 3.");
		maxBambooHeight = c.getInt("Bamboo Height", "general", maxBambooHeight, 1, 255, "The max height a stack of bamboo may grow to.  Vanilla is 16.");
		if (c.hasChanged()) c.save();
		Apotheosis.HELPER.addShapeless(ApotheosisObjects.FARMERS_LEASH, Items.ENDER_PEARL, Items.LEAD, Items.GOLD_INGOT);
		((FlowerPotBlock) Blocks.POTTED_BAMBOO).flower = Blocks.BAMBOO;
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		PlaceboUtil.registerOverrideBlock(new BlockCactusExt(), Apotheosis.MODID);
		PlaceboUtil.registerOverrideBlock(new BlockReedExt(), Apotheosis.MODID);
		PlaceboUtil.registerOverrideBlock(new BlockBambooExt(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().register(new EnderLeashItem().setRegistryName("farmers_leash"));
		ComposterBlock.CHANCES.put(Blocks.CACTUS, 0.5F);
		ComposterBlock.CHANCES.put(Blocks.SUGAR_CANE, 0.5F);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> BlockState update(BlockState state) {
		BlockState ret = state.getBlock().delegate.get().getDefaultState();
		for (Map.Entry<IProperty<?>, Comparable<?>> ent : state.getValues().entrySet()) {
			ret = state.with((IProperty<T>) ent.getKey(), (T) ent.getValue());
		}
		return ret;
	}

}
