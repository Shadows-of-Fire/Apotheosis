package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import shadows.apotheosis.Apotheosis;

@Mixin(CombatRules.class)
public class CombatRulesMixin {

	/**
	 * Override for protection calculations.  Removes the hard cap of total level 20.  Effectiveness is reduced past 20.
	 * New max protection value is 65.
	 * 80% Reduction at 20, 95% at 65.
	 * 
	 * The first 20 points provide 4% reduction each, the last 45 provide 0.33% reduction each.
	 * 
	 * @author Shadows
	 * @reason Update combat rules to account for higher protection levels.
	 * @param damage The incoming damage, after armor has been applied.
	 * @param protPoints Total protection value against this damage type.
	 * @return The modified damage, accounting for protection value.
	 */
	@Overwrite
	public static float getDamageAfterMagicAbsorb(float damage, float protPoints) {
		float clamped = Mth.clamp(protPoints, 0, 20);
		float remaining = Apotheosis.enableEnch ? Mth.clamp(protPoints - 20, 0, 45) : 0;
		float factor = 1F - clamped / 25F;
		if (remaining > 0) {
			factor -= 0.2F * remaining / 60;
		}
		return damage * factor;
	}

	/**
	 * Override for armor calculations.
	 * 
	 * Vanilla computations are:
	 * DR = clamp(armor - damage / (2 + toughness / 4), armor / 5, 20) / 25
	 * 
	 * Apoth computations are:
	 * DR = clamp(1.25 * armor - damage / (2 + toughness / 4), armor * (.25 + toughness/200), 20) / 25
	 * 
	 * This tends to prevent slightly more damage than vanilla: https://i.imgur.com/uFUt9JP.png
	 * 
	 * @author Shadows
	 * @reason Update combat rules to account for higher armor levels.
	 * @param damage The incoming unmitigated damage.
	 * @param armor The total armor value (clamped at max attribute value).
	 * @param toughness The total toughness value (clamped at max attribute value).
	 * @return The modified damage, after being reduced by armor+toughness.
	 */
	@Overwrite
	public static float getDamageAfterAbsorb(float damage, float armor, float toughness) {
		float toughnessModif = 2F + toughness / 4F;
		float clamped = Mth.clamp(1.25F * armor - damage / toughnessModif, armor * (0.25F + 0.005F * toughness), 20F);
		float factor = 1F - clamped / 25F;
		return damage * factor;
	}
}
