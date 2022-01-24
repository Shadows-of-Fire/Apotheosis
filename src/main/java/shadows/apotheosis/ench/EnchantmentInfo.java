package shadows.apotheosis.ench;

import java.math.BigDecimal;

import net.minecraft.world.item.enchantment.Enchantment;
import s_com.udojava.evalex.Expression;
import shadows.apotheosis.ench.table.EnchantingStatManager;
import shadows.placebo.config.Configuration;

public class EnchantmentInfo {

	protected final Enchantment ench;
	protected final int maxLevel;
	protected final boolean treasure, discoverable, tradeable;
	protected final PowerFunc maxPower, minPower;

	public EnchantmentInfo(Enchantment ench, int maxLevel, PowerFunc max, PowerFunc min, boolean treasure, boolean discoverable, boolean tradeable) {
		this.ench = ench;
		this.maxLevel = maxLevel;
		this.maxPower = max;
		this.minPower = min;
		this.treasure = treasure;
		this.discoverable = discoverable;
		this.tradeable = tradeable;
	}

	@Deprecated
	public EnchantmentInfo(Enchantment ench) {
		this(ench, ench.getMaxLevel(), defaultMax(ench), defaultMin(ench), ench.isTreasureOnly(), ench.isDiscoverable(), ench.isTradeable());
	}

	public int getMaxLevel() {
		return Math.min(EnchModule.ENCH_HARD_CAPS.getOrDefault(this.ench, Integer.MAX_VALUE), this.maxLevel);
	}

	public int getMinPower(int level) {
		return this.minPower.getPower(level);
	}

	public int getMaxPower(int level) {
		return this.maxPower.getPower(level);
	}

	public boolean isDiscoverable() {
		return this.discoverable;
	}

	public boolean isTreasure() {
		return this.treasure;
	}

	public boolean isTradeable() {
		return this.tradeable;
	}

	public static EnchantmentInfo load(Enchantment ench, Configuration cfg) {
		String category = ench.getRegistryName().toString();
		int max = cfg.getInt("Max Level", category, EnchModule.getDefaultMax(ench), 1, 127, "The max level of this enchantment - originally " + ench.getMaxLevel() + ".");
		String maxF = cfg.getString("Max Power Function", category, "", "A function to determine the max enchanting power.  The variable \"x\" is level.  See: https://github.com/uklimaschewski/EvalEx#usage-examples");
		String minF = cfg.getString("Min Power Function", category, "", "A function to determine the min enchanting power.");
		PowerFunc maxPower = maxF.isEmpty() ? defaultMax(ench) : new ExpressionPowerFunc(maxF);
		PowerFunc minPower = minF.isEmpty() ? defaultMin(ench) : new ExpressionPowerFunc(minF);
		boolean treasure = cfg.getBoolean("Treasure", category, ench.isTreasureOnly(), "If this enchantment is available via treasure (loot) sources. Ignored if not discoverable.");
		boolean discoverable = cfg.getBoolean("Discoverable", category, ench.isDiscoverable(), "If this enchantment is obtainable via loot and enchanting.");
		boolean tradeable = cfg.getBoolean("Tradeable", category, ench.isDiscoverable(), "If this enchantment is obtainable via villager trades.");
		EnchantmentInfo info = new EnchantmentInfo(ench, max, maxPower, minPower, treasure, discoverable, tradeable);
		String rarity = cfg.getString("Rarity", ench.getRegistryName().toString(), ench.getRarity().name(), "The rarity of this enchantment.  Valid values are COMMON, UNCOMMON, RARE, and VERY_RARE.");
		try {
			Enchantment.Rarity r = Enchantment.Rarity.valueOf(rarity);
			ench.rarity = r;
		} catch (Exception ex) {
			EnchModule.LOGGER.error("Failed to parse rarity for {}, as {} is not a valid rarity string.", ench.getRegistryName(), rarity);
		}
		return info;
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
		return level -> (int) (EnchantingStatManager.getAbsoluteMaxEterna() * 4);
	}

	/**
	 * This is the default minimum power function.
	 * If the level is equal to or below the default max level, we return the original value {@link Enchantment#getMinCost(int)}
	 *
	 * If the level is above than the default max level, then we compute the following:
	 * Let diff be the slope of {@link Enchantment#getMinCost(int)}, or 15, if the slope would be zero.
	 * minPower = baseMinPower + diff * (level - baseMaxLevel) ^ 1.6
	 */
	public static PowerFunc defaultMin(Enchantment ench) {
		return level -> {
			if (level > ench.getMaxLevel() && level > 1) {
				int diff = ench.getMinCost(ench.getMaxLevel()) - ench.getMinCost(ench.getMaxLevel() - 1);
				if (diff == 0) diff = 15;
				return ench.getMinCost(level) + diff * (int) Math.pow(level - ench.getMaxLevel(), 1.6);
			}
			return ench.getMinCost(level);
		};
	}

}