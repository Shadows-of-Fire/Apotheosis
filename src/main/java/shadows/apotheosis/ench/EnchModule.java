package shadows.apotheosis.ench;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisClientSetup;
import shadows.apotheosis.Apotheosis.ApotheosisSetup;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.EnchantmentInfo.ExpressionPowerFunc;
import shadows.apotheosis.ench.altar.BlockPrismaticAltar;
import shadows.apotheosis.ench.altar.TilePrismaticAltar;
import shadows.apotheosis.ench.anvil.BlockAnvilExt;
import shadows.apotheosis.ench.anvil.EnchantmentSplitting;
import shadows.apotheosis.ench.anvil.ItemAnvilExt;
import shadows.apotheosis.ench.anvil.TileAnvil;
import shadows.apotheosis.ench.enchantments.EnchantmentBerserk;
import shadows.apotheosis.ench.enchantments.EnchantmentDepths;
import shadows.apotheosis.ench.enchantments.EnchantmentIcyThorns;
import shadows.apotheosis.ench.enchantments.EnchantmentKnowledge;
import shadows.apotheosis.ench.enchantments.EnchantmentLifeMend;
import shadows.apotheosis.ench.enchantments.EnchantmentMagicProt;
import shadows.apotheosis.ench.enchantments.EnchantmentNatureBless;
import shadows.apotheosis.ench.enchantments.EnchantmentRebounding;
import shadows.apotheosis.ench.enchantments.EnchantmentReflective;
import shadows.apotheosis.ench.enchantments.EnchantmentScavenger;
import shadows.apotheosis.ench.enchantments.EnchantmentShieldBash;
import shadows.apotheosis.ench.enchantments.EnchantmentStableFooting;
import shadows.apotheosis.ench.enchantments.EnchantmentTempting;
import shadows.apotheosis.ench.enchantments.HellInfusionEnchantment;
import shadows.apotheosis.ench.enchantments.SeaInfusionEnchantment;
import shadows.apotheosis.ench.objects.HellshelfBlock;
import shadows.apotheosis.ench.objects.HellshelfItem;
import shadows.apotheosis.ench.objects.ItemScrapTome;
import shadows.apotheosis.ench.objects.ItemShearsExt;
import shadows.apotheosis.ench.objects.ItemTypedBook;
import shadows.apotheosis.ench.objects.SeashelfBlock;
import shadows.apotheosis.ench.objects.SeashelfItem;
import shadows.apotheosis.ench.replacements.BaneEnchantment;
import shadows.apotheosis.ench.replacements.DefenseEnchantment;
import shadows.apotheosis.ench.table.EnchantingTableBlockExt;
import shadows.apotheosis.ench.table.EnchantingTableTileEntityExt;
import shadows.apotheosis.ench.table.EnchantmentContainerExt;
import shadows.apotheosis.ench.table.EnchantmentStatRegistry;
import shadows.apotheosis.util.EnchantmentIngredient;
import shadows.placebo.config.Configuration;
import shadows.placebo.loot.LootSystem;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.ReflectionHelper;

/**
 * Short document on enchanting methods:
 * Item Enchantibility is tied to the number of enchantments the item will recieve, and the "level" passed to getRandomEnchantments.
 * The possible enchantabilities for an item are equal to:
 * [table level + 1, table level + 1 + (E/4 + 1) + (E/4 + 1)].  E == item enchantability.  (E/4 + 1) is rolled as a random int.
 *
 * Enchantment min/max enchantability should really be called something else, they aren't fully based on enchantability.
 * Enchantment rarity affects weight in WeightedRandom list picking.
 * Max table level is 100, 30 before better shelves.
 *
 */
public class EnchModule {

	public static final Map<Enchantment, EnchantmentInfo> ENCHANTMENT_INFO = new HashMap<>();
	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Enchantment");
	public static final List<ItemTypedBook> TYPED_BOOKS = new LinkedList<>();
	public static final DamageSource CORRUPTED = new DamageSource("apoth_corrupted").setDamageBypassesArmor().setDamageIsAbsolute();
	public static final EquipmentSlotType[] ARMOR = { EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET };
	public static final EnchantmentType HOE = EnchantmentType.create("HOE", i -> i instanceof HoeItem);
	public static final EnchantmentType SHIELD = EnchantmentType.create("SHIELD", i -> i instanceof ShieldItem);
	public static final EnchantmentType ANVIL = EnchantmentType.create("ANVIL", i -> i instanceof BlockItem && ((BlockItem) i).getBlock() instanceof AnvilBlock);
	static Configuration enchInfoConfig;

