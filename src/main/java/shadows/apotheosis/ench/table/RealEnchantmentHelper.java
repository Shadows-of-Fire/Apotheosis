package shadows.apotheosis.ench.table;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.ench.table.ApothEnchantContainer.Arcana;

public class RealEnchantmentHelper {

	/**
	 * Determines the level of the given enchantment table slot.
	 * @param rand Pre-seeded random.
	 * @param num Enchantment Slot Number [0-2]
	 * @param power Enchantment Power (Eterna Level)
	 * @param stack Itemstack to be enchanted.
	 * @return The level that the table will use for this specific slot.
	 */
	public static int calcSlotLevel(Random rand, int num, float power, ItemStack stack) {
		int ench = stack.getItemEnchantability();
		if (ench <= 0) return 0;
		int level = (int) (power * 2);
		if (num == 2) return level;
		float lowBound = Math.min(0.85F, 0.6F - 0.4F * (1 - num) + ench / 200F);
		float highBound = Math.min(0.95F, 0.8F - 0.4F * (1 - num) + ench / 200F);
		return (int) (level * MathHelper.nextFloat(rand, lowBound, highBound));
	}

	/**
	 * Creates a list of enchantments for a specific slot given various variables.
	 * @param rand Pre-seeded random.
	 * @param stack Itemstack to be enchanted.
	 * @param power Enchantment Power (Eterna Level)
	 * @param quanta Quanta Level
	 * @param arcanaLevel Arcana Level
	 * @param treasure If treasure enchantments can show up.
	 * @return A list of enchantments based on the seed, item, and eterna/quanta/arcana levels.
	 */
	public static List<EnchantmentData> buildEnchantmentList(Random rand, ItemStack stack, int power, float quanta, float arcanaLevel, boolean treasure) {
		List<EnchantmentData> chosenEnchants = Lists.newArrayList();
		int enchantability = stack.getItemEnchantability();
		if (enchantability <= 0) {
			return chosenEnchants;
		} else {
			power = power + rand.nextInt(Math.max(enchantability / 2, 1));
			float factor = MathHelper.nextFloat(rand, -1F, 1F) * quanta / 10;
			power = MathHelper.clamp(Math.round(power + power * factor), 1, 200);
			Arcana arcana = Arcana.getForThreshold(arcanaLevel);
			List<EnchantmentData> allEnchants = getEnchantmentDatas(power, stack, treasure);
			Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
			allEnchants.removeIf(e -> enchants.containsKey(e.enchantment));
			List<ArcanaEnchantmentData> possibleEnchants = allEnchants.stream().map(d -> new ArcanaEnchantmentData(arcana, d)).collect(Collectors.toList());
			if (!possibleEnchants.isEmpty()) {
				chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).data);
				removeIncompatible(possibleEnchants, Util.lastOf(chosenEnchants));

				if (arcanaLevel >= 2.5F && !possibleEnchants.isEmpty()) {
					chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).data);
					removeIncompatible(possibleEnchants, Util.lastOf(chosenEnchants));
				}

				if (arcanaLevel >= 7.5F && !possibleEnchants.isEmpty()) {
					chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).data);
				}

				while (arcanaLevel + rand.nextInt(50) <= power) {
					removeIncompatible(possibleEnchants, Util.lastOf(chosenEnchants));
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