package shadows.potion.asm;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import shadows.ApotheosisObjects;

/**
 * ASM methods for the potion module.
 * @author Shadows
 *
 */
public class PotionHooks {

	/**
	 * Allows for true infinity to make other arrows infinite.
	 * Called from {@link ItemArrow#isInfinite(ItemStack, ItemStack, EntityPlayer)}
	 * Injected by {@link PotionTransformer}
	 */
	public static boolean isInfinite(ItemStack stack, ItemStack bow, EntityPlayer player) {
		int enchant = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, bow);
		if (enchant <= 0 ? false : stack.getItem().getClass() == ItemArrow.class) return true;
		return EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.TRUE_INFINITY, bow) > 0 && stack.getItem() instanceof ItemArrow;
	}

	/**
	 * Disables particles for invisibility.
	 * Called from {@link PotionEffect#doesShowParticles()}
	 * Injected by {@link PotionTransformer}
	 */
	public static boolean doesShowParticles(PotionEffect ef) {
		if (ef.getPotion() == MobEffects.INVISIBILITY) return false;
		return ef.showParticles;
	}

	/**
	 * Calculates damage taken based on potions.  Required for sundering.
	 * Called from {@link EntityLivingBase#applyPotionDamageCalculations(DamageSource, float)}
	 * Injected by {@link PotionTransformer}
	 */
	public static float applyPotionDamageCalculations(EntityLivingBase entity, DamageSource source, float damage) {
		if (source.isDamageAbsolute()) {
			return damage;
		} else {
			float mult = 1;
			if (entity.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
				int level = entity.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1;
				mult -= 0.2 * level;
			}
			if (ApotheosisObjects.SUNDERING != null && entity.isPotionActive(ApotheosisObjects.SUNDERING) && source != DamageSource.OUT_OF_WORLD) {
				int level = entity.getActivePotionEffect(ApotheosisObjects.SUNDERING).getAmplifier() + 1;
				mult += 0.2 * level;
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
