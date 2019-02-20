package shadows.ench;

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
		maxPower = level -> ench.getMaxEnchantability(level);
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
