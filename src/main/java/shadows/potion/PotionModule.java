package shadows.potion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisRecipeEvent;
import shadows.ApotheosisObjects;
import shadows.potion.potions.PotionSundering;

public class PotionModule {

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Potion");
	public static final ResourceLocation POTION_TEX = new ResourceLocation(Apotheosis.MODID, "textures/potions.png");

	static Configuration config;

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		PotionHelper.addMix(PotionTypes.AWKWARD, Items.SHULKER_SHELL, ApotheosisObjects.RESISTANCE);
		PotionHelper.addMix(ApotheosisObjects.RESISTANCE, Items.REDSTONE, ApotheosisObjects.LONG_RESISTANCE);
		PotionHelper.addMix(ApotheosisObjects.RESISTANCE, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_RESISTANCE);

		PotionHelper.addMix(ApotheosisObjects.RESISTANCE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.T_SUNDERING);
		PotionHelper.addMix(ApotheosisObjects.LONG_RESISTANCE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.LONG_SUNDERING);
		PotionHelper.addMix(ApotheosisObjects.STRONG_RESISTANCE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.STRONG_SUNDERING);
		PotionHelper.addMix(ApotheosisObjects.T_SUNDERING, Items.REDSTONE, ApotheosisObjects.LONG_SUNDERING);
		PotionHelper.addMix(ApotheosisObjects.T_SUNDERING, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_SUNDERING);

		PotionHelper.addMix(PotionTypes.AWKWARD, Items.GOLDEN_APPLE, ApotheosisObjects.ABSORPTION);
		PotionHelper.addMix(ApotheosisObjects.ABSORPTION, Items.REDSTONE, ApotheosisObjects.LONG_ABSORPTION);
		PotionHelper.addMix(ApotheosisObjects.ABSORPTION, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_ABSORPTION);

		PotionHelper.addMix(PotionTypes.AWKWARD, Items.MUSHROOM_STEW, ApotheosisObjects.HASTE);
		PotionHelper.addMix(ApotheosisObjects.HASTE, Items.REDSTONE, ApotheosisObjects.LONG_HASTE);
		PotionHelper.addMix(ApotheosisObjects.HASTE, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_HASTE);

		PotionHelper.addMix(ApotheosisObjects.HASTE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.FATIGUE);
		PotionHelper.addMix(ApotheosisObjects.LONG_HASTE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.LONG_FATIGUE);
		PotionHelper.addMix(ApotheosisObjects.STRONG_HASTE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.STRONG_FATIGUE);
		PotionHelper.addMix(ApotheosisObjects.FATIGUE, Items.REDSTONE, ApotheosisObjects.LONG_FATIGUE);
		PotionHelper.addMix(ApotheosisObjects.FATIGUE, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_FATIGUE);

