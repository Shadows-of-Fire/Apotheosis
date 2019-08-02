package shadows.ench;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreIngredient;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisPreInit;
import shadows.Apotheosis.ApotheosisRecipeEvent;
import shadows.ApotheosisObjects;
import shadows.deadly.gen.BossItem;
import shadows.ench.EnchantmentInfo.ExpressionPowerFunc;
import shadows.ench.altar.BlockPrismaticAltar;
import shadows.ench.altar.TilePrismaticAltar;
import shadows.ench.anvil.BlockAnvilExt;
import shadows.ench.anvil.EnchantmentSplitting;
import shadows.ench.anvil.ItemAnvilExt;
import shadows.ench.anvil.TileAnvil;
import shadows.ench.enchantments.EnchantmentBerserk;
import shadows.ench.enchantments.EnchantmentDepths;
import shadows.ench.enchantments.EnchantmentHellInfused;
import shadows.ench.enchantments.EnchantmentIcyThorns;
import shadows.ench.enchantments.EnchantmentKnowledge;
import shadows.ench.enchantments.EnchantmentLifeMend;
import shadows.ench.enchantments.EnchantmentMagicProt;
import shadows.ench.enchantments.EnchantmentMounted;
import shadows.ench.enchantments.EnchantmentNatureBless;
import shadows.ench.enchantments.EnchantmentRebounding;
import shadows.ench.enchantments.EnchantmentReflective;
import shadows.ench.enchantments.EnchantmentScavenger;
import shadows.ench.enchantments.EnchantmentShieldBash;
import shadows.ench.enchantments.EnchantmentStableFooting;
import shadows.ench.enchantments.EnchantmentTempting;
import shadows.ench.objects.BlockHellBookshelf;
import shadows.ench.objects.ItemHellBookshelf;
import shadows.ench.objects.ItemScrapTome;
import shadows.ench.objects.ItemShearsExt;
import shadows.ench.objects.ItemTypedBook;
import shadows.placebo.itemblock.ItemBlockBase;
import shadows.placebo.util.PlaceboUtil;
import shadows.util.NBTIngredient;

/**
 * Short document on enchanting methods:
 * Item Enchantibility is tied to the number of enchantments the item will recieve, and the "level" passed to getRandomEnchantments.
 * The possible enchantabilities for an item are equal to:
 * [table level + 1, table level + 1 + (E/4 + 1) + (E/4 + 1)].  E == item enchantability.  (E/4 + 1) is rolled as a random int.
 *
 * Enchantment min/max enchantability should really be called something else, they aren't fully based on enchantability.
 * Enchantment rarity affects weight in WeightedRandom list picking.
 * Max expected table level is 150, 40 before empowered shelves.
 *
 */
@SuppressWarnings("deprecation")
public class EnchModule {

	public static final Map<Enchantment, EnchantmentInfo> ENCHANTMENT_INFO = new HashMap<>();
	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Enchantment");
	public static final List<ItemTypedBook> TYPED_BOOKS = new LinkedList<>();
	public static final List<Enchantment> BLACKLISTED_ENCHANTS = new ArrayList<>();
	public static final DamageSource CORRUPTED = new DamageSource("corrupted") {
		@Override
		public ITextComponent getDeathMessage(EntityLivingBase entity) {
			return new TextComponentTranslation("death.apotheosis.corrupted", entity.getDisplayName());
		};
	}.setDamageBypassesArmor().setDamageIsAbsolute();
	public static final EntityEquipmentSlot[] ARMOR = { EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };

	public static float localAtkStrength = 1;
	static Configuration enchInfoConfig;
	public static OreIngredient blockIron;
	public static int absMax = 170;

	public static boolean allowWeb = true;
	public static float maxNormalPower = 20;
	public static float maxPower = 75;

