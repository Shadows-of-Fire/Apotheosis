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
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
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
import shadows.ench.enchantments.EnchantmentMounted;
import shadows.ench.enchantments.EnchantmentNatureBless;
import shadows.ench.enchantments.EnchantmentRebounding;
import shadows.ench.enchantments.EnchantmentReflective;
import shadows.ench.enchantments.EnchantmentScavenger;
import shadows.ench.enchantments.EnchantmentShieldBash;
import shadows.ench.enchantments.EnchantmentStableFooting;
import shadows.ench.enchantments.EnchantmentTempting;
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
 * Max table level in Apotheosis is 256. Last Updated: (2/11/2019)
 *
 */
public class EnchModule {

	public static final Map<Enchantment, EnchantmentInfo> ENCHANTMENT_INFO = new HashMap<>();
	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Enchantment");
	public static final List<ItemTypedBook> TYPED_BOOKS = new LinkedList<>();
	public static final List<Enchantment> BLACKLISTED_ENCHANTS = new ArrayList<>();

	public static float localAtkStrength = 1;
	static Configuration config;
	public static OreIngredient blockIron;

	public static final DamageSource CORRUPTED = new DamageSource("corrupted") {
		@Override
		public ITextComponent getDeathMessage(EntityLivingBase entity) {
			return new TextComponentTranslation("death.apotheosis.corrupted", entity.getDisplayName());
		};
	}.setDamageBypassesArmor().setDamageIsAbsolute();

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		config = new Configuration(new File(Apotheosis.configDir, "enchantability.cfg"));
		setEnch(ToolMaterial.GOLD, 40);
		setEnch(ArmorMaterial.GOLD, 40);
		for (ArmorMaterial a : ArmorMaterial.values())
			setEnch(a, config.getInt(a.name(), "Enchantability - Armor", a.getEnchantability(), 0, Integer.MAX_VALUE, "The enchantability of this armor material."));
		for (ToolMaterial a : ToolMaterial.values())
			setEnch(a, config.getInt(a.name(), "Enchantability - Tools", a.getEnchantability(), 0, Integer.MAX_VALUE, "The enchantability of this tool material."));

		if (config.hasChanged()) config.save();
		config = new Configuration(new File(Apotheosis.configDir, "enchantments.cfg"));

		for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
			int max = config.getInt("Max Level", ench.getRegistryName().toString(), ench.getMaxLevel(), 1, 127, "The max level of this enchantment.");
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

		if (config.hasChanged()) config.save();

		blockIron = new OreIngredient("blockIron");

