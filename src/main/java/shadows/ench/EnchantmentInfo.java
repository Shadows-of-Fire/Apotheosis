package shadows.ench;

import net.minecraft.enchantment.Enchantment;

public class EnchantmentInfo {

	final Enchantment ench;
	int maxLevel;
	int minLevel;
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

	public static interface PowerFunc {
		int getPower(int level);
	}

}
