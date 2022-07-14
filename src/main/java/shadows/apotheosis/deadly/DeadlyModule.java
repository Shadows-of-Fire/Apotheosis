package shadows.apotheosis.deadly;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
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
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.AttributeAffix;
import shadows.apotheosis.deadly.client.DeadlyModuleClient;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.config.Configuration;

public class DeadlyModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR, 40D, "maxValue");
		ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR_TOUGHNESS, 30D, "maxValue");
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
		build.disableSaving().onBake(AffixHelper::recomputeMaps);
		e.create(build, r -> Affix.REGISTRY = (ForgeRegistry<Affix>) r);
	}

	@SubscribeEvent
	public void attribs(Register<Attribute> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new RangedAttribute("apotheosis:draw_speed", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("draw_speed"),
				new RangedAttribute("apotheosis:crit_chance", 1.0D, 1.0D, 2.0D).setSyncable(true).setRegistryName("crit_chance"),
				new RangedAttribute("apotheosis:crit_damage", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("crit_damage"),
				new RangedAttribute("apotheosis:cold_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("cold_damage"),
				new RangedAttribute("apotheosis:fire_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("fire_damage"),
				new RangedAttribute("apotheosis:life_steal", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("life_steal"),
				new RangedAttribute("apotheosis:piercing", 1.0D, 1.0D, 2.0D).setSyncable(true).setRegistryName("piercing"),
				new RangedAttribute("apotheosis:current_hp_damage", 1.0D, 1.0D, 2.0D).setSyncable(true).setRegistryName("current_hp_damage"),
				new RangedAttribute("apotheosis:overheal", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("overheal"),
				new RangedAttribute("apotheosis:ghost_health", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("overheal"),
				new RangedAttribute("apotheosis:break_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("break_speed")
		);
		//Formatter::on
	}

	@SubscribeEvent
	public void affixes(Register<Affix> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MAX_HEALTH, Operation.ADDITION, step(0.5F, 3, 0.5F)).types(LootCategory::isDefensive).build("tough"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ARMOR, Operation.ADDITION, step(0.5F, 6, 0.25F)).types(LootCategory::isDefensive).build("armored"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_DAMAGE, Operation.ADDITION, step(0.5F, 6, 0.25F)).types(LootCategory::isWeapon).build("pointy"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL, step(0.05F, 10, 0.01F)).build("quick"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_SPEED, Operation.MULTIPLY_TOTAL, step(0.1F, 15, 0.01F)).build("nimble"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_KNOCKBACK, Operation.ADDITION, step(0.25F, 5, 0.05F)).build("pushy"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(ForgeMod.REACH_DISTANCE, Operation.ADDITION, step(0.5F, 6, 0.25F)).types(t -> !t.isWeapon()).build("long"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(ForgeMod.ATTACK_RANGE, Operation.ADDITION, step(0.5F, 6, 0.25F)).types(LootCategory::isWeapon).build("extended"),
				new AttributeAffix.Builder(LootRarity.COMMON).with(() -> Apoth.Attributes.DRAW_SPEED, Operation.MULTIPLY_BASE, level -> level > 0.5F ? 0.2F : 0.1F).types(LootCategory::isRanged).build("agile")
		);

		e.getRegistry().registerAll(
				new AttributeAffix.Builder(LootRarity.UNCOMMON)
				.with(Attributes.MAX_HEALTH, Operation.ADDITION, step(1F, 5, 0.5F))
				.with(Attributes.ARMOR, Operation.ADDITION, step(1F, 8, 0.25F))
				.types(LootCategory::isDefensive).build("reinforced"),
				
				new AttributeAffix.Builder(LootRarity.UNCOMMON)
				.with(Attributes.ARMOR, Operation.ADDITION, step(1F, 10, 0.25F))
				.with(Attributes.ARMOR_TOUGHNESS, Operation.ADDITION, step(0.5F, 4, 0.25F))
				.types(LootCategory::isDefensive).build("plated"),
				
				new AttributeAffix.Builder(LootRarity.UNCOMMON)
				.with(Attributes.ATTACK_SPEED, Operation.MULTIPLY_TOTAL, step(0.2F, 10, 0.02F))
				.with(() -> Apoth.Attributes.FIRE_DAMAGE, Operation.ADDITION, step(1, 6, 0.5F))
				.types(LootCategory::isWeapon).build("blessed"),
				
				new AttributeAffix.Builder(LootRarity.UNCOMMON)
				.with(Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL, step(0.05F, 10, 0.01F))
				.with(ForgeMod.ENTITY_GRAVITY, Operation.MULTIPLY_TOTAL, step(-0.025F, 3, -0.025F))
				.build("light"),
				
				new AttributeAffix.Builder(LootRarity.UNCOMMON)
				.with(() -> Apoth.Attributes.COLD_DAMAGE, Operation.ADDITION, step(1F, 12, 0.25F))
				.with(() -> Apoth.Attributes.CRIT_CHANCE, Operation.MULTIPLY_BASE, step(0.1F, 15, 0.01F))
				.types(LootCategory::isWeapon).build("icy"),
				
				new AttributeAffix.Builder(LootRarity.UNCOMMON)
				.with(() -> Apoth.Attributes.COLD_DAMAGE, Operation.ADDITION, step(1F, 14, 0.25F))
				.with(ForgeMod.SWIM_SPEED, Operation.MULTIPLY_BASE, step(0.05F, 16, 0.01F))
				.build("aquatic"),
				
				new AttributeAffix.Builder(LootRarity.UNCOMMON)
				.with(() -> Apoth.Attributes.PIERCING, Operation.MULTIPLY_BASE, step(0.1F, 20, 0.01F))
				.with(() -> Apoth.Attributes.OVERHEAL, Operation.MULTIPLY_BASE, step(0.05F, 15, 0.01F))
				.types(t -> t == LootCategory.HEAVY_WEAPON).build("weighted")
				
				//new AttributeAffix.Builder(LootRarity.UNCOMMON).with(ForgeMod.REACH_DISTANCE, Operation.ADDITION, step(0.5F, 6, 0.25F)).types(t -> !t.isWeapon()).build("long"),
				//new AttributeAffix.Builder(LootRarity.UNCOMMON).with(ForgeMod.ATTACK_RANGE, Operation.ADDITION, step(0.5F, 6, 0.25F)).types(LootCategory::isWeapon).build("extended"),
				//new AttributeAffix.Builder(LootRarity.UNCOMMON).with(Apoth.Attributes.DRAW_SPEED, Operation.MULTIPLY_BASE, level -> level > 0.5F ? 0.2F : 0.1F).types(LootCategory::isRanged).build("agile")
		);
		//Formatter::on
	}

	/**
	 * Level Function that allows for only returning "nice" stepped numbers.
	 * @param min The min value
	 * @param steps The max number of steps
	 * @param step The value per step
	 * @return A level function according to these rules
	 */
	private Float2FloatFunction step(float min, int steps, float step) {
		return level -> min + ((int) (steps * level) * step);
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