package shadows.apotheosis.ench;

import java.math.BigDecimal;

import com.udojava.evalex.Expression;

import net.minecraft.enchantment.Enchantment;

public class EnchantmentInfo {

	final Enchantment ench;
	final int maxLevel;
	final int minLevel;
	PowerFunc maxPower;
	PowerFunc minPower;

	public EnchantmentInfo(Enchantment ench, int maxLevel, int minLevel) {
		this.ench = ench;
		this.maxLevel = maxLevel;
		this.minLevel = minLevel;
		this.maxPower = defaultMax(ench);
		this.minPower = defaultMin(ench);
	}

	public int getMaxLevel() {
		return Math.min(EnchModule.ENCH_HARD_CAPS.getOrDefault(this.ench, Integer.MAX_VALUE), this.maxLevel);
	}

	public int getMinLevel() {
		return this.minLevel;
	}

	public int getMinPower(int level) {
		return this.minPower.getPower(level);
	}

	public int getMaxPower(int level) {
		return this.maxPower.getPower(level);
	}

	public void setMaxPower(PowerFunc maxPower) {
		this.maxPower = maxPower;
	}

	public void setMinPower(PowerFunc minPower) {
		this.minPower = minPower;
	}

	/**
	 * Simple int to int function, used for converting a level into a required enchanting power.
	 */
	public static interface PowerFunc {
		int getPower(int level);
	}

	public static class ExpressionPowerFunc implements PowerFunc {

		Expression ex;

		public ExpressionPowerFunc(String func) {
			this.ex = new Expression(func);
		}

		@Override
		public int getPower(int level) {
			return this.ex.setVariable("x", new BigDecimal(level)).eval().intValue();
		}

	}

	private static PowerFunc defaultMax(Enchantment ench) {
		return level -> 200;
	}

	private static PowerFunc defaultMin(Enchantment ench) {
		return level -> {
			if (level > 1) {
				int diff = ench.getMinEnchantability(ench.getMaxLevel()) - ench.getMinEnchantability(ench.getMaxLevel() - 1);
				return level > ench.getMaxLevel() ? ench.getMinEnchantability(level) + diff * (int) Math.pow(level - ench.getMaxLevel(), 1.6) : ench.getMinEnchantability(level);
			}
			return ench.getMinEnchantability(level);
		};
	}

}