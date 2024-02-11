package dev.shadowsoffire.apotheosis.ench;

import java.math.BigDecimal;

import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import s_com.udojava.evalex.Expression;

public class EnchantmentInfo {

    protected final Enchantment ench;
    protected final int maxLevel, maxLootLevel;
    protected final boolean treasure, discoverable, lootable, tradeable;
    protected final PowerFunc maxPower, minPower;

    public EnchantmentInfo(Enchantment ench, int maxLevel, int maxLootLevel, PowerFunc max, PowerFunc min, boolean treasure, boolean discoverable, boolean lootable, boolean tradeable) {
        this.ench = ench;
        this.maxLevel = maxLevel;
        this.maxLootLevel = maxLootLevel;
        this.maxPower = max;
        this.minPower = min;
        this.treasure = treasure;
        this.discoverable = discoverable;
        this.lootable = lootable;
        this.tradeable = tradeable;
    }

    @Deprecated
    public EnchantmentInfo(Enchantment ench) {
        this(ench, ench.getMaxLevel(), ench.getMaxLevel(), defaultMax(ench), defaultMin(ench), ench.isTreasureOnly(), ench.isDiscoverable(), ench.isDiscoverable(), ench.isTradeable());
    }

    public int getMaxLevel() {
        return Math.min(EnchModule.ENCH_HARD_CAPS.getOrDefault(this.ench, 127), this.maxLevel);
    }

    public int getMaxLootLevel() {
        return Math.min(EnchModule.ENCH_HARD_CAPS.getOrDefault(this.ench, 127), this.maxLootLevel);
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

    public boolean isLootable() {
        return this.lootable;
    }

    public boolean isTradeable() {
        return this.tradeable;
    }

    public static EnchantmentInfo load(Enchantment ench, Configuration cfg) {
        String category = ForgeRegistries.ENCHANTMENTS.getKey(ench).toString();
        int max = cfg.getInt("Max Level", category, EnchModule.getDefaultMax(ench), 1, 127, "The max level of this enchantment - originally " + ench.getMaxLevel() + ".");
        int maxLoot = cfg.getInt("Max Loot Level", category, ench.getMaxLevel(), 1, 127, "The max level of this enchantment available from loot sources.");
        String maxF = cfg.getString("Max Power Function", category, "", "A function to determine the max enchanting power.  The variable \"x\" is level.  See: https://github.com/uklimaschewski/EvalEx#usage-examples");
        String minF = cfg.getString("Min Power Function", category, "", "A function to determine the min enchanting power.");
        PowerFunc maxPower = maxF.isEmpty() ? defaultMax(ench) : new ExpressionPowerFunc(maxF);
        PowerFunc minPower = minF.isEmpty() ? defaultMin(ench) : new ExpressionPowerFunc(minF);
        boolean treasure = cfg.getBoolean("Treasure", category, ench.isTreasureOnly(), "If this enchantment is only available by loot sources.");
        boolean discoverable = cfg.getBoolean("Discoverable", category, ench.isDiscoverable(), "If this enchantment is obtainable via enchanting and enchanted loot items.");
        boolean lootable = cfg.getBoolean("Lootable", category, ench.isDiscoverable(), "If enchanted books of this enchantment are available via loot sources.");
        boolean tradeable = cfg.getBoolean("Tradeable", category, ench.isTradeable(), "If enchanted books of this enchantment are available via villager trades.");
        EnchantmentInfo info = new EnchantmentInfo(ench, max, maxLoot, maxPower, minPower, treasure, discoverable, lootable, tradeable);
        String rarity = cfg.getString("Rarity", category, ench.getRarity().name(), "The rarity of this enchantment.  Valid values are COMMON, UNCOMMON, RARE, and VERY_RARE.");
        try {
            Enchantment.Rarity r = Enchantment.Rarity.valueOf(rarity);
            ench.rarity = r;
        }
        catch (Exception ex) {
            EnchModule.LOGGER.error("Failed to parse rarity for {}, as {} is not a valid rarity string.", category, rarity);
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

    public static PowerFunc defaultMax(Enchantment ench) {
        return level -> (int) (EnchantingStatRegistry.getAbsoluteMaxEterna() * 4);
    }

    /**
     * This is the default minimum power function.
     * If the level is equal to or below the default max level, we return the original value {@link Enchantment#getMinCost(int)}
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