	public static boolean itemMerging = false;

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		Configuration config = new Configuration(new File(Apotheosis.configDir, "enchantability.cfg"));
		setEnch(ToolMaterial.GOLD, 40);
		setEnch(ArmorMaterial.GOLD, 40);
		for (ArmorMaterial a : ArmorMaterial.values())
			setEnch(a, config.getInt(a.name(), "Enchantability - Armor", a.getEnchantability(), 0, Integer.MAX_VALUE, "The enchantability of this armor material."));
		for (ToolMaterial a : ToolMaterial.values())
			setEnch(a, config.getInt(a.name(), "Enchantability - Tools", a.getEnchantability(), 0, Integer.MAX_VALUE, "The enchantability of this tool material."));
		if (config.hasChanged()) config.save();

		config = new Configuration(new File(Apotheosis.configDir, "enchantment_module.cfg"));
		String[] blacklist = config.getStringList("Enchantment Blacklist", "general", new String[0], "A list of enchantments that are banned from the enchanting table and other natural sources.");
		for (String s : blacklist) {
			Enchantment ex = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ex == null) {
				LOGGER.error("Invalid enchantment blacklist entry {} will be ignored!", ex);
				continue;
			}
			BLACKLISTED_ENCHANTS.add(ex);
		}
		allowWeb = config.getBoolean("Enable Cobwebs", "general", allowWeb, "If cobwebs can be used in anvils to remove enchantments.");
		maxNormalPower = config.getFloat("Max Normal Power", "general", maxNormalPower, 0, Float.MAX_VALUE, "The maximum enchantment power a table can receive from normal sources.");
		maxPower = config.getFloat("Max Power", "general", maxPower, 0, Float.MAX_VALUE, "The maximum enchantment power a table can receive.");
		itemMerging = config.getBoolean("Item Merging", "general", false, "If any two enchanted items can be combined in an Anvil.");
		if (config.hasChanged()) config.save();

		recalcAbsMax();
		config = new Configuration(new File(Apotheosis.configDir, "enchantments.cfg"));
		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			int max = config.getInt("Max Level", ench.getRegistryName().toString(), getDefaultMax(ench), 1, 127, "The max level of this enchantment.");
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

		blockIron = new OreIngredient("blockIron");

		if (Apotheosis.enableDeadly) {
			BossItem.ARMOR_ENCHANTMENTS.add(ApotheosisObjects.BERSERK);
			BossItem.ARMOR_ENCHANTMENTS.add(ApotheosisObjects.LIFE_MENDING);
			BossItem.ARMOR_ENCHANTMENTS.add(ApotheosisObjects.ICY_THORNS);
			BossItem.ARMOR_ENCHANTMENTS.add(ApotheosisObjects.REBOUNDING);
			if (Apotheosis.enablePotion) BossItem.BOW_ENCHANTMENTS.add(ApotheosisObjects.TRUE_INFINITY);
			BossItem.SWORD_ENCHANTMENTS.add(ApotheosisObjects.SCAVENGER);
			BossItem.SWORD_ENCHANTMENTS.add(ApotheosisObjects.KNOWLEDGE);
			if (Apotheosis.enableSpawner) BossItem.SWORD_ENCHANTMENTS.add(ApotheosisObjects.CAPTURING);
			BossItem.SWORD_ENCHANTMENTS.add(ApotheosisObjects.HELL_INFUSION);
			BossItem.TOOL_ENCHANTMENTS.add(ApotheosisObjects.DEPTH_MINER);
		}
	}

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		GameRegistry.registerTileEntity(TileAnvil.class, new ResourceLocation(Apotheosis.MODID, "anvil"));
		GameRegistry.registerTileEntity(TilePrismaticAltar.class, new ResourceLocation(Apotheosis.MODID, "prismatic_altar"));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new BlockHellBookshelf(new ResourceLocation(Apotheosis.MODID, "hellshelf")),
				new BlockAnvilExt().setRegistryName("minecraft", "anvil"),
				new BlockPrismaticAltar()
				);
		//Formatter::on
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new ItemShearsExt().setRegistryName(Items.SHEARS.getRegistryName()).setTranslationKey("shears"),
				new ItemHellBookshelf(ApotheosisObjects.HELLSHELF).setRegistryName(ApotheosisObjects.HELLSHELF.getRegistryName()),
				new Item().setRegistryName(Apotheosis.MODID, "prismatic_web").setTranslationKey(Apotheosis.MODID + ".prismatic_web"),
				new ItemAnvilExt(Blocks.ANVIL),
				new ItemTypedBook(Items.AIR, null),
				new ItemTypedBook(Items.DIAMOND_HELMET, EnumEnchantmentType.ARMOR_HEAD),
				new ItemTypedBook(Items.DIAMOND_CHESTPLATE, EnumEnchantmentType.ARMOR_CHEST),
				new ItemTypedBook(Items.DIAMOND_LEGGINGS, EnumEnchantmentType.ARMOR_LEGS),
				new ItemTypedBook(Items.DIAMOND_BOOTS, EnumEnchantmentType.ARMOR_FEET),
				new ItemTypedBook(Items.DIAMOND_SWORD, EnumEnchantmentType.WEAPON),
				new ItemTypedBook(Items.DIAMOND_PICKAXE, EnumEnchantmentType.DIGGER),
				new ItemTypedBook(Items.FISHING_ROD, EnumEnchantmentType.FISHING_ROD),
				new ItemTypedBook(Items.BOW, EnumEnchantmentType.BOW),
				new ItemBlockBase(ApotheosisObjects.PRISMATIC_ALTAR),
				new ItemScrapTome()
				);
		//Formatter::on
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new EnchantmentHellInfused().setRegistryName(Apotheosis.MODID, "hell_infusion"),
				new EnchantmentMounted().setRegistryName(Apotheosis.MODID, "mounted_strike"),
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
				new EnchantmentMagicProt().setRegistryName(Apotheosis.MODID, "magic_protection")
				);
		//Formatter::on
	}

	@SubscribeEvent
	public void sounds(Register<SoundEvent> e) {
		e.getRegistry().register(new SoundEvent(new ResourceLocation(Apotheosis.MODID, "altar")).setRegistryName(Apotheosis.MODID, "altar_sound"));
	}

	@SubscribeEvent
	public void models(ModelRegistryEvent e) {
		PlaceboUtil.sMRL(ApotheosisObjects.HELLSHELF, 0, "normal");
	}

	@SubscribeEvent
	public void recipes(ApotheosisRecipeEvent e) {
		Ingredient pot = new NBTIngredient(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.REGENERATION));
		e.helper.addShaped(ApotheosisObjects.HELLSHELF, 3, 3, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Items.BLAZE_ROD, Blocks.BOOKSHELF, pot, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK);
		e.helper.addShaped(ApotheosisObjects.PRISMATIC_WEB, 3, 3, null, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, Blocks.WEB, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, null);
		ItemStack book = new ItemStack(Items.BOOK);
		ItemStack stick = new ItemStack(Items.STICK);
		ItemStack blaze = new ItemStack(Items.BLAZE_ROD);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.ARMOR_HEAD_BOOK, 5), 3, 2, book, book, book, book, blaze, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.ARMOR_CHEST_BOOK, 8), 3, 3, book, blaze, book, book, book, book, book, book, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.ARMOR_LEGS_BOOK, 7), 3, 3, book, null, book, book, blaze, book, book, book, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.ARMOR_FEET_BOOK, 4), 3, 2, book, null, book, book, blaze, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.WEAPON_BOOK, 2), 1, 3, book, book, blaze);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.DIGGER_BOOK, 3), 3, 3, book, book, book, null, blaze, null, null, stick, null);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.FISHING_ROD_BOOK, 2), 3, 3, null, null, blaze, null, stick, book, stick, null, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.BOW_BOOK, 3), 3, 3, null, stick, book, blaze, null, book, null, stick, book);
		e.helper.addShapeless(new ItemStack(ApotheosisObjects.NULL_BOOK, 4), book, book, book, book, blaze);
		ItemStack msBrick = new ItemStack(Blocks.STONEBRICK, 1, 1);
		e.helper.addShaped(ApotheosisObjects.PRISMATIC_ALTAR, 3, 3, msBrick, null, msBrick, msBrick, Blocks.SEA_LANTERN, msBrick, msBrick, Blocks.ENCHANTING_TABLE, msBrick);
		e.helper.addShaped(new ItemStack(Items.EXPERIENCE_BOTTLE, 16), 3, 3, Items.ENDER_EYE, Items.GOLD_NUGGET, Items.ENDER_EYE, Items.BLAZE_POWDER, Items.DRAGON_BREATH, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST, Items.GLOWSTONE_DUST, Items.GLOWSTONE_DUST);
		e.helper.addShaped(new ItemStack(Items.EXPERIENCE_BOTTLE, 1), 3, 3, Items.ENDER_EYE, Blocks.GOLD_BLOCK, Items.ENDER_EYE, Items.BLAZE_ROD, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER), Items.BLAZE_ROD, Blocks.GLOWSTONE, Blocks.GLOWSTONE, Blocks.GLOWSTONE);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.SCRAP_TOME, 8), 3, 3, book, book, book, book, Blocks.ANVIL, book, book, book, book);
	}

	@SubscribeEvent
	public void anvilEvent(AnvilUpdateEvent e) {
		if (!EnchantmentHelper.getEnchantments(e.getLeft()).isEmpty()) {
			if (allowWeb && e.getRight().getItem() == ApotheosisObjects.WEB) {
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
		if (e.getLeft().getItem() == ApotheosisObjects.ANVIL && blockIron.apply(e.getRight())) {
			int dmg = e.getLeft().getMetadata();
			if (dmg == 0 || e.getLeft().getCount() != 1) return;
			ItemStack out = e.getLeft().copy();
			out.setItemDamage(dmg - 1);
			out.setCount(1);
			e.setOutput(out);
			e.setCost(5 + EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, e.getLeft()) * 2 + EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.SPLITTING, e.getLeft()) * 3);
			e.setMaterialCost(1);
			return;
		}
		if (ItemTypedBook.updateAnvil(e)) return;
		if (ItemScrapTome.updateAnvil(e)) return;
		if (itemMerging && mergeAll(e)) return;
	}

	private boolean mergeAll(AnvilUpdateEvent ev) {
		ItemStack right = ev.getRight();
		ItemStack left = ev.getLeft();
		if (!right.isItemEnchanted() || !left.getItem().isEnchantable(left)) return false;
		Map<Enchantment, Integer> rightEnch = EnchantmentHelper.getEnchantments(right);
		Map<Enchantment, Integer> leftEnch = EnchantmentHelper.getEnchantments(left);
		int cost = 0;

		for (Enchantment ench : rightEnch.keySet()) {
			if (ench == null) continue;

			int level = rightEnch.get(ench);
			int curLevel = leftEnch.containsKey(ench) ? leftEnch.get(ench) : 0;
			if (level > 0 && level == curLevel) level = Math.min(EnchModule.getEnchInfo(ench).getMaxLevel(), level + 1);
			if (curLevel > level) level = curLevel;

			if (ench.canApply(left)) {
				boolean isCompat = true;
				for (Enchantment ench2 : leftEnch.keySet()) {
					if (ench != ench2 && !ench.isCompatibleWith(ench2)) isCompat = false;
				}
				if (!isCompat) return false;
				leftEnch.put(ench, level);
				int addition = 0;
				switch (ench.getRarity()) {
				case COMMON:
					addition += 2 * level;
					break;
				case UNCOMMON:
					addition += 4 * level;
					break;
				case RARE:
					addition += 6 * level;
					break;
				case VERY_RARE:
					addition += 12 * level;
				}
				cost += Math.max(1, addition / 2);
			}
		}
		if (cost > 0) {
			cost += left.getRepairCost();
			ItemStack out = left.copy();
			out.setRepairCost(left.getRepairCost() * 2 + 1);
			EnchantmentHelper.setEnchantments(leftEnch, out);
			ev.setMaterialCost(1);
			ev.setCost(cost);
			ev.setOutput(out);
			return true;
		}
		return false;
	}

	@SubscribeEvent
	public void trackCooldown(AttackEntityEvent e) {
		EntityPlayer p = e.getEntityPlayer();
		localAtkStrength = p.getCooledAttackStrength(0.5F);
	}

	Method dropLoot;

	@SubscribeEvent(priority = EventPriority.LOW)
	public void drops(LivingDropsEvent e) throws Exception {
		Entity attacker = e.getSource().getTrueSource();
		if (attacker instanceof EntityPlayer) {
			EntityPlayer p = (EntityPlayer) attacker;
			if (p.world.isRemote) return;
			int scavenger = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.SCAVENGER, p.getHeldItemMainhand());
			if (scavenger > 0 && p.world.rand.nextInt(100) < scavenger * 2.5F) {
				if (dropLoot == null) {
					dropLoot = ReflectionHelper.findMethod(EntityLivingBase.class, "dropLoot", "func_184610_a", boolean.class, int.class, DamageSource.class);
				}
				dropLoot.invoke(e.getEntityLiving(), true, e.getLootingLevel(), e.getSource());
			}
			int knowledge = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.KNOWLEDGE, p.getHeldItemMainhand());
			if (knowledge > 0) {
				int items = 0;
				for (EntityItem i : e.getDrops())
					items += i.getItem().getCount();
				if (items > 0) e.getDrops().clear();
				items *= knowledge * 25;
				Entity ded = e.getEntityLiving();
				while (items > 0) {
					int i = EntityXPOrb.getXPSplit(items);
					items -= i;
					p.world.spawnEntity(new EntityXPOrb(p.world, ded.posX, ded.posY, ded.posZ, i));
				}
			}
		}
	}

	final EntityEquipmentSlot[] slots = EntityEquipmentSlot.values();

	@SubscribeEvent
	public void lifeMend(LivingUpdateEvent e) {
		if (e.getEntity().world.isRemote) return;
		for (EntityEquipmentSlot slot : slots) {
			ItemStack stack = e.getEntityLiving().getItemStackFromSlot(slot);
			if (!stack.isEmpty() && stack.isItemDamaged()) {
				int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.LIFE_MENDING, stack);
				if (level > 0 && e.getEntityLiving().world.rand.nextInt(10) == 0) {
					int i = Math.min(level, stack.getItemDamage());
					e.getEntityLiving().attackEntityFrom(CORRUPTED, i * 0.7F);
					stack.setItemDamage(stack.getItemDamage() - i);
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public void breakSpeed(PlayerEvent.BreakSpeed e) {
		EntityPlayer p = e.getEntityPlayer();
		if (!p.onGround && EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.STABLE_FOOTING, p) > 0) {
			if (e.getOriginalSpeed() < e.getNewSpeed() * 5) e.setNewSpeed(e.getNewSpeed() * 5F);
		}
		ItemStack stack = p.getHeldItemMainhand();
		if (stack.isEmpty()) return;
		int depth = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.DEPTH_MINER, stack);
		if (depth > 0) {
			float effectiveness = (p.world.getSeaLevel() - (float) p.posY) / p.world.getSeaLevel();
			if (effectiveness < 0) effectiveness /= 3;
			float speedChange = 1 + depth * depth * effectiveness;
			e.setNewSpeed(e.getNewSpeed() + speedChange);
		}
	}

	@SubscribeEvent
	public void rightClick(PlayerInteractEvent.RightClickBlock e) {
		ItemStack s = e.getItemStack();
		int nbLevel = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.NATURES_BLESSING, s);
		if (!e.getEntityPlayer().isSneaking() && nbLevel > 0 && ItemDye.applyBonemeal(s.copy(), e.getWorld(), e.getPos(), e.getEntityPlayer(), e.getHand())) {
			s.damageItem(6 - nbLevel, e.getEntityPlayer());
			e.setCanceled(true);
			e.setCancellationResult(EnumActionResult.SUCCESS);
		}
	}

	@SubscribeEvent
	public void applyUnbreaking(AnvilRepairEvent e) {
		if (e.getEntityPlayer().openContainer instanceof ContainerRepair) {
			ContainerRepair r = (ContainerRepair) e.getEntityPlayer().openContainer;
			TileEntity te = r.world.getTileEntity(r.pos);
			if (te instanceof TileAnvil) e.setBreakChance(e.getBreakChance() / (((TileAnvil) te).getUnbreaking() + 1));
		}
	}

	@SubscribeEvent
	public void enchLevel(EnchantmentLevelSetEvent e) {
		int power = e.getPower();
		//Power * 2, Power * 1.5, Power * 1
		e.setLevel(MathHelper.floor(power * (1 + e.getEnchantRow() * 0.5F)));
	}

	@SubscribeEvent
	public void enchContainer(PlayerContainerEvent.Open e) {
		if (!e.getEntityPlayer().world.isRemote && e.getContainer().getClass() == ContainerEnchantment.class) {
			ContainerEnchantment old = (ContainerEnchantment) e.getContainer();
			ContainerEnchantmentExt newC = new ContainerEnchantmentExt(e.getEntityPlayer().inventory, old.world, old.position);
			newC.windowId = old.windowId;
			newC.addListener((EntityPlayerMP) e.getEntityPlayer());
			e.getEntityPlayer().openContainer = newC;
		}
	}

	@SubscribeEvent
	public void livingHurt(LivingHurtEvent e) {
		EntityLivingBase user = e.getEntityLiving();
		if (e.getSource().getTrueSource() instanceof Entity && user.getActivePotionEffect(MobEffects.RESISTANCE) == null) {
			int level = EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.BERSERK, user);
			if (level > 0) {
				user.attackEntityFrom(EnchModule.CORRUPTED, level * level);
				user.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 200 * level, level - 1));
				user.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 200 * level, level - 1));
				user.addPotionEffect(new PotionEffect(MobEffects.SPEED, 200 * level, level - 1));
			}
		}
		if (e.getSource().isMagicDamage() && e.getSource().getTrueSource() instanceof EntityLivingBase) {
			EntityLivingBase src = (EntityLivingBase) e.getSource().getTrueSource();
			int lvl = EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.MAGIC_PROTECTION, src);
			if (lvl > 0) {
				e.setAmount(CombatRules.getDamageAfterMagicAbsorb(e.getAmount(), EnchantmentHelper.getEnchantmentModifierDamage(src.getArmorInventoryList(), e.getSource())));
			}
		}
	}

	public static void setEnch(ToolMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ToolMaterial.class, mat, ench, "enchantability", "field_78008_j");
	}

	public static void setEnch(ArmorMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ArmorMaterial.class, mat, ench, "enchantability", "field_78055_h");
	}

	public static EnchantmentInfo getEnchInfo(Enchantment ench) {
		EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);
		if (enchInfoConfig == null) {
			LOGGER.error("A mod has attempted to access enchantment information before Apotheosis init, this should not happen.");
			Thread.dumpStack();
			return new EnchantmentInfo(ench, ench.getMaxLevel(), ench.getMinLevel());
		}
		if (info == null) {
			int max = enchInfoConfig.getInt("Max Level", ench.getRegistryName().toString(), ench.getMaxLevel(), 1, 127, "The max level of this enchantment.");
			int min = enchInfoConfig.getInt("Min Level", ench.getRegistryName().toString(), ench.getMinLevel(), 1, 127, "The min level of this enchantment.");
			if (ench == Enchantments.LURE) max = Enchantments.LURE.getMaxLevel();
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
		int maxPower = ench.getMaxEnchantability(level);
		if (maxPower >= absMax) return level;
		int lastMaxPower = maxPower; //Need this to check that we don't get locked up on single-level enchantments.
		while (maxPower < absMax) {
			maxPower = ench.getMaxEnchantability(++level);
			if (lastMaxPower == maxPower) {
				level--;
				break;
			}
			lastMaxPower = maxPower;
		}
		if (ench == Enchantments.SILK_TOUCH) return 1;
		return level;
	}

	static void recalcAbsMax() {
		int max = MathHelper.ceil(maxPower * 2);
		int maxEnch = 0;
		for (ToolMaterial m : ToolMaterial.values()) {
			maxEnch = Math.max(maxEnch, m.getEnchantability());
		}
		for (ArmorMaterial m : ArmorMaterial.values()) {
			maxEnch = Math.max(maxEnch, m.getEnchantability());
		}
		absMax = max + maxEnch / 2 + 3;
	}

}
