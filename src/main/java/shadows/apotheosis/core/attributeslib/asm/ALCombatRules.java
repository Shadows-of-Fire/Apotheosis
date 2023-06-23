package shadows.apotheosis.core.attributeslib.asm;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import shadows.apotheosis.core.attributeslib.api.ALAttributes;

/**
 * Contains AL-specific combat calculations for armor and protection values.
 */
public class ALCombatRules {

	/**
	 * Gets the amount of damage the user would take after applying protection points and protection bypass.<br>
	 * Protection bypass is based on {@linkplain ALAttributes#PROT_PIERCE Protection Pierce} and {@linkplain ALAttributes#PROT_SHRED Protection Shred}.
	 * <p>
	 * This is not invoked if the user does not have any protection points, and excess protection bypass has no effect.
	 * @param target The attack's target.
	 * @param src The DamageSource of the attack.
	 * @param amount The amount of damage the attack is currently dealing, after armor has been applied.
	 * @param protPoints The amount of protection points the target has against the incoming damage source.
	 * @return The modified damage value, after applying protection points, accounting for the attacker's bypass.
	 */
	public static float getDamageAfterProtection(LivingEntity target, DamageSource src, float amount, float protPoints) {
		if (src.getEntity() instanceof LivingEntity attacker) {
			float shred = (float) attacker.getAttributeValue(ALAttributes.PROT_SHRED.get());
			if (shred > 0.001F) {
				protPoints *= (1 - shred);
			}
			float pierce = (float) attacker.getAttributeValue(ALAttributes.PROT_PIERCE.get());
			if (pierce > 0.001F) {
				protPoints -= pierce;
			}
		}

		if (protPoints <= 0) return amount;
		return amount * getProtDamageReduction(protPoints);
	}

	/**
	 * Computes the damage reduction factor for the given amount of protection points.<br>
	 * Each protection point reduces damage by 2.5%, up to 80%.
	 * <p>
	 * In vanilla, each protection point reduces damage by 4%, up to 80%.
	 * 
	 * @see #getDamageAfterProtection(LivingEntity, DamageSource, float, float)
	 */
	public static float getProtDamageReduction(float protPoints) {
		return 1 - Math.min(0.025F * protPoints, 0.85F);
	}

	/**
	 * Gets the amount of damage the user would take after applying armor, toughness, and armor bypass.<br>
	 * Armor bypass is based on {@linkplain ALAttributes#PROT_PIERCE Protection Pierce} and {@linkplain ALAttributes#PROT_SHRED Protection Shred}.
	 * <p>
	 * Unlike protection bypass, additional armor bypass will cause unarmored targets to take additional damage.<br>
	 * Not invoked if the incoming damage source {@linkplain DamageSource#isBypassArmor() bypasses armor}.
	 * <p>
	 * With the introduction of this attribute, armor toughness acts as a shield against armor bypass.<br>
	 * Each point of armor toughness reduces the effectiveness of all armor bypass by 1%, up to 50%.<br>
	 * That said, armor toughness no longer reduces damage, and only reduces armor bypass.
	 * <p>
	 * @param target The attack's target.
	 * @param src The DamageSource of the attack.	
	 * @param amount The amount of damage the attack is currently dealing, after armor has been applied.
	 * @param armor The amount of armor points the target has.
	 * @param toughness The amount of armor toughness points the target has.
	 * @return The modified damage value, after applying armor, accounting for the attacker's bypass.
	 */
	public static float getDamageAfterArmor(LivingEntity target, DamageSource src, float amount, float armor, float toughness) {
		if (src.getEntity() instanceof LivingEntity attacker) {
			float shred = (float) attacker.getAttributeValue(ALAttributes.ARMOR_SHRED.get());
			float bypassResist = Math.min(toughness / 100, 0.5F);
			if (shred > 0.001F) {
				shred *= (1 - bypassResist);
				armor *= (1 - shred);
			}
			float pierce = (float) attacker.getAttributeValue(ALAttributes.ARMOR_PIERCE.get());
			if (pierce > 0.001F) {
				pierce *= (1 - bypassResist);
				armor -= pierce;
			}
		}

		if (armor <= 0) return amount;
		return amount * getArmorDamageReduction(armor);
	}

	/**
	 * Computes the damage reduction factor of the given armor level.<br>
	 * Armor reduces a percentage of incoming damage equal to <code>50 / (50 + armor)</code>.
	 * <p>
	 * Armor Toughness no longer impacts this calculation.
	 * <p>
	 * The vanilla calculation is <code>DR = clamp(armor - damage / (2 + toughness / 4), armor / 5, 20) / 25</code>
	 * <p>
	 * For comparisons, see https://i.imgur.com/3yEnTyi.png
	 * @see #getDamageAfterArmor(LivingEntity, DamageSource, float, float, float)
	 */
	public static float getArmorDamageReduction(float armor) {
		return 50 / (50 + armor);
	}
}
