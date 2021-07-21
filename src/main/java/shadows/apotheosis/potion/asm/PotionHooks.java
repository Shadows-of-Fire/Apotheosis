package shadows.apotheosis.potion.asm;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import shadows.apotheosis.ApotheosisObjects;

/**
 * ASM methods for the potion module.
 * @author Shadows
 *
 */
public class PotionHooks {

	/**
	 * Allows for true infinity to make other arrows infinite.
	 * Called from {@link ArrowItem#isInfinite(ItemStack, ItemStack, PlayerEntity)}
	 * Injected by javascript/true-infinity.js
	 */
	public static boolean isInfinite(ItemStack stack, ItemStack bow, PlayerEntity player) {
		int enchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow);
		if (enchant <= 0 ? false : stack.getItem().getClass() == ArrowItem.class) return true;
		return EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.TRUE_INFINITY, bow) > 0 && stack.getItem() instanceof ArrowItem;
	}

	/**
	 * Calculates damage taken based on potions.  Required for sundering.
	 * Called from {@link LivingEntity#applyPotionDamageCalculations(DamageSource, float)}
	 * Injected by javascript/sundering.js
	 */
	public static float applyPotionDamageCalculations(LivingEntity entity, DamageSource source, float damage) {
		if (source.isBypassMagic()) {
			return damage;
		} else {
			float mult = 1;
			if (entity.hasEffect(Effects.DAMAGE_RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
				int level = entity.getEffect(Effects.DAMAGE_RESISTANCE).getAmplifier() + 1;
				mult -= 0.2 * level;
			}
			if (ApotheosisObjects.SUNDERING != null && entity.hasEffect(ApotheosisObjects.SUNDERING) && source != DamageSource.OUT_OF_WORLD) {
				int level = entity.getEffect(ApotheosisObjects.SUNDERING).getAmplifier() + 1;
				mult += 0.2 * level;
			}

			damage *= mult;

			if (damage <= 0.0F) {
				return 0.0F;
			} else {
				int k = EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source);

				if (k > 0) {
					damage = CombatRules.getDamageAfterMagicAbsorb(damage, k);
				}

				return damage;
			}
		}
	}

}