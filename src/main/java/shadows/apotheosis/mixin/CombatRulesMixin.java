package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.damagesource.CombatRules;
import shadows.apotheosis.ench.asm.EnchHooks;

@Mixin(CombatRules.class)
public class CombatRulesMixin {

	/**
	 * @author Shadows
	 * @reason Update combat rules to account for higher protection levels.
	 * @param damage Original damage value
	 * @param enchantModifiers Total protection value against this damage
	 * @return The modified damage, accounting for protection value.
	 */
	@Overwrite
	public static float getDamageAfterMagicAbsorb(float damage, float enchantModifiers) {
		return EnchHooks.getDamageAfterMagicAbsorb(damage, enchantModifiers);
	}
}
