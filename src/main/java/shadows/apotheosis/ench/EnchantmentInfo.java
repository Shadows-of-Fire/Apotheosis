package shadows.apotheosis.ench;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.udojava.evalex.Expression;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;

public class EnchantmentInfo {

	/**
	 * Special cased vanilla defaults to enchantments that cause min power > max power at higher levels.
	 * These override the max power functions.
	 */
	static Map<Enchantment, PowerFunc> overrides = new HashMap<>();
	static {
		overrides.put(Enchantments.THORNS, l -> Enchantments.THORNS.getMinEnchantability(l) + 20);
		overrides.put(Enchantments.KNOCKBACK, l -> Enchantments.KNOCKBACK.getMinEnchantability(l) + 20);
		overrides.put(Enchantments.FIRE_ASPECT, l -> Enchantments.FIRE_ASPECT.getMinEnchantability(l) + 20);
		overrides.put(Enchantments.QUICK_CHARGE, l -> Enchantments.QUICK_CHARGE.getMinEnchantability(l) + 20);
	}

	final Enchantment ench;
	final int maxLevel;
	final int minLevel;
	PowerFunc maxPower;
	PowerFunc minPower;

	public EnchantmentInfo(Enchantment ench, int maxLevel, int minLevel) {
		this.ench = ench;
		this.maxLevel = maxLevel;
		this.minLevel = minLevel;
		maxPower = level -> ench.getMaxEnchantability(level);
		if (overrides.containsKey(ench)) maxPower = overrides.get(ench);
		minPower = level -> ench.getMinEnchantability(level);
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public int getMinPower(int level) {
		return minPower.getPower(level);
	}

	public int getMaxPower(int level) {
		return maxPower.getPower(level);
	}

	public void setMaxPower(PowerFunc maxPower) {
		this.maxPower = maxPower;
	}

	public void setMinPower(PowerFunc minPower) {
		this.minPower = minPower;
	}

	public static interface PowerFunc {
		int getPower(int level);
	}

	public static class ExpressionPowerFunc implements PowerFunc {

		Expression ex;

		public ExpressionPowerFunc(String func) {
			ex = new Expression(func);
		}

		@Override
		public int getPower(int level) {
			return ex.setVariable("x", new BigDecimal(level)).eval().intValue();
		}

	}

}
