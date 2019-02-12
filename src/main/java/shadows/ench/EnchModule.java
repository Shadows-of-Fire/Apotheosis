package shadows.ench;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisRecipeEvent;
import shadows.ench.EnchantmentInfo.ExpressionPowerFunc;
import shadows.ench.enchantments.EnchantmentDepths;
import shadows.ench.enchantments.EnchantmentHellInfused;
import shadows.ench.enchantments.EnchantmentIcyThorns;
import shadows.ench.enchantments.EnchantmentLifeMend;
import shadows.ench.enchantments.EnchantmentMounted;
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

	@ObjectHolder("apotheosis:hellshelf")
	public static final BlockHellBookshelf HELLSHELF = null;

	@ObjectHolder("minecraft:web")
	public static final Item COBWEB = null;

	@ObjectHolder("apotheosis:prismatic_web")
	public static final Item PRISMATIC_COBWEB = null;

	@ObjectHolder("apotheosis:hell_infusion")
	public static final EnchantmentHellInfused HELL_INFUSION = null;

	@ObjectHolder("apotheosis:mounted_strike")
	public static final EnchantmentMounted MOUNTED_STRIKE = null;

	@ObjectHolder("apotheosis:depth_miner")
	public static final EnchantmentDepths DEPTH_MINER = null;

	@ObjectHolder("apotheosis:stable_footing")
	public static final EnchantmentStableFooting STABLE_FOOTING = null;

	@ObjectHolder("apotheosis:scavenger")
	public static final EnchantmentScavenger SCAVENGER = null;

	@ObjectHolder("apotheosis:life_mending")
	public static final EnchantmentLifeMend LIFE_MENDING = null;

	@ObjectHolder("apotheosis:icy_thorns")
	public static final EnchantmentIcyThorns ICY_THORNS = null;

	@ObjectHolder("apotheosis:tempting")
	public static final EnchantmentTempting TEMPTING = null;

	@ObjectHolder("apotheosis:shield_bash")
	public static final EnchantmentShieldBash SHIELD_BASH = null;

	public static float localAtkStrength = 1;

	public static final DamageSource CORRUPTED = new DamageSource("corrupted") {
		@Override
		public ITextComponent getDeathMessage(EntityLivingBase entity) {
			return new TextComponentTranslation("death.apotheosis.corrupted", entity.getDisplayName());
		};
	}.setDamageBypassesArmor().setDamageIsAbsolute();

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
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		e.getRegistry().register(new BlockHellBookshelf(new ResourceLocation(Apotheosis.MODID, "hellshelf")));
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new ItemShearsExt().setRegistryName(Items.SHEARS.getRegistryName()).setTranslationKey("shears"),
				new ItemHellBookshelf(HELLSHELF).setRegistryName(HELLSHELF.getRegistryName()),
				new Item().setRegistryName(Apotheosis.MODID, "prismatic_web").setTranslationKey(Apotheosis.MODID + ".prismatic_web"));
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
				new EnchantmentShieldBash().setRegistryName(Apotheosis.MODID, "shield_bash"));
		//Formatter::on
	}

	@SubscribeEvent
	public void models(ModelRegistryEvent e) {
		PlaceboUtil.sMRL(HELLSHELF, 0, "normal");
	}

	@SubscribeEvent
	public void recipes(ApotheosisRecipeEvent e) {
		Ingredient pot = new NBTIngredient(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.REGENERATION));
		e.helper.addShaped(HELLSHELF, 3, 3, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Items.BLAZE_ROD, Blocks.BOOKSHELF, pot, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK);
		e.helper.addShaped(PRISMATIC_COBWEB, 3, 3, null, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, Blocks.WEB, Items.PRISMARINE_SHARD, null, Items.PRISMARINE_SHARD, null);
	}

	@SubscribeEvent
	public void removeEnch(AnvilUpdateEvent e) {
		if (!EnchantmentHelper.getEnchantments(e.getLeft()).isEmpty()) {
			if (e.getRight().getItem() == COBWEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> ent.getKey().isCurse()).collect(Collectors.toMap(ent -> ent.getKey(), ent -> ent.getValue())), stack);
				e.setCost(1);
				e.setMaterialCost(1);
				e.setOutput(stack);
			} else if (e.getRight().getItem() == PRISMATIC_COBWEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> !ent.getKey().isCurse()).collect(Collectors.toMap(ent -> ent.getKey(), ent -> ent.getValue())), stack);
				e.setCost(30);
				e.setMaterialCost(1);
				e.setOutput(stack);
			}
		}
	}

	@SubscribeEvent
	public void trackCooldown(AttackEntityEvent e) {
		EntityPlayer p = e.getEntityPlayer();
		localAtkStrength = p.getCooledAttackStrength(0.5F);
	}

	Method dropLoot;

	@SubscribeEvent
	public void scavenger(LivingDropsEvent e) throws Exception {
		Entity attacker = e.getSource().getTrueSource();
		if (attacker instanceof EntityPlayer) {
			EntityPlayer p = (EntityPlayer) attacker;
			int scavenger = EnchantmentHelper.getEnchantmentLevel(SCAVENGER, p.getHeldItemMainhand());
			if (scavenger > 0 && p.world.rand.nextInt(100) < scavenger * 2.5F) {
				if (dropLoot == null) {
					dropLoot = EntityLivingBase.class.getDeclaredMethod("dropLoot", boolean.class, int.class, DamageSource.class);
					dropLoot.setAccessible(true);
				}
				dropLoot.invoke(e.getEntityLiving(), true, e.getLootingLevel(), e.getSource());
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
				int level = EnchantmentHelper.getEnchantmentLevel(LIFE_MENDING, stack);
				if (level > 0 && e.getEntityLiving().world.rand.nextInt(10) == 0) {
					int i = Math.min(level, stack.getItemDamage());
					e.getEntityLiving().attackEntityFrom(CORRUPTED, i * 0.2F);
					stack.setItemDamage(stack.getItemDamage() - i);
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public void breakSpeed(PlayerEvent.BreakSpeed e) {
		EntityPlayer p = e.getEntityPlayer();
		ItemStack stack = p.getHeldItemMainhand();
		if (stack.isEmpty()) return;
		int depth = EnchantmentHelper.getEnchantmentLevel(DEPTH_MINER, stack);
		if (depth > 0) {
			float effectiveness = (p.world.getSeaLevel() - (float) p.posY) / p.world.getSeaLevel();
			if (effectiveness < 0) effectiveness /= 3;
			float speedChange = 1 + depth * depth * effectiveness;
			e.setNewSpeed(e.getNewSpeed() + speedChange);
		}
		if (!p.onGround && EnchantmentHelper.getEnchantmentLevel(STABLE_FOOTING, stack) > 0) {
			e.setNewSpeed(e.getNewSpeed() * 5F);
		}
	}

	public static void setEnch(ToolMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ToolMaterial.class, mat, ench, "enchantability", "field_78008_j");
	}

	public static void setEnch(ArmorMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ArmorMaterial.class, mat, ench, "enchantability", "field_78055_h");
	}

	public static List<EnchantmentData> getEnchantmentDatas(int power, Object s, boolean allowTreasure) {
		ItemStack stack = (ItemStack) s;
		List<EnchantmentData> list = new ArrayList<>();
		boolean isBook = stack.getItem() == Items.BOOK;
		for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
			if (enchantment.isTreasureEnchantment() && !allowTreasure) continue;
			if ((enchantment.canApplyAtEnchantingTable(stack) || (isBook && enchantment.isAllowedOnBooks()))) {
				EnchantmentInfo info = ENCHANTMENT_INFO.get(enchantment);
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

	public static boolean isTempting(boolean was, Object s) {
		ItemStack stack = (ItemStack) s;
		if (EnchantmentHelper.getEnchantmentLevel(TEMPTING, stack) > 0) return true;
		return was;
	}

}
