package shadows.apotheosis.ench.table;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.ench.asm.EnchHooks;

public class RealEnchantmentHelper {

	/**
	* Returns the enchantability of itemstack, using a separate calculation for each enchantNum (0, 1 or 2).
	*/
	public static int calcItemStackEnchantability(Random rand, int num, int power, ItemStack stack) {
		int i = stack.getItemEnchantability();
		if (i <= 0) {
			return 0;
		} else {
			int j = rand.nextInt(8) + 1 + (power >> 1) + rand.nextInt(power + 1);
			if (num == 0) {
				return Math.max(j / 3, 1);
			} else {
				return num == 1 ? j * 2 / 3 + 1 : Math.max(j, power * 2);
			}
		}
	}

	/**
	* Create a list of random EnchantmentData (enchantments) that can be added together to the ItemStack, the 3rd
	* parameter is the table level.
	*/
	public static List<EnchantmentData> buildEnchantmentList(Random rand, ItemStack stack, int level, boolean treasure) {
		List<EnchantmentData> list = Lists.newArrayList();
		int enchantability = stack.getItemEnchantability();
		if (enchantability <= 0) {
			return list;
		} else {
			level = level + 1 + rand.nextInt(enchantability / 4 + 1) + rand.nextInt(enchantability / 4 + 1);
			float f = (rand.nextFloat() + rand.nextFloat() - 1.0F) * 0.15F;
			level = MathHelper.clamp(Math.round(level + level * f), 1, Integer.MAX_VALUE);
			List<EnchantmentData> list1 = getEnchantmentDatas(level, stack, treasure);
			if (!list1.isEmpty()) {
				list.add(WeightedRandom.getRandomItem(rand, list1));

				while (rand.nextInt(50) <= level) {
					removeIncompatible(list1, Util.getLast(list));
					if (list1.isEmpty()) {
						break;
					}

					list.add(WeightedRandom.getRandomItem(rand, list1));
					level /= 2;
				}
			}

			return list;
		}
	}

	/**
	 * Removes all enchantments from the list that are incompatible with the passed enchantment.
	 */
	public static void removeIncompatible(List<EnchantmentData> list, EnchantmentData data) {
		Iterator<EnchantmentData> iterator = list.iterator();

		while (iterator.hasNext()) {
			if (!data.enchantment.isCompatibleWith(iterator.next().enchantment)) {
				iterator.remove();
			}
		}

	}

	/**
	 * Gets all enchantments that can be applied to the given item at the respective power level.
	 */
	public static List<EnchantmentData> getEnchantmentDatas(int power, ItemStack stack, boolean treasure) {
		return EnchHooks.getEnchantmentDatas(power, stack, treasure);
	}
}