		if (ApotheosisObjects.SKULL_FRAGMENT != null) PotionHelper.addMix(PotionTypes.AWKWARD, ApotheosisObjects.SKULL_FRAGMENT, ApotheosisObjects.WITHER);
		else PotionHelper.addMix(PotionTypes.AWKWARD, Ingredient.fromStacks(new ItemStack(Items.SKULL, 1, 1)), ApotheosisObjects.WITHER);
		PotionHelper.addMix(ApotheosisObjects.WITHER, Items.REDSTONE, ApotheosisObjects.LONG_WITHER);
		PotionHelper.addMix(ApotheosisObjects.WITHER, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_WITHER);
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new EnchantmentTrueInfinity().setRegistryName(Apotheosis.MODID, "true_infinity"));
	}

	@SubscribeEvent
	public void recipes(ApotheosisRecipeEvent e) {
		Ingredient fireRes = Apotheosis.potionIngredient(PotionTypes.FIRE_RESISTANCE);
		Ingredient abs = Apotheosis.potionIngredient(ApotheosisObjects.STRONG_ABSORPTION);
		Ingredient res = Apotheosis.potionIngredient(ApotheosisObjects.RESISTANCE);
		Ingredient regen = Apotheosis.potionIngredient(PotionTypes.STRONG_REGENERATION);
		e.helper.addShaped(new ItemStack(Items.GOLDEN_APPLE, 1, 1), 3, 3, fireRes, regen, fireRes, abs, Items.GOLDEN_APPLE, abs, res, abs, res);
	}

	@SubscribeEvent
	public void types(Register<PotionType> e) {
		Potion sundering = ForgeRegistries.POTIONS.getValue(new ResourceLocation(Apotheosis.MODID, "sundering"));
		//Formatter::off
		e.getRegistry().registerAll(
				new PotionType("resistance", new PotionEffect(MobEffects.RESISTANCE, 3600)).setRegistryName(Apotheosis.MODID, "resistance"),
				new PotionType("resistance", new PotionEffect(MobEffects.RESISTANCE, 9600)).setRegistryName(Apotheosis.MODID, "long_resistance"),
				new PotionType("resistance", new PotionEffect(MobEffects.RESISTANCE, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_resistance"),
				new PotionType("absorption", new PotionEffect(MobEffects.ABSORPTION, 1200, 1)).setRegistryName(Apotheosis.MODID, "absorption"),
				new PotionType("absorption", new PotionEffect(MobEffects.ABSORPTION, 3600, 1)).setRegistryName(Apotheosis.MODID, "long_absorption"),
				new PotionType("absorption", new PotionEffect(MobEffects.ABSORPTION, 600, 3)).setRegistryName(Apotheosis.MODID, "strong_absorption"),
				new PotionType("haste", new PotionEffect(MobEffects.HASTE, 3600)).setRegistryName(Apotheosis.MODID, "haste"),
				new PotionType("haste", new PotionEffect(MobEffects.HASTE, 9600)).setRegistryName(Apotheosis.MODID, "long_haste"),
				new PotionType("haste", new PotionEffect(MobEffects.HASTE, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_haste"),
				new PotionType("fatigue", new PotionEffect(MobEffects.MINING_FATIGUE, 3600)).setRegistryName(Apotheosis.MODID, "fatigue"),
				new PotionType("fatigue", new PotionEffect(MobEffects.MINING_FATIGUE, 9600)).setRegistryName(Apotheosis.MODID, "long_fatigue"),
				new PotionType("fatigue", new PotionEffect(MobEffects.MINING_FATIGUE, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_fatigue"),
				new PotionType("wither", new PotionEffect(MobEffects.WITHER, 3600)).setRegistryName(Apotheosis.MODID, "wither"),
				new PotionType("wither", new PotionEffect(MobEffects.WITHER, 9600)).setRegistryName(Apotheosis.MODID, "long_wither"),
				new PotionType("wither", new PotionEffect(MobEffects.WITHER, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_wither"),
				new PotionType("sundering", new PotionEffect(sundering, 3600)).setRegistryName(Apotheosis.MODID, "sundering"),
				new PotionType("sundering", new PotionEffect(sundering, 9600)).setRegistryName(Apotheosis.MODID, "long_sundering"),
				new PotionType("sundering", new PotionEffect(sundering, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_sundering"));
		//Formatter::on
	}

	@SubscribeEvent
	public void potions(Register<Potion> e) {
		e.getRegistry().register(new PotionSundering().setRegistryName(Apotheosis.MODID, "sundering"));
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
		int enchant = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, bow);
		if (enchant <= 0 ? false : stack.getItem().getClass() == ItemArrow.class) return true;
		return (EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.TRUE_INFINITY, bow) > 0 && stack.getItem() instanceof ItemArrow);
	}

	public static boolean doesShowParticles(Object e) {
		PotionEffect ef = (PotionEffect) e;
		if (ef.getPotion() == MobEffects.INVISIBILITY) return false;
		return ef.showParticles;
	}

	public static float applyPotionDamageCalculations(Object ent, Object src, float damage) {
		EntityLivingBase entity = (EntityLivingBase) ent;
		DamageSource source = (DamageSource) src;
		if (source.isDamageAbsolute()) {
			return damage;
		} else {
			float mult = 1;
			if (entity.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
				int level = (entity.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1);
				mult -= (0.2 * level);
			}
			if (ApotheosisObjects.SUNDERING != null && entity.isPotionActive(ApotheosisObjects.SUNDERING) && source != DamageSource.OUT_OF_WORLD) {
				int level = (entity.getActivePotionEffect(ApotheosisObjects.SUNDERING).getAmplifier() + 1);
				mult += (0.2 * level);
			}

			damage *= mult;

			if (damage <= 0.0F) {
				return 0.0F;
			} else {
				int k = EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), source);

				if (k > 0) {
					damage = CombatRules.getDamageAfterMagicAbsorb(damage, k);
				}

				return damage;
			}
		}
	}

}