	@SubscribeEvent
	public void init(ApotheosisSetup e) {
		Configuration config = new Configuration(new File(Apotheosis.configDir, "enchantment_module.cfg"));
		if (config.hasChanged()) config.save();

		config = new Configuration(new File(Apotheosis.configDir, "enchantments.cfg"));
		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			int max = config.getInt("Max Level", ench.getRegistryName().toString(), getDefaultMax(ench), 1, 127, "The max level of this enchantment - normally " + ench.getMaxLevel() + ".");
			int min = config.getInt("Min Level", ench.getRegistryName().toString(), ench.getMinLevel(), 1, 127, "The min level of this enchantment.");
			if (min > max) min = max;
			EnchantmentInfo info = new EnchantmentInfo(ench, max, min);
			String maxF = config.getString("Max Power Function", ench.getRegistryName().toString(), "", "A function to determine the max enchanting power.  The variable \"x\" is level.  See: https://github.com/uklimaschewski/EvalEx#usage-examples");
			if (!maxF.isEmpty()) info.setMaxPower(new ExpressionPowerFunc(maxF));
			String minF = config.getString("Min Power Function", ench.getRegistryName().toString(), "", "A function to determine the min enchanting power.");
			if (!minF.isEmpty()) info.setMinPower(new ExpressionPowerFunc(minF));
			ENCHANTMENT_INFO.put(ench, info);
		}
		if (config.hasChanged()) config.save();
		enchInfoConfig = config;

