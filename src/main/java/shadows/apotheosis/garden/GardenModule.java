package shadows.apotheosis.garden;

import java.io.File;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BambooLeaves;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BambooFeature;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
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
		fix(DefaultBiomeFeatures.SUGAR_CANE_CONFIG, Blocks.SUGAR_CANE);
		fix(DefaultBiomeFeatures.CACTUS_CONFIG, Blocks.CACTUS);
		BambooFeature.BAMBOO_BASE = Blocks.BAMBOO.getDefaultState().with(BambooBlock.PROPERTY_AGE, 1).with(BambooBlock.PROPERTY_BAMBOO_LEAVES, BambooLeaves.NONE).with(BambooBlock.PROPERTY_STAGE, 0);
		BambooFeature.BAMBOO_LARGE_LEAVES_GROWN = BambooFeature.BAMBOO_BASE.with(BambooBlock.PROPERTY_BAMBOO_LEAVES, BambooLeaves.LARGE).with(BambooBlock.PROPERTY_STAGE, 1);
		BambooFeature.BAMBOO_LARGE_LEAVES = BambooFeature.BAMBOO_BASE.with(BambooBlock.PROPERTY_BAMBOO_LEAVES, BambooLeaves.LARGE);
		BambooFeature.BAMBOO_SMALL_LEAVES = BambooFeature.BAMBOO_BASE.with(BambooBlock.PROPERTY_BAMBOO_LEAVES, BambooLeaves.SMALL);
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
		ComposterBlock.CHANCES.put(Blocks.CACTUS.asItem(), 0.5F);
		ComposterBlock.CHANCES.put(Blocks.SUGAR_CANE.asItem(), 0.5F);
	}

	private static void fix(BlockClusterFeatureConfig cfg, Block newBlock) {
		ObfuscationReflectionHelper.setPrivateValue(BlockClusterFeatureConfig.class, cfg, new SimpleBlockStateProvider(newBlock.getDefaultState()), "field_227289_a_");
	}

}