		if (Apotheosis.enableDeadly) {
			BossItem.ARMOR_ENCHANTMENTS.add(ApotheosisObjects.BERSERK);
			BossItem.ARMOR_ENCHANTMENTS.add(ApotheosisObjects.LIFE_MENDING);
			BossItem.ARMOR_ENCHANTMENTS.add(ApotheosisObjects.ICY_THORNS);
			BossItem.ARMOR_ENCHANTMENTS.add(ApotheosisObjects.REBOUNDING);
			BossItem.BOW_ENCHANTMENTS.add(ApotheosisObjects.TRUE_INFINITY);
			BossItem.SWORD_ENCHANTMENTS.add(ApotheosisObjects.SCAVENGER);
			BossItem.SWORD_ENCHANTMENTS.add(ApotheosisObjects.KNOWLEDGE);
			BossItem.SWORD_ENCHANTMENTS.add(ApotheosisObjects.CAPTURING);
			BossItem.SWORD_ENCHANTMENTS.add(ApotheosisObjects.HELL_INFUSION);
			BossItem.TOOL_ENCHANTMENTS.add(ApotheosisObjects.DEPTH_MINER);
		}
	}

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		GameRegistry.registerTileEntity(TileAnvil.class, new ResourceLocation(Apotheosis.MODID, "anvil"));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		e.getRegistry().register(new BlockHellBookshelf(new ResourceLocation(Apotheosis.MODID, "hellshelf")));
		e.getRegistry().register(new BlockAnvilExt());
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
				new ItemTypedBook(Items.BOW, EnumEnchantmentType.BOW)
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
				new EnchantmentRebounding().setRegistryName(Apotheosis.MODID, "rebounding"));
		//Formatter::on
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
		e.helper.addSimpleShapeless(new ItemStack(ApotheosisObjects.NULL_BOOK, 4), book, 4);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.ARMOR_HEAD_BOOK, 5), 3, 2, book, book, book, book, null, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.ARMOR_CHEST_BOOK, 8), 3, 3, book, null, book, book, book, book, book, book, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.ARMOR_LEGS_BOOK, 7), 3, 3, book, null, book, book, null, book, book, book, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.ARMOR_FEET_BOOK, 4), 3, 2, book, null, book, book, null, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.WEAPON_BOOK, 2), 1, 3, book, book, stick);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.DIGGER_BOOK, 3), 3, 3, book, book, book, null, stick, null, null, stick, null);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.FISHING_ROD_BOOK, 2), 3, 3, null, null, stick, null, stick, book, stick, null, book);
		e.helper.addShaped(new ItemStack(ApotheosisObjects.BOW_BOOK, 3), 3, 3, null, stick, book, stick, null, book, null, stick, book);
	}

	@SubscribeEvent
	public void anvilEvent(AnvilUpdateEvent e) {
		if (!EnchantmentHelper.getEnchantments(e.getLeft()).isEmpty()) {
			if (e.getRight().getItem() == ApotheosisObjects.WEB) {
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
		ItemTypedBook.updateAnvil(e);
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
				if (items > 0) e.getDrops().forEach(en -> en.setDead());
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

	public static void setEnch(ToolMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ToolMaterial.class, mat, ench, "enchantability", "field_78008_j");
	}

	public static void setEnch(ArmorMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ArmorMaterial.class, mat, ench, "enchantability", "field_78055_h");
	}

	/**
	 * Full redirect for EnchantmentHelper#getEnchantmentDatas
	 * @param power Enchanting power, pre-calculated, not table level.
	 * @param s ItemStack to be enchanted.
	 * @param allowTreasure If treasure enchants are allowed.
	 * @return The possible enchantment datas for this item.
	 */
	public static List<EnchantmentData> getEnchantmentDatas(int power, Object s, boolean allowTreasure) {
		ItemStack stack = (ItemStack) s;
		List<EnchantmentData> list = new ArrayList<>();
		boolean isBook = stack.getItem() == Items.BOOK;
		boolean typedBook = stack.getItem() instanceof ItemTypedBook;
		for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
			if (enchantment.isTreasureEnchantment() && !allowTreasure || BLACKLISTED_ENCHANTS.contains(enchantment)) continue;
			if (enchantment.canApplyAtEnchantingTable(stack) || isBook && enchantment.isAllowedOnBooks() || typedBook && stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
				EnchantmentInfo info = getEnchInfo(enchantment);
				for (int i = info.getMaxLevel(); i > info.getMinLevel() - 1; --i) {
					if (power >= info.getMinPower(i) && power <= info.getMaxPower(i)) {
						list.add(new EnchantmentData(enchantment, i));
						break;
					}
				}
			}
		}
		return list;
	}

	/**
	 * Hook for EntityAITempt#isTempting.  Applied by EnchTransformer.
	 * @param was The previous return of the method.
	 * @param s The itemstack being held by a player.
	 * @return If this stack is tempting, basically if it has the Tempting enchantment.
	 */
	public static boolean isTempting(boolean was, Object s) {
		ItemStack stack = (ItemStack) s;
		if (EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.TEMPTING, stack) > 0) return true;
		return was;
	}

	/**
	 * Hook needed for the Reflective enchantment to work properly.  Injected into EntityLivingBase#blockUsingShield.  Applied by EnchTransformer.
	 * @param a The entity holding the shield.
	 * @param b The attacking entity.
	 */
	public static void reflectiveHook(Object a, Object b) {
		EntityLivingBase user = (EntityLivingBase) a;
		EntityLivingBase attacker = (EntityLivingBase) b;
		int level;
		if ((level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.REFLECTIVE, user.getActiveItemStack())) > 0) {
			if (user.world.rand.nextInt(Math.max(0, 7 - level)) == 0) {
				DamageSource src = user instanceof EntityPlayer ? DamageSource.causePlayerDamage((EntityPlayer) user) : DamageSource.GENERIC;
				attacker.attackEntityFrom(src, level * 1.6F);
				user.getActiveItemStack().damageItem(10, user);
			}
		}
	}

	/**
	 * Hook that replaces calls to {@link Enchantment#getMaxLevel()} inside ContainerRepair.
	 * @param a An enchantment.
	 * @return The configured max level of that enchantment.
	 */
	public static int getMaxLevel(Object a) {
		return getEnchInfo((Enchantment) a).getMaxLevel();
	}

	public static EnchantmentInfo getEnchInfo(Enchantment ench) {
		EnchantmentInfo info = ENCHANTMENT_INFO.get(ench);
		if (config == null) {
			LOGGER.error("A mod has attempted to access enchantment information before Apotheosis init, this should not happen.");
			Thread.dumpStack();
			return new EnchantmentInfo(ench, ench.getMaxLevel(), ench.getMinLevel());
		}
		if (info == null) {
			int max = config.getInt("Max Level", ench.getRegistryName().toString(), ench.getMaxLevel(), 1, 127, "The max level of this enchantment.");
			int min = config.getInt("Min Level", ench.getRegistryName().toString(), ench.getMinLevel(), 1, 127, "The min level of this enchantment.");
			if (min > max) min = max;
			info = new EnchantmentInfo(ench, max, min);
			String maxF = config.getString("Max Power Function", ench.getRegistryName().toString(), "", "A function to determine the max enchanting power.  The variable \"x\" is level.  See: https://github.com/uklimaschewski/EvalEx#usage-examples");
			if (!maxF.isEmpty()) info.setMaxPower(new ExpressionPowerFunc(maxF));
			String minF = config.getString("Min Power Function", ench.getRegistryName().toString(), "", "A function to determine the min enchanting power.");
			if (!minF.isEmpty()) info.setMinPower(new ExpressionPowerFunc(minF));
			ENCHANTMENT_INFO.put(ench, info);
			if (config.hasChanged()) config.save();
			LOGGER.error("Had to late load enchantment info for {}, this is a bug in the mod {} as they are registering late!", ench.getRegistryName(), ench.getRegistryName().getNamespace());
		}
		return info;
	}

}