		Ingredient pot = Apotheosis.potionIngredient(Potions.REGENERATION);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.HELLSHELF, 3, 3, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Items.BLAZE_ROD, "forge:bookshelves", pot, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.PRISMATIC_WEB, 3, 3, null, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, Blocks.COBWEB, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, null);
		ItemStack book = new ItemStack(Items.BOOK);
		ItemStack stick = new ItemStack(Items.STICK);
		ItemStack blaze = new ItemStack(Items.BLAZE_ROD);
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.ARMOR_HEAD_BOOK, 5), 3, 2, book, book, book, book, blaze, book);
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.ARMOR_CHEST_BOOK, 8), 3, 3, book, blaze, book, book, book, book, book, book, book);
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.ARMOR_LEGS_BOOK, 7), 3, 3, book, null, book, book, blaze, book, book, book, book);
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.ARMOR_FEET_BOOK, 4), 3, 2, book, null, book, book, blaze, book);
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.WEAPON_BOOK, 2), 1, 3, book, book, new ItemStack(Items.BLAZE_POWDER));
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.DIGGER_BOOK, 3), 3, 3, book, book, book, null, blaze, null, null, stick, null);
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.FISHING_ROD_BOOK, 2), 3, 3, null, null, blaze, null, stick, book, stick, null, book);
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.BOW_BOOK, 3), 3, 3, null, stick, book, blaze, null, book, null, stick, book);
		Apotheosis.HELPER.addShapeless(new ItemStack(ApotheosisObjects.NULL_BOOK, 6), book, book, book, book, book, book, blaze);
		ItemStack msBrick = new ItemStack(Blocks.MOSSY_STONE_BRICKS);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.PRISMATIC_ALTAR, 3, 3, msBrick, null, msBrick, msBrick, Items.SEA_LANTERN, msBrick, msBrick, Blocks.ENCHANTING_TABLE, msBrick);
		Apotheosis.HELPER.addShaped(new ItemStack(ApotheosisObjects.SCRAP_TOME, 8), 3, 3, book, book, book, book, Blocks.ANVIL, book, book, book, book);
		Ingredient maxHellshelf = new EnchantmentIngredient(ApotheosisObjects.HELLSHELF, ApotheosisObjects.HELL_INFUSION, Math.min(5, getEnchInfo(ApotheosisObjects.HELL_INFUSION).getMaxLevel()));
		Apotheosis.HELPER.addShaped(ApotheosisObjects.BLAZING_HELLSHELF, 3, 3, null, Items.FIRE_CHARGE, null, Items.FIRE_CHARGE, maxHellshelf, Items.FIRE_CHARGE, Items.BLAZE_POWDER, Items.BLAZE_POWDER, Items.BLAZE_POWDER);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.GLOWING_HELLSHELF, 3, 3, null, Blocks.GLOWSTONE, null, null, maxHellshelf, null, Blocks.GLOWSTONE, null, Blocks.GLOWSTONE);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.SEASHELF, 3, 3, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Apotheosis.potionIngredient(Potions.WATER), "forge:bookshelves", Items.PUFFERFISH, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS);
		Ingredient maxSeashelf = new EnchantmentIngredient(ApotheosisObjects.SEASHELF, ApotheosisObjects.SEA_INFUSION, Math.min(5, getEnchInfo(ApotheosisObjects.SEA_INFUSION).getMaxLevel()));
		Apotheosis.HELPER.addShaped(ApotheosisObjects.CRYSTAL_SEASHELF, 3, 3, null, Items.PRISMARINE_CRYSTALS, null, null, maxSeashelf, null, Items.PRISMARINE_CRYSTALS, null, Items.PRISMARINE_CRYSTALS);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.HEART_SEASHELF, 3, 3, null, Items.HEART_OF_THE_SEA, null, Items.PRISMARINE_SHARD, maxSeashelf, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.ENDSHELF, 3, 3, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Items.DRAGON_BREATH, "forge:bookshelves", Items.ENDER_PEARL, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.PEARL_ENDSHELF, 3, 3, Items.END_ROD, null, Items.END_ROD, Items.ENDER_PEARL, ApotheosisObjects.ENDSHELF, Items.ENDER_PEARL, Items.END_ROD, null, Items.END_ROD);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.DRACONIC_ENDSHELF, 3, 3, null, Items.DRAGON_HEAD, null, Items.ENDER_PEARL, ApotheosisObjects.ENDSHELF, Items.ENDER_PEARL, Items.ENDER_PEARL, Items.ENDER_PEARL, Items.ENDER_PEARL);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.BEESHELF, 3, 3, Items.field_226635_pU_, Items.BEEHIVE, Items.field_226635_pU_, Items.HONEY_BLOCK, "forge:bookshelves", Items.HONEY_BLOCK, Items.field_226635_pU_, Items.BEEHIVE, Items.field_226635_pU_);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.MELONSHELF, 3, 3, Items.MELON, Items.MELON, Items.MELON, Items.GLISTERING_MELON_SLICE, "forge:bookshelves", Items.GLISTERING_MELON_SLICE, Items.MELON, Items.MELON, Items.MELON);
		Apotheosis.HELPER.addShaped(Items.EXPERIENCE_BOTTLE, 3, 3, Items.GLOWSTONE, "forge:gems/diamond", Items.GLOWSTONE, Items.ENCHANTED_BOOK, Items.field_226638_pX_, Items.ENCHANTED_BOOK, Items.GLOWSTONE, "forge:gems/diamond", Items.GLOWSTONE);

		LootSystem.defaultBlockTable(ApotheosisObjects.PRISMATIC_ALTAR);
		LootSystem.defaultBlockTable(ApotheosisObjects.BLAZING_HELLSHELF);
		LootSystem.defaultBlockTable(ApotheosisObjects.GLOWING_HELLSHELF);
		LootSystem.defaultBlockTable(ApotheosisObjects.CRYSTAL_SEASHELF);
		LootSystem.defaultBlockTable(ApotheosisObjects.HEART_SEASHELF);
		LootSystem.defaultBlockTable(ApotheosisObjects.ENDSHELF);
		LootSystem.defaultBlockTable(ApotheosisObjects.PEARL_ENDSHELF);
		LootSystem.defaultBlockTable(ApotheosisObjects.DRACONIC_ENDSHELF);
		LootSystem.defaultBlockTable(ApotheosisObjects.BEESHELF);
		LootSystem.defaultBlockTable(ApotheosisObjects.MELONSHELF);
		MinecraftForge.EVENT_BUS.register(this);
		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);
			for (int i = 1; i <= info.getMaxLevel(); i++)
				if (info.getMinPower(i) > info.getMaxPower(i)) LOGGER.error("Enchantment {} has min/max power {}/{} at level {}, making this level unobtainable.", ench.getRegistryName(), info.getMinPower(i), info.getMaxPower(i), i);
		}
		EnchantmentStatRegistry.init();
	}

	@SubscribeEvent
	public void client(ApotheosisClientSetup e) {
		MinecraftForge.EVENT_BUS.register(new EnchModuleClient());
		EnchModuleClient.init();
	}

	@SubscribeEvent
	public void tiles(Register<TileEntityType<?>> e) {
		e.getRegistry().register(new TileEntityType<>(TileAnvil::new, ImmutableSet.of(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL), null).setRegistryName("anvil"));
		e.getRegistry().register(new TileEntityType<>(TilePrismaticAltar::new, ImmutableSet.of(ApotheosisObjects.PRISMATIC_ALTAR), null).setRegistryName("prismatic_altar"));
		e.getRegistry().register(new TileEntityType<>(EnchantingTableTileEntityExt::new, ImmutableSet.of(Blocks.ENCHANTING_TABLE), null).setRegistryName("minecraft:enchanting_table"));
	}

	@SubscribeEvent
	public void containers(Register<ContainerType<?>> e) {
		e.getRegistry().register(new ContainerType<>(EnchantmentContainerExt::new).setRegistryName("enchanting"));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new BlockPrismaticAltar().setRegistryName(Apotheosis.MODID, "prismatic_altar"),
				new BlockAnvilExt().setRegistryName("minecraft", "anvil"),
				new BlockAnvilExt().setRegistryName("minecraft", "chipped_anvil"),
				new BlockAnvilExt().setRegistryName("minecraft", "damaged_anvil"),
				new HellshelfBlock().setRegistryName("hellshelf"),
				new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F).sound(SoundType.STONE)).setRegistryName("blazing_hellshelf"),
				new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F).sound(SoundType.STONE)).setRegistryName("glowing_hellshelf"),
				new SeashelfBlock().setRegistryName("seashelf"),
				new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F).sound(SoundType.STONE)).setRegistryName("crystal_seashelf"),
				new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F).sound(SoundType.STONE)).setRegistryName("heart_seashelf"),
				new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F).sound(SoundType.STONE)).setRegistryName("endshelf"),
				new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F).sound(SoundType.STONE)).setRegistryName("pearl_endshelf"),
				new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F).sound(SoundType.STONE)).setRegistryName("draconic_endshelf"),
				new Block(Block.Properties.create(Material.WOOD).hardnessAndResistance(1.5F).sound(SoundType.WOOD)).setRegistryName("beeshelf"),
				new Block(Block.Properties.create(Material.GOURD).hardnessAndResistance(1.5F).sound(SoundType.WOOD)).setRegistryName("melonshelf")
				);
		//Formatter::on
		PlaceboUtil.registerOverrideBlock(new EnchantingTableBlockExt().setRegistryName("minecraft:enchanting_table"), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		Item oldShears = Items.SHEARS;
		Item shears;
		//Formatter::off
		e.getRegistry().registerAll(
				shears = new ItemShearsExt(),
				new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(Apotheosis.MODID, "prismatic_web"),
				new ItemAnvilExt(Blocks.ANVIL),
				new ItemAnvilExt(Blocks.CHIPPED_ANVIL),
				new ItemAnvilExt(Blocks.DAMAGED_ANVIL),
				new ItemTypedBook(Items.AIR, null),
				new ItemTypedBook(Items.DIAMOND_HELMET, EnchantmentType.ARMOR_HEAD),
				new ItemTypedBook(Items.DIAMOND_CHESTPLATE, EnchantmentType.ARMOR_CHEST),
				new ItemTypedBook(Items.DIAMOND_LEGGINGS, EnchantmentType.ARMOR_LEGS),
				new ItemTypedBook(Items.DIAMOND_BOOTS, EnchantmentType.ARMOR_FEET),
				new ItemTypedBook(Items.DIAMOND_SWORD, EnchantmentType.WEAPON),
				new ItemTypedBook(Items.DIAMOND_PICKAXE, EnchantmentType.DIGGER),
				new ItemTypedBook(Items.FISHING_ROD, EnchantmentType.FISHING_ROD),
				new ItemTypedBook(Items.BOW, EnchantmentType.BOW),
				new BlockItem(ApotheosisObjects.PRISMATIC_ALTAR, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("prismatic_altar"),
				new ItemScrapTome(),
				new HellshelfItem(ApotheosisObjects.HELLSHELF).setRegistryName(ApotheosisObjects.HELLSHELF.getRegistryName()),
				new BlockItem(ApotheosisObjects.BLAZING_HELLSHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("blazing_hellshelf"),
				new BlockItem(ApotheosisObjects.GLOWING_HELLSHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("glowing_hellshelf"),
				new SeashelfItem(ApotheosisObjects.SEASHELF).setRegistryName(ApotheosisObjects.SEASHELF.getRegistryName()),
				new BlockItem(ApotheosisObjects.CRYSTAL_SEASHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("crystal_seashelf"),
				new BlockItem(ApotheosisObjects.HEART_SEASHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("heart_seashelf"),
				new BlockItem(ApotheosisObjects.ENDSHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("endshelf"),
				new BlockItem(ApotheosisObjects.DRACONIC_ENDSHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("draconic_endshelf"),
				new BlockItem(ApotheosisObjects.PEARL_ENDSHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("pearl_endshelf"),
				new BlockItem(ApotheosisObjects.BEESHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("beeshelf"),
				new BlockItem(ApotheosisObjects.MELONSHELF, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName("melonshelf")
				);
		//Formatter::on
		DispenserBlock.registerDispenseBehavior(shears, DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.get(oldShears));
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new HellInfusionEnchantment().setRegistryName(Apotheosis.MODID, "hell_infusion"),
				new EnchantmentDepths().setRegistryName(Apotheosis.MODID, "depth_miner"),
				new EnchantmentStableFooting().setRegistryName(Apotheosis.MODID, "stable_footing"),
				new EnchantmentScavenger().setRegistryName(Apotheosis.MODID, "scavenger"),
				new EnchantmentLifeMend().setRegistryName(Apotheosis.MODID, "life_mending"),
				new EnchantmentIcyThorns().setRegistryName(Apotheosis.MODID, "icy_thorns"),
				new EnchantmentTempting().setRegistryName(Apotheosis.MODID, "tempting"),
				new EnchantmentShieldBash().setRegistryName(Apotheosis.MODID, "shield_bash"),
				new EnchantmentReflective().setRegistryName(Apotheosis.MODID, "reflective"),
				new EnchantmentBerserk().setRegistryName(Apotheosis.MODID, "berserk"),
				new EnchantmentKnowledge().setRegistryName(Apotheosis.MODID, "knowledge"),
				new EnchantmentSplitting().setRegistryName(Apotheosis.MODID, "splitting"),
				new EnchantmentNatureBless().setRegistryName(Apotheosis.MODID, "natures_blessing"),
				new EnchantmentRebounding().setRegistryName(Apotheosis.MODID, "rebounding"),
				new EnchantmentMagicProt().setRegistryName(Apotheosis.MODID, "magic_protection"),
				new SeaInfusionEnchantment().setRegistryName("sea_infusion"),
				new BaneEnchantment(Rarity.UNCOMMON, CreatureAttribute.ARTHROPOD, EquipmentSlotType.MAINHAND).setRegistryName("minecraft", "bane_of_arthropods"),
				new BaneEnchantment(Rarity.UNCOMMON, CreatureAttribute.UNDEAD, EquipmentSlotType.MAINHAND).setRegistryName("minecraft", "smite"),
				new BaneEnchantment(Rarity.COMMON, CreatureAttribute.UNDEFINED, EquipmentSlotType.MAINHAND).setRegistryName("minecraft", "sharpness"),
				new BaneEnchantment(Rarity.UNCOMMON, CreatureAttribute.ILLAGER, EquipmentSlotType.MAINHAND).setRegistryName("bane_of_illagers"),
				new DefenseEnchantment(Rarity.COMMON, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "protection"),
				new DefenseEnchantment(Rarity.UNCOMMON, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "fire_protection"),
				new DefenseEnchantment(Rarity.RARE, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "blast_protection"),
				new DefenseEnchantment(Rarity.UNCOMMON, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "projectile_protection"),
				new DefenseEnchantment(Rarity.UNCOMMON, ProtectionEnchantment.Type.ALL, EquipmentSlotType.FEET).setRegistryName("minecraft", "feather_falling")
				);
		//Formatter::on
	}

	@SubscribeEvent
	public void sounds(Register<SoundEvent> e) {
		e.getRegistry().register(new SoundEvent(new ResourceLocation(Apotheosis.MODID, "altar")).setRegistryName(Apotheosis.MODID, "altar_sound"));
	}

	@SubscribeEvent
	public void anvilEvent(AnvilUpdateEvent e) {
		if (!EnchantmentHelper.getEnchantments(e.getLeft()).isEmpty()) {
			if (e.getRight().getItem() == Items.COBWEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> ent.getKey().isCurse()).collect(Collectors.toMap(ent -> ent.getKey(), ent -> ent.getValue())), stack);
				e.setCost(1);
				e.setMaterialCost(1);
				e.setOutput(stack);
			} else if (e.getRight().getItem() == ApotheosisObjects.PRISMATIC_WEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> !ent.getKey().isCurse()).collect(Collectors.toMap(ent -> ent.getKey(), ent -> ent.getValue())), stack);
				e.setCost(30);
				e.setMaterialCost(1);
				e.setOutput(stack);
				return;
			}
		}
		if ((e.getLeft().getItem() == Items.CHIPPED_ANVIL || e.getLeft().getItem() == Items.DAMAGED_ANVIL) && e.getRight().getItem().isIn(Tags.Items.STORAGE_BLOCKS_IRON)) {
			if (e.getLeft().getCount() != 1) return;
			int dmg = e.getLeft().getItem() == Items.DAMAGED_ANVIL ? 2 : 1;
			ItemStack out = new ItemStack(dmg == 1 ? Items.ANVIL : Items.CHIPPED_ANVIL);
			EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(e.getLeft()), out);
			out.setCount(1);
			e.setOutput(out);
			e.setCost(5 + EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, e.getLeft()) + EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.SPLITTING, e.getLeft()) * 2);
			e.setMaterialCost(1);
			return;
		}
		if (e.getLeft().getItem() == ApotheosisObjects.HELLSHELF.asItem() || e.getLeft().getItem() == ApotheosisObjects.SEASHELF.asItem()) {
			if (e.getLeft().getItem() != e.getRight().getItem() || e.getLeft().getCount() != 1) return;
			Enchantment ench = e.getLeft().getItem() == ApotheosisObjects.HELLSHELF.asItem() ? ApotheosisObjects.HELL_INFUSION : ApotheosisObjects.SEA_INFUSION;
			int leftLvl = EnchantmentHelper.getEnchantmentLevel(ench, e.getLeft());
			int rightLvl = EnchantmentHelper.getEnchantmentLevel(ench, e.getRight());
			if (leftLvl == 0 || rightLvl != leftLvl) return;
			if (leftLvl + 1 > EnchModule.getEnchInfo(ench).getMaxLevel()) return;
			ItemStack out = e.getLeft().copy();
			EnchantmentHelper.setEnchantments(ImmutableMap.of(ench, leftLvl + 1), out);
			out.setCount(1);
			e.setOutput(out);
			e.setCost(1 + leftLvl);
			e.setMaterialCost(1);
			return;
		}
		if (ItemTypedBook.updateAnvil(e)) return;
		if (ItemScrapTome.updateAnvil(e)) return;
	}

	Method dropLoot;

	/**
	 * Event handler for the Scavenger and Knowledge of the Ages enchantments.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void drops(LivingDropsEvent e) throws Exception {
		Entity attacker = e.getSource().getTrueSource();
		if (attacker instanceof PlayerEntity) {
			PlayerEntity p = (PlayerEntity) attacker;
			if (p.world.isRemote) return;
			int scavenger = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.SCAVENGER, p.getHeldItemMainhand());
			if (scavenger > 0 && p.world.rand.nextInt(100) < scavenger * 2.5F) {
				if (dropLoot == null) {
					dropLoot = ReflectionHelper.findMethod(LivingEntity.class, "dropLoot", "func_213354_a", DamageSource.class, boolean.class);
				}
				dropLoot.invoke(e.getEntityLiving(), e.getSource(), true);
			}
			int knowledge = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.KNOWLEDGE, p.getHeldItemMainhand());
			if (knowledge > 0 && !(e.getEntity() instanceof PlayerEntity)) {
				int items = 0;
				for (ItemEntity i : e.getDrops())
					items += i.getItem().getCount();
				if (items > 0) e.getDrops().clear();
				items *= knowledge * 25;
				Entity ded = e.getEntityLiving();
				while (items > 0) {
					int i = ExperienceOrbEntity.getXPSplit(items);
					items -= i;
					p.world.addEntity(new ExperienceOrbEntity(p.world, ded.getX(), ded.getY(), ded.getZ(), i));
				}
			}
		}
	}

	final EquipmentSlotType[] slots = EquipmentSlotType.values();

	/**
	 * Event handler for the Life Mending enchantment
	 */
	@SubscribeEvent
	public void lifeMend(LivingUpdateEvent e) {
		if (e.getEntity().world.isRemote || e.getEntity().ticksExisted % 20 != 0) return;
		for (EquipmentSlotType slot : slots) {
			ItemStack stack = e.getEntityLiving().getItemStackFromSlot(slot);
			if (!stack.isEmpty() && stack.isDamaged()) {
				int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.LIFE_MENDING, stack);
				if (level > 0) {
					int i = Math.min(level, stack.getDamage());
					e.getEntityLiving().attackEntityFrom(CORRUPTED, i * 0.7F);
					stack.setDamage(stack.getDamage() - i);
					return;
				}
			}
		}
	}

	/**
	 * Event handler for the Stable Footing and Miner's Fervor enchants.
	 */
	@SubscribeEvent
	public void breakSpeed(PlayerEvent.BreakSpeed e) {
		PlayerEntity p = e.getPlayer();
		if (!p.onGround && EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.STABLE_FOOTING, p) > 0) {
			if (e.getOriginalSpeed() < e.getNewSpeed() * 5) e.setNewSpeed(e.getNewSpeed() * 5F);
		}
		ItemStack stack = p.getHeldItemMainhand();
		if (stack.isEmpty()) return;
		int depth = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.DEPTH_MINER, stack);
		if (depth > 0) {
			if (stack.getDestroySpeed(e.getState()) > 1.0F) {
				float hardness = e.getState().getBlockHardness(e.getPlayer().world, e.getPos());
				e.setNewSpeed(Math.min(29.99F, 7.5F + 4.5F * depth) * hardness);
			}
		}
	}

	/**
	 * Event handler for the Nature's Blessing enchantment.
	 */
	@SubscribeEvent
	public void rightClick(PlayerInteractEvent.RightClickBlock e) {
		ItemStack s = e.getItemStack();
		int nbLevel = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.NATURES_BLESSING, s);
		if (!e.getEntity().isSneaking() && nbLevel > 0 && BoneMealItem.applyBonemeal(s.copy(), e.getWorld(), e.getPos(), e.getPlayer())) {
			s.damageItem(6 - nbLevel, e.getPlayer(), ent -> ent.sendBreakAnimation(e.getHand()));
			e.setCanceled(true);
			e.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	/**
	 * Event handler for Anvil Unbreaking.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void applyUnbreaking(AnvilRepairEvent e) {
		if (e.getPlayer().openContainer instanceof RepairContainer) {
			RepairContainer r = (RepairContainer) e.getPlayer().openContainer;
			TileEntity te = r.field_216980_g.apply((w, p) -> w.getTileEntity(p)).orElse(null);
			if (te instanceof TileAnvil) e.setBreakChance(e.getBreakChance() / (((TileAnvil) te).getUnbreaking() + 1));
		}
	}

	/**
	 * Handles the Berserker's Fury and Occult Aversion enchantments.
	 */
	@SubscribeEvent
	public void livingHurt(LivingHurtEvent e) {
		LivingEntity user = e.getEntityLiving();
		if (e.getSource().getTrueSource() instanceof Entity && user.getActivePotionEffect(Effects.RESISTANCE) == null) {
			int level = EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.BERSERK, user);
			if (level > 0) {
				user.attackEntityFrom(EnchModule.CORRUPTED, level * level);
				user.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 200 * level, level - 1));
				user.addPotionEffect(new EffectInstance(Effects.STRENGTH, 200 * level, level - 1));
				user.addPotionEffect(new EffectInstance(Effects.SPEED, 200 * level, level - 1));
			}
		}
		if (e.getSource().isMagicDamage() && e.getSource().getTrueSource() instanceof LivingEntity) {
			LivingEntity src = (LivingEntity) e.getSource().getTrueSource();
			int lvl = EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.MAGIC_PROTECTION, src);
			if (lvl > 0) {
				//TODO: FIXME should only be reducing damage by the value of OA, this will use all active protection enchantments.
				e.setAmount(CombatRules.getDamageAfterMagicAbsorb(e.getAmount(), EnchantmentHelper.getEnchantmentModifierDamage(src.getArmorInventoryList(), e.getSource())));
			}
		}
	}

	public static EnchantmentInfo getEnchInfo(Enchantment ench) {
		EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);

		if (!Apotheosis.enableEnch) {
			return ENCHANTMENT_INFO.computeIfAbsent(ench, e -> new EnchantmentInfo(e, e.getMaxLevel(), e.getMinLevel()));
		}

		if (enchInfoConfig == null) {
			LOGGER.error("A mod has attempted to access enchantment information before Apotheosis init, this should not happen.");
			Thread.dumpStack();
			return new EnchantmentInfo(ench, ench.getMaxLevel(), ench.getMinLevel());
		}
		if (info == null) {
			int max = enchInfoConfig.getInt("Max Level", ench.getRegistryName().toString(), getDefaultMax(ench), 1, 127, "The max level of this enchantment - normally " + ench.getMaxLevel() + ".");
			int min = enchInfoConfig.getInt("Min Level", ench.getRegistryName().toString(), ench.getMinLevel(), 1, 127, "The min level of this enchantment.");
			if (min > max) min = max;
			info = new EnchantmentInfo(ench, max, min);
			String maxF = enchInfoConfig.getString("Max Power Function", ench.getRegistryName().toString(), "", "A function to determine the max enchanting power.  The variable \"x\" is level.  See: https://github.com/uklimaschewski/EvalEx#usage-examples");
			if (!maxF.isEmpty()) info.setMaxPower(new ExpressionPowerFunc(maxF));
			String minF = enchInfoConfig.getString("Min Power Function", ench.getRegistryName().toString(), "", "A function to determine the min enchanting power.");
			if (!minF.isEmpty()) info.setMinPower(new ExpressionPowerFunc(minF));
			ENCHANTMENT_INFO.put(ench, info);
			if (enchInfoConfig.hasChanged()) enchInfoConfig.save();
			LOGGER.error("Had to late load enchantment info for {}, this is a bug in the mod {} as they are registering late!", ench.getRegistryName(), ench.getRegistryName().getNamespace());
		}
		return info;
	}

	/**
	 * Tries to find a max level for this enchantment.  This is used to scale up default levels to the Apoth cap.
	 */
	public static int getDefaultMax(Enchantment ench) {
		int level = ench.getMaxLevel();
		if (level == 1) return 1;
		int minPower = ench.getMinEnchantability(level);
		if (minPower >= 150) return level;
		int lastPower = minPower; //Need this to check that we don't get locked up on static-power enchantments.
		while (minPower < 150) {
			++level;
			int diff = ench.getMinEnchantability(level) - ench.getMinEnchantability(level - 1);
			minPower = level > ench.getMaxLevel() ? ench.getMinEnchantability(level) + diff * (int) Math.pow(level - ench.getMaxLevel(), 1.6) : ench.getMinEnchantability(level);
			if (lastPower == minPower) {
				level--;
				break;
			}
			lastPower = minPower;
		}
		return level;
	}

}
