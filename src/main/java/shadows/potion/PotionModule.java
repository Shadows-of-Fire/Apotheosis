package shadows.potion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisRecipeEvent;

public class PotionModule {

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Potion");

	@ObjectHolder("apotheosis:potion_infinity")
	public static final EnchantmentPotionInfinity POTION_INFINITY = null;

	@ObjectHolder("apotheosis:resistance")
	public static final PotionType RESISTANCE = null;

	@ObjectHolder("apotheosis:long_resistance")
	public static final PotionType LONG_RESISTANCE = null;

	@ObjectHolder("apotheosis:strong_resistance")
	public static final PotionType STRONG_RESISTANCE = null;

	@ObjectHolder("apotheosis:absorption")
	public static final PotionType ABSORPTION = null;

	@ObjectHolder("apotheosis:long_absorption")
	public static final PotionType LONG_ABSORPTION = null;

	@ObjectHolder("apotheosis:strong_absorption")
	public static final PotionType STRONG_ABSORPTION = null;

	static Configuration config;

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		PotionHelper.addMix(PotionTypes.AWKWARD, Items.SHULKER_SHELL, RESISTANCE);
		PotionHelper.addMix(RESISTANCE, Items.REDSTONE, LONG_RESISTANCE);
		PotionHelper.addMix(RESISTANCE, Items.GLOWSTONE_DUST, STRONG_RESISTANCE);

		PotionHelper.addMix(PotionTypes.AWKWARD, Items.GOLDEN_APPLE, ABSORPTION);
		PotionHelper.addMix(ABSORPTION, Items.REDSTONE, LONG_ABSORPTION);
		PotionHelper.addMix(ABSORPTION, Items.GLOWSTONE_DUST, STRONG_ABSORPTION);
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new EnchantmentPotionInfinity().setRegistryName(Apotheosis.MODID, "potion_infinity"));
	}

	@SubscribeEvent
	public void recipes(ApotheosisRecipeEvent e) {
		Ingredient fireRes = Apotheosis.potionIngredient(PotionTypes.FIRE_RESISTANCE);
		Ingredient abs = Apotheosis.potionIngredient(STRONG_ABSORPTION);
		Ingredient res = Apotheosis.potionIngredient(RESISTANCE);
		Ingredient regen = Apotheosis.potionIngredient(PotionTypes.STRONG_REGENERATION);
		e.helper.addShaped(new ItemStack(Items.GOLDEN_APPLE, 1, 1), 3, 3, fireRes, regen, fireRes, abs, Items.GOLDEN_APPLE, abs, res, abs, res);
	}

	@SubscribeEvent
	public void types(Register<PotionType> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new PotionType("resistance", new PotionEffect(MobEffects.RESISTANCE, 3600)).setRegistryName(Apotheosis.MODID, "resistance"),
				new PotionType("resistance", new PotionEffect(MobEffects.RESISTANCE, 9600)).setRegistryName(Apotheosis.MODID, "long_resistance"),
				new PotionType("resistance", new PotionEffect(MobEffects.RESISTANCE, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_resistance"),
				new PotionType("absorption", new PotionEffect(MobEffects.ABSORPTION, 1200, 1)).setRegistryName(Apotheosis.MODID, "absorption"),
				new PotionType("absorption", new PotionEffect(MobEffects.ABSORPTION, 3600, 1)).setRegistryName(Apotheosis.MODID, "long_absorption"),
				new PotionType("absorption", new PotionEffect(MobEffects.ABSORPTION, 600, 3)).setRegistryName(Apotheosis.MODID, "strong_absorption"));
		//Formatter::on
	}

	/**
	 * Redirect for {@link ItemArrow#isInfinite(ItemStack, ItemStack, net.minecraft.entity.player.EntityPlayer)}
	 * @param a Arrow ItemStack
	 * @param b Bow ItemStack
	 * @param c EntityPlayer using the bow
	 * @return If this arrow should not be consumed.
	 */
	public static boolean isInfinite(Object a, Object b, Object c) {
		ItemStack stack = (ItemStack) a;
		ItemStack bow = (ItemStack) b;
		int enchant = net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.INFINITY, bow);
		if (enchant <= 0 ? false : stack.getItem().getClass() == ItemArrow.class) return true;
		return (POTION_INFINITY != null && EnchantmentHelper.getEnchantmentLevel(POTION_INFINITY, bow) > 0 && stack.getItem() instanceof ItemArrow);
	}

	public static boolean doesShowParticles(Object e) {
		PotionEffect ef = (PotionEffect) e;
		if (ef.getPotion() == MobEffects.INVISIBILITY) return false;
		return ef.showParticles;
	}

}
