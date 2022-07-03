package shadows.apotheosis.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AttributeAffix;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;
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
		MinecraftForge.EVENT_BUS.addListener(this::reload);
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
	public void registry(NewRegistryEvent e) {
		RegistryBuilder<Affix> build = new RegistryBuilder<>();
		build.setName(new ResourceLocation(Apotheosis.MODID, "affixes"));
		build.setType(Affix.class);
		e.create(build, r -> Affix.REGISTRY = (ForgeRegistry<Affix>) r);
	}

	@SubscribeEvent
	public void attribs(Register<Attribute> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new RangedAttribute("apotheosis:draw_speed", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("draw_speed"),
				new RangedAttribute("apotheosis:crit_chance", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("crit_chance"),
				new RangedAttribute("apotheosis:crit_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("crit_damage"),
				new RangedAttribute("apotheosis:cold_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("cold_damage"),
				new RangedAttribute("apotheosis:fire_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("fire_damage"),
				new RangedAttribute("apotheosis:life_steal", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("life_steal"),
				new RangedAttribute("apotheosis:piercing", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("piercing"),
				new RangedAttribute("apotheosis:current_hp_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("current_hp_damage"),
				new RangedAttribute("apotheosis:overheal", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("overheal")
		);
		//Formatter::on
	}

	@SubscribeEvent
	public void affixes(Register<Affix> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MAX_HEALTH, Operation.ADDITION, (level -> 0.5F + Math.round(level * 3) / 2F)).types(LootCategory::isDefensive).build("common_max_hp"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ARMOR, Operation.ADDITION, 0.5F, 2).types(LootCategory::isDefensive).build("common_armor"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_DAMAGE, Operation.ADDITION, 0.5F, 2).build("common_dmg"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL, 0.05F, 0.15F).build("common_mvspd"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_SPEED, Operation.MULTIPLY_TOTAL, 0.1F, 0.25F).build("common_aspd"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_KNOCKBACK, Operation.ADDITION, 0.25F, 0.5F).build("common_kb"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(ForgeMod.REACH_DISTANCE, Operation.ADDITION, (level -> 0.5F + Math.round(level * 3) / 2F)).build("common_reach")
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