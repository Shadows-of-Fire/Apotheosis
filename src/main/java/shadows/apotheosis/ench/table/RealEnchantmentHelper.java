package shadows.apotheosis.ench.table;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.ench.table.EnchantmentContainerExt.Arcana;

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
	public static List<EnchantmentData> buildEnchantmentList(Random rand, ItemStack stack, int power, float quanta, float arcanaLevel, boolean treasure) {
		List<EnchantmentData> chosenEnchants = Lists.newArrayList();
		int enchantability = stack.getItemEnchantability();
		if (enchantability <= 0) {
			return chosenEnchants;
		} else {
			power = power + rand.nextInt(Math.max(enchantability / 2, 1));
			float factor = MathHelper.nextFloat(rand, -1F, 1F) * quanta / 10;
			power = MathHelper.clamp(Math.round(power + power * factor), 1, Integer.MAX_VALUE);
			Arcana arcana = Arcana.getForThreshold(arcanaLevel);
			List<EnchantmentData> allEnchants = getEnchantmentDatas(power, stack, treasure);
			List<ArcanaEnchantmentData> possibleEnchants = allEnchants.stream().map(d -> new ArcanaEnchantmentData(arcana, d)).collect(Collectors.toList());
			if (!possibleEnchants.isEmpty()) {
				chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).data);

				if (arcanaLevel >= 2.5F && !possibleEnchants.isEmpty()) {
					removeIncompatible(possibleEnchants, Util.getLast(chosenEnchants));
					chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).data);
				}

				if (arcanaLevel >= 7.5F && !possibleEnchants.isEmpty()) {
					removeIncompatible(possibleEnchants, Util.getLast(chosenEnchants));
					chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).data);
				}

				while (arcanaLevel + rand.nextInt(50) <= power) {
					removeIncompatible(possibleEnchants, Util.getLast(chosenEnchants));
					if (possibleEnchants.isEmpty()) {
						break;
					}

					chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).data);
					power /= 2;
				}
			}

			return chosenEnchants;
		}
	}

	/**
	 * Removes all enchantments from the list that are incompatible with the passed enchantment.
	 */
	public static void removeIncompatible(List<ArcanaEnchantmentData> list, EnchantmentData data) {
		Iterator<ArcanaEnchantmentData> iterator = list.iterator();

		while (iterator.hasNext()) {
			if (!data.enchantment.isCompatibleWith(iterator.next().data.enchantment)) {
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

	private static class ArcanaEnchantmentData extends WeightedRandom.Item {
		EnchantmentData data;

		private ArcanaEnchantmentData(Arcana arcana, EnchantmentData data) {
			super(arcana.getRarities()[data.enchantment.getRarity().ordinal()]);
			this.data = data;
		}
	}
}