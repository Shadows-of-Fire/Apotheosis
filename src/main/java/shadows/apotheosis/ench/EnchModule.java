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
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
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
import shadows.apotheosis.ench.compat.EnchTOPPlugin;
import shadows.apotheosis.ench.enchantments.ChromaticEnchant;
import shadows.apotheosis.ench.enchantments.IcyThornsEnchant;
import shadows.apotheosis.ench.enchantments.InertEnchantment;
import shadows.apotheosis.ench.enchantments.NaturesBlessingEnchant;
import shadows.apotheosis.ench.enchantments.ReboundingEnchant;
import shadows.apotheosis.ench.enchantments.ReflectiveEnchant;
import shadows.apotheosis.ench.enchantments.ShieldBashEnchant;
import shadows.apotheosis.ench.enchantments.SpearfishingEnchant;
import shadows.apotheosis.ench.enchantments.StableFootingEnchant;
import shadows.apotheosis.ench.enchantments.TemptingEnchant;
import shadows.apotheosis.ench.enchantments.corrupted.BerserkersFuryEnchant;
import shadows.apotheosis.ench.enchantments.corrupted.LifeMendingEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.ChainsawEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.CrescendoEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.EarthsBoonEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.EndlessQuiverEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.GrowthSerumEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.KnowledgeEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.ScavengerEnchant;
import shadows.apotheosis.ench.enchantments.twisted.ExploitationEnchant;
import shadows.apotheosis.ench.enchantments.twisted.MinersFervorEnchant;
import shadows.apotheosis.ench.library.EnchLibraryBlock;
import shadows.apotheosis.ench.library.EnchLibraryContainer;
import shadows.apotheosis.ench.library.EnchLibraryTile.BasicLibraryTile;
import shadows.apotheosis.ench.library.EnchLibraryTile.EnderLibraryTile;
import shadows.apotheosis.ench.objects.ExtractionTomeItem;
import shadows.apotheosis.ench.objects.GlowyBlockItem;
import shadows.apotheosis.ench.objects.ImprovedScrappingTomeItem;
import shadows.apotheosis.ench.objects.ScrappingTomeItem;
import shadows.apotheosis.ench.objects.TomeItem;
import shadows.apotheosis.ench.replacements.BaneEnchant;
import shadows.apotheosis.ench.replacements.DefenseEnchant;
import shadows.apotheosis.ench.table.ApothEnchantBlock;
import shadows.apotheosis.ench.table.ApothEnchantContainer;
import shadows.apotheosis.ench.table.ApothEnchantTile;
import shadows.apotheosis.ench.table.EnchantingRecipe;
import shadows.apotheosis.ench.table.EnchantingStatManager;
import shadows.apotheosis.ench.table.KeepNBTEnchantingRecipe;
import shadows.placebo.config.Configuration;
import shadows.placebo.container.ContainerUtil;
import shadows.placebo.loot.LootSystem;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.RegistryEvent.Register;

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
	public static final EnchantmentCategory PICKAXE = EnchantmentCategory.create("PICKAXE", i -> i.canPerformAction(new ItemStack(i), ToolActions.PICKAXE_DIG));
	public static final EnchantmentCategory AXE = EnchantmentCategory.create("AXE", i -> i.canPerformAction(new ItemStack(i), ToolActions.AXE_DIG));
	public static final EnchantmentCategory ARMOR_CHEST_LEGS = EnchantmentCategory.create("ARMOR_CHEST_LEGS", i -> EnchantmentCategory.ARMOR_CHEST.canEnchant(i) || EnchantmentCategory.ARMOR_LEGS.canEnchant(i));
	static Configuration enchInfoConfig;

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		this.reload(null);

		Apotheosis.HELPER.registerProvider(factory -> {
			Ingredient pot = Apotheosis.potionIngredient(Potions.REGENERATION);
			factory.addShaped(Apoth.Blocks.HELLSHELF.get(), 3, 3, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Items.BLAZE_ROD, "forge:bookshelves", pot, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS);
			factory.addShaped(Apoth.Items.PRISMATIC_WEB, 3, 3, null, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, Blocks.COBWEB, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, null);
			ItemStack book = new ItemStack(Items.BOOK);
			ItemStack stick = new ItemStack(Items.STICK);
			ItemStack blaze = new ItemStack(Items.BLAZE_ROD);
			factory.addShaped(new ItemStack(Apoth.Items.HELMET_TOME.get(), 5), 3, 2, book, book, book, book, blaze, book);
			factory.addShaped(new ItemStack(Apoth.Items.CHESTPLATE_TOME.get(), 8), 3, 3, book, blaze, book, book, book, book, book, book, book);
			factory.addShaped(new ItemStack(Apoth.Items.LEGGINGS_TOME.get(), 7), 3, 3, book, null, book, book, blaze, book, book, book, book);
			factory.addShaped(new ItemStack(Apoth.Items.BOOTS_TOME.get(), 4), 3, 2, book, null, book, book, blaze, book);
			factory.addShaped(new ItemStack(Apoth.Items.WEAPON_TOME.get(), 2), 1, 3, book, book, new ItemStack(Items.BLAZE_POWDER));
			factory.addShaped(new ItemStack(Apoth.Items.PICKAXE_TOME.get(), 3), 3, 3, book, book, book, null, blaze, null, null, stick, null);
			factory.addShaped(new ItemStack(Apoth.Items.FISHING_TOME.get(), 2), 3, 3, null, null, blaze, null, stick, book, stick, null, book);
			factory.addShaped(new ItemStack(Apoth.Items.BOW_TOME.get(), 3), 3, 3, null, stick, book, blaze, null, book, null, stick, book);
			factory.addShapeless(new ItemStack(Apoth.Items.OTHER_TOME.get(), 6), book, book, book, book, book, book, blaze);
			factory.addShaped(new ItemStack(Apoth.Items.SCRAP_TOME.get(), 8), 3, 3, book, book, book, book, Blocks.ANVIL, book, book, book, book);
			Ingredient maxHellshelf = Ingredient.of(Apoth.Blocks.INFUSED_HELLSHELF.get());
			factory.addShaped(Apoth.Blocks.BLAZING_HELLSHELF.get(), 3, 3, null, Items.FIRE_CHARGE, null, Items.FIRE_CHARGE, maxHellshelf, Items.FIRE_CHARGE, Items.BLAZE_POWDER, Items.BLAZE_POWDER, Items.BLAZE_POWDER);
			factory.addShaped(Apoth.Blocks.GLOWING_HELLSHELF.get(), 3, 3, null, Blocks.GLOWSTONE, null, null, maxHellshelf, null, Blocks.GLOWSTONE, null, Blocks.GLOWSTONE);
			factory.addShaped(Apoth.Blocks.SEASHELF.get(), 3, 3, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Apotheosis.potionIngredient(Potions.WATER), "forge:bookshelves", Items.PUFFERFISH, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICKS);
			Ingredient maxSeashelf = Ingredient.of(Apoth.Blocks.INFUSED_SEASHELF.get());
			factory.addShaped(Apoth.Blocks.CRYSTAL_SEASHELF.get(), 3, 3, null, Items.PRISMARINE_CRYSTALS, null, null, maxSeashelf, null, Items.PRISMARINE_CRYSTALS, null, Items.PRISMARINE_CRYSTALS);
			factory.addShaped(Apoth.Blocks.HEART_SEASHELF.get(), 3, 3, null, Items.HEART_OF_THE_SEA, null, Items.PRISMARINE_SHARD, maxSeashelf, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD, Items.PRISMARINE_SHARD);
			factory.addShaped(Apoth.Blocks.ENDSHELF.get(), 3, 3, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Items.DRAGON_BREATH, "forge:bookshelves", Items.ENDER_PEARL, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS);
			factory.addShaped(Apoth.Blocks.PEARL_ENDSHELF.get(), 3, 3, Items.END_ROD, null, Items.END_ROD, Items.ENDER_PEARL, Apoth.Blocks.ENDSHELF.get(), Items.ENDER_PEARL, Items.END_ROD, null, Items.END_ROD);
			factory.addShaped(Apoth.Blocks.DRACONIC_ENDSHELF.get(), 3, 3, null, Items.DRAGON_HEAD, null, Items.ENDER_PEARL, Apoth.Blocks.ENDSHELF.get(), Items.ENDER_PEARL, Items.ENDER_PEARL, Items.ENDER_PEARL, Items.ENDER_PEARL);
			factory.addShaped(Apoth.Blocks.BEESHELF.get(), 3, 3, Items.HONEYCOMB, Items.BEEHIVE, Items.HONEYCOMB, Items.HONEY_BLOCK, "forge:bookshelves", Items.HONEY_BLOCK, Items.HONEYCOMB, Items.BEEHIVE, Items.HONEYCOMB);
			factory.addShaped(Apoth.Blocks.MELONSHELF.get(), 3, 3, Items.MELON, Items.MELON, Items.MELON, Items.GLISTERING_MELON_SLICE, "forge:bookshelves", Items.GLISTERING_MELON_SLICE, Items.MELON, Items.MELON, Items.MELON);
			factory.addShaped(Apoth.Blocks.LIBRARY.get(), 3, 3, Blocks.ENDER_CHEST, Apoth.Blocks.INFUSED_HELLSHELF.get(), Blocks.ENDER_CHEST, Apoth.Blocks.INFUSED_HELLSHELF.get(), Blocks.ENCHANTING_TABLE, Apoth.Blocks.INFUSED_HELLSHELF.get(), Blocks.ENDER_CHEST, Apoth.Blocks.INFUSED_HELLSHELF.get(), Blocks.ENDER_CHEST);
		});

		LootSystem.defaultBlockTable(Apoth.Blocks.HELLSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.INFUSED_HELLSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.BLAZING_HELLSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.GLOWING_HELLSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.SEASHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.INFUSED_SEASHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.CRYSTAL_SEASHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.HEART_SEASHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.ENDSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.PEARL_ENDSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.DRACONIC_ENDSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.BEESHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.MELONSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.LIBRARY.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.RECTIFIER.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.RECTIFIER_T2.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.RECTIFIER_T3.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.SIGHTSHELF.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.SIGHTSHELF_T2.get());
		LootSystem.defaultBlockTable(Apoth.Blocks.ENDER_LIBRARY.get());
		MinecraftForge.EVENT_BUS.register(new EnchModuleEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reload);
		e.enqueueWork(() -> {
			DispenserBlock.registerBehavior(Items.SHEARS, new ShearsDispenseItemBehavior());
		});
		if (ModList.get().isLoaded("theoneprobe")) EnchTOPPlugin.register();
		EnchantingStatManager.INSTANCE.registerToBus();
	}

	@SubscribeEvent
	public void client(FMLClientSetupEvent e) {
		MinecraftForge.EVENT_BUS.register(new EnchModuleClient());
		e.enqueueWork(EnchModuleClient::init);
	}

	@SubscribeEvent
	public void tiles(Register<BlockEntityType<?>> e) {
		e.getRegistry().register(new BlockEntityType<>(AnvilTile::new, ImmutableSet.of(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL), null), "anvil");
		BlockEntityType.ENCHANTING_TABLE.factory = ApothEnchantTile::new;
		BlockEntityType.ENCHANTING_TABLE.validBlocks = ImmutableSet.of(Blocks.ENCHANTING_TABLE);
		e.getRegistry().register(new BlockEntityType<>(BasicLibraryTile::new, ImmutableSet.of(Apoth.Blocks.LIBRARY.get()), null), "library");
		e.getRegistry().register(new BlockEntityType<>(EnderLibraryTile::new, ImmutableSet.of(Apoth.Blocks.ENDER_LIBRARY.get()), null), "ender_library");
	}

	@SubscribeEvent
	public void containers(Register<MenuType<?>> e) {
		e.getRegistry().register(new MenuType<>(ApothEnchantContainer::new), "enchanting_table");
		e.getRegistry().register(ContainerUtil.makeType(EnchLibraryContainer::new), "library");
	}

	@SubscribeEvent
	public void recipeSerializers(Register<RecipeSerializer<?>> e) {
		e.getRegistry().register(EnchantingRecipe.SERIALIZER, "enchanting");
		e.getRegistry().register(KeepNBTEnchantingRecipe.SERIALIZER, "keep_nbt_enchanting");
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
				new ApothAnvilBlock(), new ResourceLocation("minecraft", "anvil"),
				new ApothAnvilBlock(), new ResourceLocation("minecraft", "chipped_anvil"),
				new ApothAnvilBlock(), new ResourceLocation("minecraft", "damaged_anvil"),
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "hellshelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "infused_hellshelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "blazing_hellshelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "glowing_hellshelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "seashelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "infused_seashelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "crystal_seashelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "heart_seashelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "endshelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "pearl_endshelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "draconic_endshelf",
				new Block(BlockBehaviour.Properties.of(Material.WOOD).strength(1.5F).sound(SoundType.WOOD)), "beeshelf",
				new Block(BlockBehaviour.Properties.of(Material.VEGETABLE).strength(1.5F).sound(SoundType.WOOD)), "melonshelf",
				new EnchLibraryBlock(BasicLibraryTile::new, 16), "library",
				new EnchLibraryBlock(EnderLibraryTile::new, 31), "ender_library",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "rectifier",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "rectifier_t2",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "rectifier_t3",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "sightshelf",
				new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.STONE)), "sightshelf_t2"
				);
		//Formatter::on
		PlaceboUtil.registerOverride(Blocks.ENCHANTING_TABLE, new ApothEnchantBlock(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "prismatic_web",
				new ApothAnvilItem(Blocks.ANVIL), new ResourceLocation("minecraft", "anvil"),
				new ApothAnvilItem(Blocks.CHIPPED_ANVIL), new ResourceLocation("minecraft", "chipped_anvil"),
				new ApothAnvilItem(Blocks.DAMAGED_ANVIL), new ResourceLocation("minecraft", "damaged_anvil"),
				new TomeItem(Items.AIR, null), "other_tome",
				new TomeItem(Items.DIAMOND_HELMET, EnchantmentCategory.ARMOR_HEAD), "helmet_tome",
				new TomeItem(Items.DIAMOND_CHESTPLATE, EnchantmentCategory.ARMOR_CHEST), "chestplate_tome",
				new TomeItem(Items.DIAMOND_LEGGINGS, EnchantmentCategory.ARMOR_LEGS), "leggings_tome",
				new TomeItem(Items.DIAMOND_BOOTS, EnchantmentCategory.ARMOR_FEET), "boots_tome",
				new TomeItem(Items.DIAMOND_SWORD, EnchantmentCategory.WEAPON), "weapon_tome",
				new TomeItem(Items.DIAMOND_PICKAXE, EnchantmentCategory.DIGGER), "pickaxe_tome",
				new TomeItem(Items.FISHING_ROD, EnchantmentCategory.FISHING_ROD), "fishing_tome",
				new TomeItem(Items.BOW, EnchantmentCategory.BOW), "bow_tome",
				new ScrappingTomeItem(), "scrap_tome",
				new BlockItem(Apoth.Blocks.HELLSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "hellshelf",
				new GlowyBlockItem(Apoth.Blocks.INFUSED_HELLSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "infused_hellshelf",
				new BlockItem(Apoth.Blocks.BLAZING_HELLSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "blazing_hellshelf",
				new BlockItem(Apoth.Blocks.GLOWING_HELLSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "glowing_hellshelf",
				new BlockItem(Apoth.Blocks.SEASHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "seashelf",
				new GlowyBlockItem(Apoth.Blocks.INFUSED_SEASHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "infused_seashelf",
				new BlockItem(Apoth.Blocks.CRYSTAL_SEASHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "crystal_seashelf",
				new BlockItem(Apoth.Blocks.HEART_SEASHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "heart_seashelf",
				new BlockItem(Apoth.Blocks.ENDSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "endshelf",
				new BlockItem(Apoth.Blocks.DRACONIC_ENDSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "draconic_endshelf",
				new BlockItem(Apoth.Blocks.PEARL_ENDSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "pearl_endshelf",
				new BlockItem(Apoth.Blocks.BEESHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "beeshelf",
				new BlockItem(Apoth.Blocks.MELONSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "melonshelf",
				new BlockItem(Apoth.Blocks.LIBRARY.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "library",
				new BlockItem(Apoth.Blocks.RECTIFIER.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "rectifier",
				new BlockItem(Apoth.Blocks.RECTIFIER_T2.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "rectifier_t2",
				new BlockItem(Apoth.Blocks.RECTIFIER_T3.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "rectifier_t3",
				new BlockItem(Apoth.Blocks.SIGHTSHELF.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "sightshelf",
				new BlockItem(Apoth.Blocks.SIGHTSHELF_T2.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "sightshelf_t2",
				new Item(new Item.Properties().stacksTo(1).tab(Apotheosis.APOTH_GROUP)), "inert_trident",
				new BlockItem(Apoth.Blocks.ENDER_LIBRARY.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "ender_library",
				new ImprovedScrappingTomeItem(), "improved_scrap_tome",
				new ExtractionTomeItem(), "extraction_tome"
				);
		//Formatter::on
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new MinersFervorEnchant(), "miners_fervor",
				new StableFootingEnchant(), "stable_footing",
				new ScavengerEnchant(), "scavenger",
				new LifeMendingEnchant(), "life_mending",
				new IcyThornsEnchant(), "icy_thorns",
				new TemptingEnchant(), "tempting",
				new ShieldBashEnchant(), "shield_bash",
				new ReflectiveEnchant(), "reflective",
				new BerserkersFuryEnchant(), "berserkers_fury",
				new KnowledgeEnchant(), "knowledge",
				new SplittingEnchant(), "splitting",
				new NaturesBlessingEnchant(), "natures_blessing",
				new ReboundingEnchant(), "rebounding",
				new BaneEnchant(Rarity.UNCOMMON, MobType.ARTHROPOD, EquipmentSlot.MAINHAND), new ResourceLocation("minecraft", "bane_of_arthropods"),
				new BaneEnchant(Rarity.UNCOMMON, MobType.UNDEAD, EquipmentSlot.MAINHAND), new ResourceLocation("minecraft", "smite"),
				new BaneEnchant(Rarity.COMMON, MobType.UNDEFINED, EquipmentSlot.MAINHAND), new ResourceLocation("minecraft", "sharpness"),
				new BaneEnchant(Rarity.UNCOMMON, MobType.ILLAGER, EquipmentSlot.MAINHAND), "bane_of_illagers",
				new DefenseEnchant(Rarity.COMMON, ProtectionEnchantment.Type.ALL, ARMOR), new ResourceLocation("minecraft", "protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.FIRE, ARMOR), new ResourceLocation("minecraft", "fire_protection"),
				new DefenseEnchant(Rarity.RARE, ProtectionEnchantment.Type.EXPLOSION, ARMOR), new ResourceLocation("minecraft", "blast_protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.PROJECTILE, ARMOR), new ResourceLocation("minecraft", "projectile_protection"),
				new DefenseEnchant(Rarity.UNCOMMON, ProtectionEnchantment.Type.FALL, EquipmentSlot.FEET), new ResourceLocation("minecraft", "feather_falling"),
				new ObliterationEnchant(), "obliteration",
				new CrescendoEnchant(), "crescendo",
				new InertEnchantment(), "infusion",
				new EndlessQuiverEnchant(), "endless_quiver",
				new ChromaticEnchant(), "chromatic",
				new ExploitationEnchant(), "exploitation",
				new GrowthSerumEnchant(), "growth_serum",
				new EarthsBoonEnchant(), "earths_boon",
				new ChainsawEnchant(), "chainsaw",
				new SpearfishingEnchant(), "spearfishing"
				);
		//Formatter::on
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

		if (info == null) { // Should be impossible now.
			info = EnchantmentInfo.load(ench, enchInfoConfig);
			ENCHANTMENT_INFO.put(ench, info);
			if (enchInfoConfig.hasChanged()) enchInfoConfig.save();
			LOGGER.error("Had to late load enchantment info for {}, this is a bug in the mod {} as they are registering late!", ForgeRegistries.ENCHANTMENTS.getKey(ench), ForgeRegistries.ENCHANTMENTS.getKey(ench).getNamespace());
		}

		return info;
	}

	/**
	 * Tries to find a max level for this enchantment.  This is used to scale up default levels to the Apoth cap.
	 * Single-Level enchantments are not scaled.
	 * Barring that, enchantments are scaled using the {@link EnchantmentInfo#defaultMin(Enchantment)} until outside the default level space.
	 */
	public static int getDefaultMax(Enchantment ench) {
		int level = ench.getMaxLevel();
		if (level == 1) return 1;
		PowerFunc minFunc = EnchantmentInfo.defaultMin(ench);
		int max = (int) (EnchantingStatManager.getAbsoluteMaxEterna() * 4);
		int minPower = minFunc.getPower(level);
		if (minPower >= max) return level;
		int lastPower = minPower;
		while (minPower < max) {
			minPower = minFunc.getPower(++level);
			if (lastPower == minPower) return level;
			if (minPower > max) return level - 1;
			lastPower = minPower;
		}
		return level;
	}

	public void reload(ApotheosisReloadEvent e) {
		enchInfoConfig = new Configuration(new File(Apotheosis.configDir, "enchantments.cfg"));
		enchInfoConfig.setTitle("Apotheosis Enchantment Information");
		enchInfoConfig.setComment("This file contains configurable data for each enchantment.\nThe names of each category correspond to the registry names of every loaded enchantment.");
		ENCHANTMENT_INFO.clear();

		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			ENCHANTMENT_INFO.put(ench, EnchantmentInfo.load(ench, enchInfoConfig));
		}

		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);
			for (int i = 1; i <= info.getMaxLevel(); i++)
				if (info.getMinPower(i) > info.getMaxPower(i)) LOGGER.warn("Enchantment {} has min/max power {}/{} at level {}, making this level unobtainable.", ForgeRegistries.ENCHANTMENTS.getKey(ench), info.getMinPower(i), info.getMaxPower(i), i);
		}

		if (e == null && enchInfoConfig.hasChanged()) enchInfoConfig.save();
	}

}