package shadows.apotheosis.ench;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.EnchantmentInfo.ExpressionPowerFunc;
import shadows.apotheosis.ench.altar.SeaAltarBlock;
import shadows.apotheosis.ench.altar.SeaAltarTile;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.anvil.ApothAnvilBlock;
import shadows.apotheosis.ench.anvil.ApothAnvilItem;
import shadows.apotheosis.ench.anvil.ObliterationEnchant;
import shadows.apotheosis.ench.anvil.SplittingEnchant;
import shadows.apotheosis.ench.enchantments.BerserkersFuryEnchant;
import shadows.apotheosis.ench.enchantments.CrescendoEnchant;
import shadows.apotheosis.ench.enchantments.HellInfusionEnchantment;
import shadows.apotheosis.ench.enchantments.IcyThornsEnchant;
import shadows.apotheosis.ench.enchantments.KnowledgeEnchant;
import shadows.apotheosis.ench.enchantments.LifeMendingEnchant;
import shadows.apotheosis.ench.enchantments.MagicProtEnchant;
import shadows.apotheosis.ench.enchantments.MinersFervorEnchant;
import shadows.apotheosis.ench.enchantments.NaturesBlessingEnchant;
import shadows.apotheosis.ench.enchantments.ReboundingEnchant;
import shadows.apotheosis.ench.enchantments.ReflectiveEnchant;
import shadows.apotheosis.ench.enchantments.ScavengerEnchant;
import shadows.apotheosis.ench.enchantments.SeaInfusionEnchantment;
import shadows.apotheosis.ench.enchantments.ShieldBashEnchant;
import shadows.apotheosis.ench.enchantments.StableFootingEnchant;
import shadows.apotheosis.ench.enchantments.TemptingEnchant;
import shadows.apotheosis.ench.library.EnchLibraryBlock;
import shadows.apotheosis.ench.library.EnchLibraryContainer;
import shadows.apotheosis.ench.library.EnchLibraryTile;
import shadows.apotheosis.ench.objects.ApothShearsItem;
import shadows.apotheosis.ench.objects.HellshelfBlock;
import shadows.apotheosis.ench.objects.HellshelfItem;
import shadows.apotheosis.ench.objects.ScrappingTomeItem;
import shadows.apotheosis.ench.objects.SeashelfBlock;
import shadows.apotheosis.ench.objects.SeashelfItem;
import shadows.apotheosis.ench.objects.TomeItem;
import shadows.apotheosis.ench.replacements.BaneEnchant;
import shadows.apotheosis.ench.replacements.DefenseEnchant;
import shadows.apotheosis.ench.table.ApothEnchantBlock;
import shadows.apotheosis.ench.table.ApothEnchantContainer;
import shadows.apotheosis.ench.table.ApothEnchantTile;
import shadows.apotheosis.util.EnchantmentIngredient;
import shadows.placebo.config.Configuration;
import shadows.placebo.loot.LootSystem;
import shadows.placebo.util.PlaceboUtil;

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
	public static final Object2IntMap<Enchantment> ENCH_HARD_CAPS = new Object2IntOpenHashMap<>();
	public static final String ENCH_HARD_CAP_IMC = "set_ench_hard_cap";
	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Enchantment");
	public static final List<TomeItem> TYPED_BOOKS = new ArrayList<>();
	public static final DamageSource CORRUPTED = new DamageSource("apoth_corrupted").bypassArmor().bypassMagic();
	public static final EquipmentSlotType[] ARMOR = { EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET };
	public static final EnchantmentType HOE = EnchantmentType.create("HOE", i -> i instanceof HoeItem);
	public static final EnchantmentType SHIELD = EnchantmentType.create("SHIELD", i -> i instanceof ShieldItem);
	public static final EnchantmentType ANVIL = EnchantmentType.create("ANVIL", i -> i instanceof BlockItem && ((BlockItem) i).getBlock() instanceof AnvilBlock);
	static Configuration enchInfoConfig;

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		this.reload(null);

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
		Ingredient maxHellshelf = new EnchantmentIngredient(ApotheosisObjects.HELLSHELF, ApotheosisObjects.HELL_INFUSION, Math.min(3, getEnchInfo(ApotheosisObjects.HELL_INFUSION).getMaxLevel()));
		Apotheosis.HELPER.addShaped(ApotheosisObjects.BLAZING_HELLSHELF, 3, 3, null, Items.FIRE_CHARGE, null, Items.FIRE_CHARGE, maxHellshelf, Items.FIRE_CHARGE, Items.BLAZE_POWDER, Items.BLAZE_POWDER, Items.BLAZE_POWDER);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.GLOWING_HELLSHELF, 3, 3, null, Blocks.GLOWSTONE, null, null, maxHellshelf, null, Blocks.GLOWSTONE, null, Blocks.GLOWSTONE);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.SEASHELF, 3, 3, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Apotheosis.potionIngredient(Potions.WATER), "forge:bookshelves", Items.PUFFERFISH, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS);
		Ingredient maxSeashelf = new EnchantmentIngredient(ApotheosisObjects.SEASHELF, ApotheosisObjects.SEA_INFUSION, Math.min(3, getEnchInfo(ApotheosisObjects.SEA_INFUSION).getMaxLevel()));
		Apotheosis.HELPER.addShaped(ApotheosisObjects.CRYSTAL_SEASHELF, 3, 3, null, Items.PRISMARINE_CRYSTALS, null, null, maxSeashelf, null, Items.PRISMARINE_CRYSTALS, null, Items.PRISMARINE_CRYSTALS);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.HEART_SEASHELF, 3, 3, null, Items.HEART_OF_THE_SEA, null, Items.PRISMARINE_SHARD, maxSeashelf, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.ENDSHELF, 3, 3, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Items.DRAGON_BREATH, "forge:bookshelves", Items.ENDER_PEARL, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.PEARL_ENDSHELF, 3, 3, Items.END_ROD, null, Items.END_ROD, Items.ENDER_PEARL, ApotheosisObjects.ENDSHELF, Items.ENDER_PEARL, Items.END_ROD, null, Items.END_ROD);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.DRACONIC_ENDSHELF, 3, 3, null, Items.DRAGON_HEAD, null, Items.ENDER_PEARL, ApotheosisObjects.ENDSHELF, Items.ENDER_PEARL, Items.ENDER_PEARL, Items.ENDER_PEARL, Items.ENDER_PEARL);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.BEESHELF, 3, 3, Items.HONEYCOMB, Items.BEEHIVE, Items.HONEYCOMB, Items.HONEY_BLOCK, "forge:bookshelves", Items.HONEY_BLOCK, Items.HONEYCOMB, Items.BEEHIVE, Items.HONEYCOMB);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.MELONSHELF, 3, 3, Items.MELON, Items.MELON, Items.MELON, Items.GLISTERING_MELON_SLICE, "forge:bookshelves", Items.GLISTERING_MELON_SLICE, Items.MELON, Items.MELON, Items.MELON);
		Apotheosis.HELPER.addShaped(Items.EXPERIENCE_BOTTLE, 3, 3, Items.GLOWSTONE, "forge:gems/diamond", Items.GLOWSTONE, Items.ENCHANTED_BOOK, Items.HONEY_BOTTLE, Items.ENCHANTED_BOOK, Items.GLOWSTONE, "forge:gems/diamond", Items.GLOWSTONE);
		Apotheosis.HELPER.addShaped(ApotheosisObjects.ENCHANTMENT_LIBRARY, 3, 3, Blocks.ENDER_CHEST, ApotheosisObjects.HELLSHELF, Blocks.ENDER_CHEST, ApotheosisObjects.HELLSHELF, Blocks.ENCHANTING_TABLE, ApotheosisObjects.HELLSHELF, Blocks.ENDER_CHEST, ApotheosisObjects.HELLSHELF, Blocks.ENDER_CHEST);
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
		LootSystem.defaultBlockTable(ApotheosisObjects.ENCHANTMENT_LIBRARY);
		MinecraftForge.EVENT_BUS.register(new EnchModuleEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reload);
	}

	@SubscribeEvent
	public void client(FMLClientSetupEvent e) {
		MinecraftForge.EVENT_BUS.register(new EnchModuleClient());
		e.enqueueWork(EnchModuleClient::init);
	}

	@SubscribeEvent
	public void tiles(Register<TileEntityType<?>> e) {
		e.getRegistry().register(new TileEntityType<>(AnvilTile::new, ImmutableSet.of(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL), null).setRegistryName("anvil"));
		e.getRegistry().register(new TileEntityType<>(SeaAltarTile::new, ImmutableSet.of(ApotheosisObjects.PRISMATIC_ALTAR), null).setRegistryName("prismatic_altar"));
		e.getRegistry().register(new TileEntityType<>(ApothEnchantTile::new, ImmutableSet.of(Blocks.ENCHANTING_TABLE), null).setRegistryName("minecraft:enchanting_table"));
		e.getRegistry().register(new TileEntityType<>(EnchLibraryTile::new, ImmutableSet.of(ApotheosisObjects.ENCHANTMENT_LIBRARY), null).setRegistryName("ench_lib_tile"));
	}

	@SubscribeEvent
	public void containers(Register<ContainerType<?>> e) {
		e.getRegistry().register(new ContainerType<>(ApothEnchantContainer::new).setRegistryName("enchanting"));
		e.getRegistry().register(new ContainerType<>((IContainerFactory<EnchLibraryContainer>) (id, inv, buf) -> new EnchLibraryContainer(id, inv, buf.readBlockPos())).setRegistryName("ench_lib_con"));
	}

	/**
	 * This handles IMC events for the enchantment module. <br>
	 * Currently only one type is supported.  A mod may pass a single {@link EnchantmentData} indicating the hard capped max level for an enchantment. <br>
	 * That pair must use the method {@link ENCH_HARD_CAP_IMC}.
	 */
	@SubscribeEvent
	public void handleIMC(InterModProcessEvent e) {
		e.getIMCStream(ENCH_HARD_CAP_IMC::equals).forEach(msg -> {
			try {
				EnchantmentData data = msg.<EnchantmentData>getMessageSupplier().get();
				if (data != null && data.enchantment != null && data.level > 0) {
					ENCH_HARD_CAPS.put(data.enchantment, data.level);
				} else LOGGER.error("Failed to process IMC message with method {} from {} (invalid values passed).", msg.getMethod(), msg.getSenderModId());
			} catch (Exception ex) {
				LOGGER.error("Exception thrown during IMC message with method {} from {}.", msg.getMethod(), msg.getSenderModId());
				ex.printStackTrace();
			}
		});
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new SeaAltarBlock().setRegistryName(Apotheosis.MODID, "prismatic_altar"),
				new ApothAnvilBlock().setRegistryName("minecraft", "anvil"),
				new ApothAnvilBlock().setRegistryName("minecraft", "chipped_anvil"),
				new ApothAnvilBlock().setRegistryName("minecraft", "damaged_anvil"),
				new HellshelfBlock().setRegistryName("hellshelf"),
				new Block(AbstractBlock.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("blazing_hellshelf"),
				new Block(AbstractBlock.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("glowing_hellshelf"),
				new SeashelfBlock().setRegistryName("seashelf"),
				new Block(AbstractBlock.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("crystal_seashelf"),
				new Block(AbstractBlock.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("heart_seashelf"),
				new Block(AbstractBlock.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("endshelf"),
				new Block(AbstractBlock.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("pearl_endshelf"),
				new Block(AbstractBlock.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("draconic_endshelf"),
				new Block(AbstractBlock.Properties.of(Material.WOOD).strength(1.5F).sound(SoundType.WOOD)).setRegistryName("beeshelf"),
				new Block(AbstractBlock.Properties.of(Material.VEGETABLE).strength(1.5F).sound(SoundType.WOOD)).setRegistryName("melonshelf"),
				new EnchLibraryBlock().setRegistryName("enchantment_library")
				);
		//Formatter::on
		PlaceboUtil.registerOverride(new ApothEnchantBlock(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		Item oldShears = Items.SHEARS;
		Item shears;
		//Formatter::off
		e.getRegistry().registerAll(
				shears = new ApothShearsItem(),
				new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName(Apotheosis.MODID, "prismatic_web"),
				new ApothAnvilItem(Blocks.ANVIL),
				new ApothAnvilItem(Blocks.CHIPPED_ANVIL),
				new ApothAnvilItem(Blocks.DAMAGED_ANVIL),
				new TomeItem(Items.AIR, null),
				new TomeItem(Items.DIAMOND_HELMET, EnchantmentType.ARMOR_HEAD),
				new TomeItem(Items.DIAMOND_CHESTPLATE, EnchantmentType.ARMOR_CHEST),
				new TomeItem(Items.DIAMOND_LEGGINGS, EnchantmentType.ARMOR_LEGS),
				new TomeItem(Items.DIAMOND_BOOTS, EnchantmentType.ARMOR_FEET),
				new TomeItem(Items.DIAMOND_SWORD, EnchantmentType.WEAPON),
				new TomeItem(Items.DIAMOND_PICKAXE, EnchantmentType.DIGGER),
				new TomeItem(Items.FISHING_ROD, EnchantmentType.FISHING_ROD),
				new TomeItem(Items.BOW, EnchantmentType.BOW),
				new BlockItem(ApotheosisObjects.PRISMATIC_ALTAR, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("prismatic_altar"),
				new ScrappingTomeItem(),
				new HellshelfItem(ApotheosisObjects.HELLSHELF).setRegistryName(ApotheosisObjects.HELLSHELF.getRegistryName()),
				new BlockItem(ApotheosisObjects.BLAZING_HELLSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("blazing_hellshelf"),
				new BlockItem(ApotheosisObjects.GLOWING_HELLSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("glowing_hellshelf"),
				new SeashelfItem(ApotheosisObjects.SEASHELF).setRegistryName(ApotheosisObjects.SEASHELF.getRegistryName()),
				new BlockItem(ApotheosisObjects.CRYSTAL_SEASHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("crystal_seashelf"),
				new BlockItem(ApotheosisObjects.HEART_SEASHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("heart_seashelf"),
				new BlockItem(ApotheosisObjects.ENDSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("endshelf"),
				new BlockItem(ApotheosisObjects.DRACONIC_ENDSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("draconic_endshelf"),
				new BlockItem(ApotheosisObjects.PEARL_ENDSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("pearl_endshelf"),
				new BlockItem(ApotheosisObjects.BEESHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("beeshelf"),
				new BlockItem(ApotheosisObjects.MELONSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("melonshelf"),
				new BlockItem(ApotheosisObjects.ENCHANTMENT_LIBRARY, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("enchantment_library")
				);
		//Formatter::on
		DispenserBlock.registerBehavior(shears, DispenserBlock.DISPENSER_REGISTRY.get(oldShears));
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new HellInfusionEnchantment().setRegistryName(Apotheosis.MODID, "hell_infusion"),
				new MinersFervorEnchant().setRegistryName(Apotheosis.MODID, "depth_miner"),
				new StableFootingEnchant().setRegistryName(Apotheosis.MODID, "stable_footing"),
				new ScavengerEnchant().setRegistryName(Apotheosis.MODID, "scavenger"),
				new LifeMendingEnchant().setRegistryName(Apotheosis.MODID, "life_mending"),
				new IcyThornsEnchant().setRegistryName(Apotheosis.MODID, "icy_thorns"),
				new TemptingEnchant().setRegistryName(Apotheosis.MODID, "tempting"),
				new ShieldBashEnchant().setRegistryName(Apotheosis.MODID, "shield_bash"),
				new ReflectiveEnchant().setRegistryName(Apotheosis.MODID, "reflective"),
				new BerserkersFuryEnchant().setRegistryName(Apotheosis.MODID, "berserk"),
				new KnowledgeEnchant().setRegistryName(Apotheosis.MODID, "knowledge"),
				new SplittingEnchant().setRegistryName(Apotheosis.MODID, "splitting"),
				new NaturesBlessingEnchant().setRegistryName(Apotheosis.MODID, "natures_blessing"),
				new ReboundingEnchant().setRegistryName(Apotheosis.MODID, "rebounding"),
				new MagicProtEnchant().setRegistryName(Apotheosis.MODID, "magic_protection"),
				new SeaInfusionEnchantment().setRegistryName("sea_infusion"),
				new BaneEnchant(Rarity.UNCOMMON, CreatureAttribute.ARTHROPOD, EquipmentSlotType.MAINHAND).setRegistryName("minecraft", "bane_of_arthropods"),
				new BaneEnchant(Rarity.UNCOMMON, CreatureAttribute.UNDEAD, EquipmentSlotType.MAINHAND).setRegistryName("minecraft", "smite"),
				new BaneEnchant(Rarity.COMMON, CreatureAttribute.UNDEFINED, EquipmentSlotType.MAINHAND).setRegistryName("minecraft", "sharpness"),
				new BaneEnchant(Rarity.UNCOMMON, CreatureAttribute.ILLAGER, EquipmentSlotType.MAINHAND).setRegistryName("bane_of_illagers"),
				new DefenseEnchant(Rarity.COMMON, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "fire_protection"),
				new DefenseEnchant(Rarity.RARE, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "blast_protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "projectile_protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.ALL, EquipmentSlotType.FEET).setRegistryName("minecraft", "feather_falling"),
				new ObliterationEnchant().setRegistryName("obliteration"),
				new CrescendoEnchant().setRegistryName("crescendo")
				);
		//Formatter::on
	}

	@SubscribeEvent
	public void sounds(Register<SoundEvent> e) {
		e.getRegistry().register(new SoundEvent(new ResourceLocation(Apotheosis.MODID, "altar")).setRegistryName(Apotheosis.MODID, "altar_sound"));
	}

	public static EnchantmentInfo getEnchInfo(Enchantment ench) {
		EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);

		if (!Apotheosis.enableEnch) {
			return ENCHANTMENT_INFO.computeIfAbsent(ench, e -> new EnchantmentInfo(e, e.getMaxLevel(), e.getMinLevel()));
		}

		if (enchInfoConfig == null) { //Legitimate occurances can now happen, such as when vanilla calls fillItemGroup
			//LOGGER.error("A mod has attempted to access enchantment information before Apotheosis init, this should not happen.");
			//Thread.dumpStack();
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
		int minPower = ench.getMinCost(level);
		if (minPower >= 150) return level;
		int lastPower = minPower; //Need this to check that we don't get locked up on static-power enchantments.
		while (minPower < 150) {
			++level;
			int diff = ench.getMinCost(level) - ench.getMinCost(level - 1);
			minPower = level > ench.getMaxLevel() ? ench.getMinCost(level) + diff * (int) Math.pow(level - ench.getMaxLevel(), 1.6) : ench.getMinCost(level);
			if (lastPower == minPower) {
				level--;
				break;
			}
			lastPower = minPower;
		}
		return level;
	}

	public void reload(ApotheosisReloadEvent e) {
		enchInfoConfig = new Configuration(new File(Apotheosis.configDir, "enchantments.cfg"));
		ENCHANTMENT_INFO.clear();

		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			int max = enchInfoConfig.getInt("Max Level", ench.getRegistryName().toString(), getDefaultMax(ench), 1, 127, "The max level of this enchantment - normally " + ench.getMaxLevel() + ".");
			int min = enchInfoConfig.getInt("Min Level", ench.getRegistryName().toString(), ench.getMinLevel(), 1, 127, "The min level of this enchantment.");
			if (min > max) min = max;
			EnchantmentInfo info = new EnchantmentInfo(ench, max, min);
			String maxF = enchInfoConfig.getString("Max Power Function", ench.getRegistryName().toString(), "", "A function to determine the max enchanting power.  The variable \"x\" is level.  See: https://github.com/uklimaschewski/EvalEx#usage-examples");
			if (!maxF.isEmpty()) info.setMaxPower(new ExpressionPowerFunc(maxF));
			String minF = enchInfoConfig.getString("Min Power Function", ench.getRegistryName().toString(), "", "A function to determine the min enchanting power.");
			if (!minF.isEmpty()) info.setMinPower(new ExpressionPowerFunc(minF));
			ENCHANTMENT_INFO.put(ench, info);
		}

		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);
			for (int i = 1; i <= info.getMaxLevel(); i++)
				if (info.getMinPower(i) > info.getMaxPower(i)) LOGGER.error("Enchantment {} has min/max power {}/{} at level {}, making this level unobtainable.", ench.getRegistryName(), info.getMinPower(i), info.getMaxPower(i), i);
		}

		if (e == null && enchInfoConfig.hasChanged()) enchInfoConfig.save();
	}

}