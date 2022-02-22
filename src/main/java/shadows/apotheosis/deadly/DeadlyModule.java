package shadows.apotheosis.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AttributeAffix;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.config.Configuration;

public class DeadlyModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
	}

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		this.reload(null);
		MinecraftForge.EVENT_BUS.register(new DeadlyModuleEvents());
	}

	@SubscribeEvent
	public void register(Register<Feature<?>> e) {
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
	}

	@SubscribeEvent
	public void tiles(Register<BlockEntityType<?>> e) {
	}

	@SubscribeEvent
	public void serializers(Register<RecipeSerializer<?>> e) {
	}

	@SubscribeEvent
	public void affixes(Register<Affix> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MAX_HEALTH, Operation.ADDITION, (level -> 0.5F + Math.round(level * 3) / 2F)).build("common_max_hp"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ARMOR, Operation.ADDITION, 0.5F, 2).build("common_armor"),
				new AttributeAffix.Builder(LootRarity.COMMON).with( Attributes.ATTACK_DAMAGE, Operation.ADDITION, 0.5F, 2).build("common_dmg"),
				new AttributeAffix.Builder(LootRarity.COMMON).with( Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL, 0.05F, 0.15F).build("common_mvspd"),
				new AttributeAffix.Builder(LootRarity.COMMON).with( Attributes.ATTACK_SPEED, Operation.MULTIPLY_TOTAL, 0.1F, 0.25F).build("common_aspd")
		);
		//Formatter::on
	}

	@SubscribeEvent
	public void client(FMLClientSetupEvent e) {
		e.enqueueWork(DeadlyModuleClient::init);
	}

	/**
	 * Loads all configurable data for the deadly module.
	 */
	public void reload(ApotheosisReloadEvent e) {
		Configuration mainConfig = new Configuration(new File(Apotheosis.configDir, "deadly.cfg"));
		Configuration nameConfig = new Configuration(new File(Apotheosis.configDir, "names.cfg"));
		DeadlyConfig.load(mainConfig);
		NameHelper.load(nameConfig);
		if (e == null && mainConfig.hasChanged()) mainConfig.save();
		if (e == null && nameConfig.hasChanged()) nameConfig.save();
	}

	public static final boolean DEBUG = false;

	public static void debugLog(BlockPos pos, String name) {
		if (DEBUG) DeadlyModule.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
	}

}