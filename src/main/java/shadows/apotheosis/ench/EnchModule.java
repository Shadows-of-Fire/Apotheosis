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
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.ench.EnchantmentInfo.PowerFunc;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.anvil.ApothAnvilBlock;
import shadows.apotheosis.ench.anvil.ApothAnvilItem;
import shadows.apotheosis.ench.anvil.ObliterationEnchant;
import shadows.apotheosis.ench.anvil.SplittingEnchant;
import shadows.apotheosis.ench.enchantments.ChromaticEnchant;
import shadows.apotheosis.ench.enchantments.IcyThornsEnchant;
import shadows.apotheosis.ench.enchantments.InertEnchantment;
import shadows.apotheosis.ench.enchantments.NaturesBlessingEnchant;
import shadows.apotheosis.ench.enchantments.ReboundingEnchant;
import shadows.apotheosis.ench.enchantments.ReflectiveEnchant;
import shadows.apotheosis.ench.enchantments.ShieldBashEnchant;
import shadows.apotheosis.ench.enchantments.StableFootingEnchant;
import shadows.apotheosis.ench.enchantments.TemptingEnchant;
import shadows.apotheosis.ench.enchantments.corrupted.BerserkersFuryEnchant;
import shadows.apotheosis.ench.enchantments.corrupted.LifeMendingEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.CrescendoEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.EndlessQuiverEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.GrowthSerumEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.KnowledgeEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.ScavengerEnchant;
import shadows.apotheosis.ench.enchantments.twisted.ExploitationEnchant;
import shadows.apotheosis.ench.enchantments.twisted.MinersFervorEnchant;
import shadows.apotheosis.ench.library.EnchLibraryBlock;
import shadows.apotheosis.ench.library.EnchLibraryContainer;
import shadows.apotheosis.ench.library.EnchLibraryTile;
import shadows.apotheosis.ench.objects.ApothShearsItem;
import shadows.apotheosis.ench.objects.GlowyItem;
import shadows.apotheosis.ench.objects.ScrappingTomeItem;
import shadows.apotheosis.ench.objects.TomeItem;
import shadows.apotheosis.ench.replacements.BaneEnchant;
import shadows.apotheosis.ench.replacements.DefenseEnchant;
import shadows.apotheosis.ench.table.ApothEnchantBlock;
import shadows.apotheosis.ench.table.ApothEnchantContainer;
import shadows.apotheosis.ench.table.ApothEnchantTile;
import shadows.apotheosis.ench.table.EnchantingRecipe;
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
	public static final EquipmentSlot[] ARMOR = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
	public static final EnchantmentCategory HOE = EnchantmentCategory.create("HOE", i -> i instanceof HoeItem);
	public static final EnchantmentCategory SHIELD = EnchantmentCategory.create("SHIELD", i -> i instanceof ShieldItem);
	public static final EnchantmentCategory ANVIL = EnchantmentCategory.create("ANVIL", i -> i instanceof BlockItem && ((BlockItem) i).getBlock() instanceof AnvilBlock);
	public static final EnchantmentCategory SHEARS = EnchantmentCategory.create("SHEARS", i -> i instanceof ShearsItem);
	static Configuration enchInfoConfig;

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		this.reload(null);

		Ingredient pot = Apotheosis.potionIngredient(Potions.REGENERATION);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.HELLSHELF, 3, 3, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Items.BLAZE_ROD, "forge:bookshelves", pot, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS);
		Apotheosis.HELPER.addShaped(Apoth.Items.PRISMATIC_WEB, 3, 3, null, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, Blocks.COBWEB, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, null);
		ItemStack book = new ItemStack(Items.BOOK);
		ItemStack stick = new ItemStack(Items.STICK);
		ItemStack blaze = new ItemStack(Items.BLAZE_ROD);
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.HELMET_TOME, 5), 3, 2, book, book, book, book, blaze, book);
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.CHESTPLATE_TOME, 8), 3, 3, book, blaze, book, book, book, book, book, book, book);
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.LEGGINGS_TOME, 7), 3, 3, book, null, book, book, blaze, book, book, book, book);
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.BOOTS_TOME, 4), 3, 2, book, null, book, book, blaze, book);
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.WEAPON_TOME, 2), 1, 3, book, book, new ItemStack(Items.BLAZE_POWDER));
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.PICKAXE_TOME, 3), 3, 3, book, book, book, null, blaze, null, null, stick, null);
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.FISHING_TOME, 2), 3, 3, null, null, blaze, null, stick, book, stick, null, book);
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.BOW_TOME, 3), 3, 3, null, stick, book, blaze, null, book, null, stick, book);
		Apotheosis.HELPER.addShapeless(new ItemStack(Apoth.Items.OTHER_TOME, 6), book, book, book, book, book, book, blaze);
		Apotheosis.HELPER.addShaped(new ItemStack(Apoth.Items.SCRAP_TOME, 8), 3, 3, book, book, book, book, Blocks.ANVIL, book, book, book, book);
		Ingredient maxHellshelf = Ingredient.of(Apoth.Blocks.INFUSED_HELLSHELF);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.BLAZING_HELLSHELF, 3, 3, null, Items.FIRE_CHARGE, null, Items.FIRE_CHARGE, maxHellshelf, Items.FIRE_CHARGE, Items.BLAZE_POWDER, Items.BLAZE_POWDER, Items.BLAZE_POWDER);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.GLOWING_HELLSHELF, 3, 3, null, Blocks.GLOWSTONE, null, null, maxHellshelf, null, Blocks.GLOWSTONE, null, Blocks.GLOWSTONE);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.SEASHELF, 3, 3, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Apotheosis.potionIngredient(Potions.WATER), "forge:bookshelves", Items.PUFFERFISH, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS);
		Ingredient maxSeashelf = Ingredient.of(Apoth.Blocks.INFUSED_SEASHELF);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.CRYSTAL_SEASHELF, 3, 3, null, Items.PRISMARINE_CRYSTALS, null, null, maxSeashelf, null, Items.PRISMARINE_CRYSTALS, null, Items.PRISMARINE_CRYSTALS);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.HEART_SEASHELF, 3, 3, null, Items.HEART_OF_THE_SEA, null, Items.PRISMARINE_SHARD, maxSeashelf, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.ENDSHELF, 3, 3, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Items.DRAGON_BREATH, "forge:bookshelves", Items.ENDER_PEARL, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.PEARL_ENDSHELF, 3, 3, Items.END_ROD, null, Items.END_ROD, Items.ENDER_PEARL, Apoth.Blocks.ENDSHELF, Items.ENDER_PEARL, Items.END_ROD, null, Items.END_ROD);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.DRACONIC_ENDSHELF, 3, 3, null, Items.DRAGON_HEAD, null, Items.ENDER_PEARL, Apoth.Blocks.ENDSHELF, Items.ENDER_PEARL, Items.ENDER_PEARL, Items.ENDER_PEARL, Items.ENDER_PEARL);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.BEESHELF, 3, 3, Items.HONEYCOMB, Items.BEEHIVE, Items.HONEYCOMB, Items.HONEY_BLOCK, "forge:bookshelves", Items.HONEY_BLOCK, Items.HONEYCOMB, Items.BEEHIVE, Items.HONEYCOMB);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.MELONSHELF, 3, 3, Items.MELON, Items.MELON, Items.MELON, Items.GLISTERING_MELON_SLICE, "forge:bookshelves", Items.GLISTERING_MELON_SLICE, Items.MELON, Items.MELON, Items.MELON);
		Apotheosis.HELPER.addShaped(Items.EXPERIENCE_BOTTLE, 3, 3, Items.GLOWSTONE, "forge:gems/diamond", Items.GLOWSTONE, Items.ENCHANTED_BOOK, Items.HONEY_BOTTLE, Items.ENCHANTED_BOOK, Items.GLOWSTONE, "forge:gems/diamond", Items.GLOWSTONE);
		Apotheosis.HELPER.addShaped(Apoth.Blocks.LIBRARY, 3, 3, Blocks.ENDER_CHEST, Apoth.Blocks.HELLSHELF, Blocks.ENDER_CHEST, Apoth.Blocks.HELLSHELF, Blocks.ENCHANTING_TABLE, Apoth.Blocks.HELLSHELF, Blocks.ENDER_CHEST, Apoth.Blocks.HELLSHELF, Blocks.ENDER_CHEST);
		LootSystem.defaultBlockTable(Apoth.Blocks.BLAZING_HELLSHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.GLOWING_HELLSHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.CRYSTAL_SEASHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.HEART_SEASHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.ENDSHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.PEARL_ENDSHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.DRACONIC_ENDSHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.BEESHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.MELONSHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.LIBRARY);
		LootSystem.defaultBlockTable(Apoth.Blocks.RECTIFIER);
		LootSystem.defaultBlockTable(Apoth.Blocks.RECTIFIER_T2);
		LootSystem.defaultBlockTable(Apoth.Blocks.RECTIFIER_T3);
		LootSystem.defaultBlockTable(Apoth.Blocks.SIGHTSHELF);
		LootSystem.defaultBlockTable(Apoth.Blocks.SIGHTSHELF_T2);
		MinecraftForge.EVENT_BUS.register(new EnchModuleEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reload);
		e.enqueueWork(() -> {
			DispenserBlock.registerBehavior(Items.SHEARS, new ShearsDispenseItemBehavior());
		});
	}

	@SubscribeEvent
	public void client(FMLClientSetupEvent e) {
		MinecraftForge.EVENT_BUS.register(new EnchModuleClient());
		e.enqueueWork(EnchModuleClient::init);
	}

	@SubscribeEvent
	public void tiles(Register<BlockEntityType<?>> e) {
		e.getRegistry().register(new BlockEntityType<>(AnvilTile::new, ImmutableSet.of(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL), null).setRegistryName("anvil"));
		BlockEntityType.ENCHANTING_TABLE.factory = ApothEnchantTile::new;
		BlockEntityType.ENCHANTING_TABLE.validBlocks = ImmutableSet.of(Blocks.ENCHANTING_TABLE);
		e.getRegistry().register(new BlockEntityType<>(EnchLibraryTile::new, ImmutableSet.of(Apoth.Blocks.LIBRARY), null).setRegistryName("library"));
	}

	@SubscribeEvent
	public void containers(Register<MenuType<?>> e) {
		e.getRegistry().register(new MenuType<>(ApothEnchantContainer::new).setRegistryName("enchanting_table"));
		e.getRegistry().register(new MenuType<>((IContainerFactory<EnchLibraryContainer>) (id, inv, buf) -> new EnchLibraryContainer(id, inv, buf.readBlockPos())).setRegistryName("library"));
	}

	@SubscribeEvent
	public void recipeSerializers(Register<RecipeSerializer<?>> e) {
		e.getRegistry().register(EnchantingRecipe.SERIALIZER.setRegistryName("enchanting"));
	}

	/**
	 * This handles IMC events for the enchantment module. <br>
	 * Currently only one type is supported. A mod may pass a single {@link EnchantmentInstance} indicating the hard capped max level for an enchantment. <br>
	 * That pair must use the method {@link ENCH_HARD_CAP_IMC}.
	 */
	@SubscribeEvent
	public void handleIMC(InterModProcessEvent e) {
		e.getIMCStream(ENCH_HARD_CAP_IMC::equals).forEach(msg -> {
			try {
				EnchantmentInstance data = (EnchantmentInstance) msg.messageSupplier().get();
				if (data != null && data.enchantment != null && data.level > 0) {
					ENCH_HARD_CAPS.put(data.enchantment, data.level);
				} else LOGGER.error("Failed to process IMC message with method {} from {} (invalid values passed).", msg.method(), msg.senderModId());
			} catch (Exception ex) {
				LOGGER.error("Exception thrown during IMC message with method {} from {}.", msg.method(), msg.senderModId());
				ex.printStackTrace();
			}
		});
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new ApothAnvilBlock().setRegistryName("minecraft", "anvil"),
				new ApothAnvilBlock().setRegistryName("minecraft", "chipped_anvil"),
				new ApothAnvilBlock().setRegistryName("minecraft", "damaged_anvil"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("hellshelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("infused_hellshelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("blazing_hellshelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("glowing_hellshelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("seashelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("infused_seashelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("crystal_seashelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("heart_seashelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("endshelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("pearl_endshelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("draconic_endshelf"),
				new Block(BlockBehaviour.Properties.of(Material.WOOD).strength(1.5F).sound(SoundType.WOOD)).setRegistryName("beeshelf"),
				new Block(BlockBehaviour.Properties.of(Material.VEGETABLE).strength(1.5F).sound(SoundType.WOOD)).setRegistryName("melonshelf"),
				new EnchLibraryBlock().setRegistryName("library"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("rectifier"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("rectifier_t2"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("rectifier_t3"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("sightshelf"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)).setRegistryName("sightshelf_t2")
				);
		//Formatter::on
		PlaceboUtil.registerOverride(new ApothEnchantBlock(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new ApothShearsItem(),
				new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName(Apotheosis.MODID, "prismatic_web"),
				new ApothAnvilItem(Blocks.ANVIL),
				new ApothAnvilItem(Blocks.CHIPPED_ANVIL),
				new ApothAnvilItem(Blocks.DAMAGED_ANVIL),
				new TomeItem(Items.AIR, null).setRegistryName("other_tome"),
				new TomeItem(Items.DIAMOND_HELMET, EnchantmentCategory.ARMOR_HEAD).setRegistryName("helmet_tome"),
				new TomeItem(Items.DIAMOND_CHESTPLATE, EnchantmentCategory.ARMOR_CHEST).setRegistryName("chestplate_tome"),
				new TomeItem(Items.DIAMOND_LEGGINGS, EnchantmentCategory.ARMOR_LEGS).setRegistryName("leggings_tome"),
				new TomeItem(Items.DIAMOND_BOOTS, EnchantmentCategory.ARMOR_FEET).setRegistryName("boots_tome"),
				new TomeItem(Items.DIAMOND_SWORD, EnchantmentCategory.WEAPON).setRegistryName("weapon_tome"),
				new TomeItem(Items.DIAMOND_PICKAXE, EnchantmentCategory.DIGGER).setRegistryName("pickaxe_tome"),
				new TomeItem(Items.FISHING_ROD, EnchantmentCategory.FISHING_ROD).setRegistryName("fishing_tome"),
				new TomeItem(Items.BOW, EnchantmentCategory.BOW).setRegistryName("bow_tome"),
				new ScrappingTomeItem(),
				new BlockItem(Apoth.Blocks.HELLSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("hellshelf"),
				new GlowyItem(Apoth.Blocks.INFUSED_HELLSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("infused_hellshelf"),
				new BlockItem(Apoth.Blocks.BLAZING_HELLSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("blazing_hellshelf"),
				new BlockItem(Apoth.Blocks.GLOWING_HELLSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("glowing_hellshelf"),
				new BlockItem(Apoth.Blocks.SEASHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("seashelf"),
				new GlowyItem(Apoth.Blocks.INFUSED_SEASHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("infused_seashelf"),
				new BlockItem(Apoth.Blocks.CRYSTAL_SEASHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("crystal_seashelf"),
				new BlockItem(Apoth.Blocks.HEART_SEASHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("heart_seashelf"),
				new BlockItem(Apoth.Blocks.ENDSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("endshelf"),
				new BlockItem(Apoth.Blocks.DRACONIC_ENDSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("draconic_endshelf"),
				new BlockItem(Apoth.Blocks.PEARL_ENDSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("pearl_endshelf"),
				new BlockItem(Apoth.Blocks.BEESHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("beeshelf"),
				new BlockItem(Apoth.Blocks.MELONSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("melonshelf"),
				new BlockItem(Apoth.Blocks.LIBRARY, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("library"),
				new BlockItem(Apoth.Blocks.RECTIFIER, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("rectifier"),
				new BlockItem(Apoth.Blocks.RECTIFIER_T2, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("rectifier_t2"),
				new BlockItem(Apoth.Blocks.RECTIFIER_T3, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("rectifier_t3"),
				new BlockItem(Apoth.Blocks.SIGHTSHELF, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("sightshelf"),
				new BlockItem(Apoth.Blocks.SIGHTSHELF_T2, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("sightshelf_t2")
				);
		//Formatter::on
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new MinersFervorEnchant().setRegistryName("miners_fervor"),
				new StableFootingEnchant().setRegistryName("stable_footing"),
				new ScavengerEnchant().setRegistryName("scavenger"),
				new LifeMendingEnchant().setRegistryName("life_mending"),
				new IcyThornsEnchant().setRegistryName("icy_thorns"),
				new TemptingEnchant().setRegistryName("tempting"),
				new ShieldBashEnchant().setRegistryName("shield_bash"),
				new ReflectiveEnchant().setRegistryName("reflective"),
				new BerserkersFuryEnchant().setRegistryName("berserkers_fury"),
				new KnowledgeEnchant().setRegistryName("knowledge"),
				new SplittingEnchant().setRegistryName("splitting"),
				new NaturesBlessingEnchant().setRegistryName("natures_blessing"),
				new ReboundingEnchant().setRegistryName("rebounding"),
				new BaneEnchant(Rarity.UNCOMMON, MobType.ARTHROPOD, EquipmentSlot.MAINHAND).setRegistryName("minecraft", "bane_of_arthropods"),
				new BaneEnchant(Rarity.UNCOMMON, MobType.UNDEAD, EquipmentSlot.MAINHAND).setRegistryName("minecraft", "smite"),
				new BaneEnchant(Rarity.COMMON, MobType.UNDEFINED, EquipmentSlot.MAINHAND).setRegistryName("minecraft", "sharpness"),
				new BaneEnchant(Rarity.UNCOMMON, MobType.ILLAGER, EquipmentSlot.MAINHAND).setRegistryName("bane_of_illagers"),
				new DefenseEnchant(Rarity.COMMON, ProtectionEnchantment.Type.ALL, ARMOR).setRegistryName("minecraft", "protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.FIRE, ARMOR).setRegistryName("minecraft", "fire_protection"),
				new DefenseEnchant(Rarity.RARE, ProtectionEnchantment.Type.EXPLOSION, ARMOR).setRegistryName("minecraft", "blast_protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.PROJECTILE, ARMOR).setRegistryName("minecraft", "projectile_protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.FALL, EquipmentSlot.FEET).setRegistryName("minecraft", "feather_falling"),
				new ObliterationEnchant().setRegistryName("obliteration"),
				new CrescendoEnchant().setRegistryName("crescendo"),
				new InertEnchantment().setRegistryName("infusion"),
				new EndlessQuiverEnchant().setRegistryName("endless_quiver"),
				new ChromaticEnchant().setRegistryName("chromatic"),
				new ExploitationEnchant().setRegistryName("exploitation"),
				new GrowthSerumEnchant().setRegistryName("growth_serum")
				);
		//Formatter::on
	}

	@SubscribeEvent
	public void sounds(Register<SoundEvent> e) {
		e.getRegistry().register(new SoundEvent(new ResourceLocation(Apotheosis.MODID, "altar")).setRegistryName(Apotheosis.MODID, "altar_sound"));
	}

	@SuppressWarnings("deprecation")
	public static EnchantmentInfo getEnchInfo(Enchantment ench) {
		if (!Apotheosis.enableEnch) return ENCHANTMENT_INFO.computeIfAbsent(ench, EnchantmentInfo::new);

		EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);

		if (enchInfoConfig == null) { //Legitimate occurances can now happen, such as when vanilla calls fillItemGroup
			//LOGGER.error("A mod has attempted to access enchantment information before Apotheosis init, this should not happen.");
			//Thread.dumpStack();
			return new EnchantmentInfo(ench);
		}

		if (info == null) {
			info = EnchantmentInfo.load(ench, enchInfoConfig);
			ENCHANTMENT_INFO.put(ench, info);
			if (enchInfoConfig.hasChanged()) enchInfoConfig.save();
			LOGGER.error("Had to late load enchantment info for {}, this is a bug in the mod {} as they are registering late!", ench.getRegistryName(), ench.getRegistryName().getNamespace());
		}

		return info;
	}

	/**
	 * Tries to find a max level for this enchantment.  This is used to scale up default levels to the Apoth cap.
	 * Single-Level enchantments are not scaled.
	 * Barring that, enchantments are scaled using the {@link EnchantmentInfo#defaultMin(Enchantment)} until it is >= 150
	 */
	public static int getDefaultMax(Enchantment ench) {
		int level = ench.getMaxLevel();
		if (level == 1) return 1;
		PowerFunc func = EnchantmentInfo.defaultMin(ench);
		int minPower = func.getPower(level);
		if (minPower >= 150) return level;
		int lastPower = minPower;
		while (minPower < 150) {
			minPower = func.getPower(++level);
			if (lastPower == minPower) return level;
			lastPower = minPower;
		}
		return level;
	}

	public void reload(ApotheosisReloadEvent e) {
		enchInfoConfig = new Configuration(new File(Apotheosis.configDir, "enchantments.cfg"));
		ENCHANTMENT_INFO.clear();

		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			ENCHANTMENT_INFO.put(ench, EnchantmentInfo.load(ench, enchInfoConfig));
		}

		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);
			for (int i = 1; i <= info.getMaxLevel(); i++)
				if (info.getMinPower(i) > info.getMaxPower(i)) LOGGER.error("Enchantment {} has min/max power {}/{} at level {}, making this level unobtainable.", ench.getRegistryName(), info.getMinPower(i), info.getMaxPower(i), i);
		}

		if (e == null && enchInfoConfig.hasChanged()) enchInfoConfig.save();
	}

}