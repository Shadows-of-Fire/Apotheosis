package dev.shadowsoffire.apotheosis.util;

import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;

public class ApothMiscUtil {

    /**
     * Gets the experience cost when enchanting at a particular slot. This computes the true xp cost as if you had exactly as many levels as the level cost.
     * <p>
     * For a slot S and level L, the costs are the following:<br>
     * S == 0 -> cost = XP(L)<br>
     * S == 1 -> cost = XP(L) + XP(L-1)<br>
     * S == 2 -> cost = XP(L) + XP(L-1) + XP(L-2)
     * <p>
     * And so on and so forth, if there were ever to be more than three slots.
     *
     * @param level The level of the slot
     * @param slot  The slot index
     * @return The cost, in experience points, of buying the enchantment in a particular slot.
     */
    public static int getExpCostForSlot(int level, int slot) {
        int cost = 0;
        for (int i = 0; i <= slot; i++) {
            cost += EnchantmentUtils.getExperienceForLevel(level - i);
        }
        return cost - 1; // Eating exactly the amount will put you one point below the level, so offset by one here.
    }

    /**
     * Since {@link GradientColor} goes 1:1 through the entire array, if we have a unidirectional gradient, we need to make it wrap around.
     * <p>
     * This is done by making a reversed copy and concatenating them together.
     *
     * @param data The original unidirectional gradient data.
     * @return A cyclical gradient.
     */
    public static int[] doubleUpGradient(int[] data) {
        int[] out = new int[data.length * 2];
        System.arraycopy(data, 0, out, 0, data.length);
        for (int i = data.length - 1; i >= 0; i--) {
            out[data.length * 2 - 1 - i] = data[i];
        }
        return out;
    }

}
