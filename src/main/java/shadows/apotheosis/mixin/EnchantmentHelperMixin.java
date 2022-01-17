package shadows.apotheosis.mixin;

import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import shadows.apotheosis.ench.table.RealEnchantmentHelper;

public class EnchantmentHelperMixin {

	/**
	 * @author Shadows
	 * @reason Enables apotheosis special handling of enchanting rules.  More lenient injection is not possible.
	 * @param power The current enchanting power.
	 * @param stack The ItemStack being enchanted.
	 * @param allowTreasure If treasure enchantments are allowed.
	 * @return All possible enchantments that are eligible to be placed on this item at a specific power level.
	 */
	@Overwrite
	public static List<EnchantmentInstance> getAvailableEnchantmentResults(int power, ItemStack stack, boolean allowTreasure) {
		return RealEnchantmentHelper.getAvailableEnchantmentResults(power, stack, allowTreasure);
	}

	/**
	 * @author Shadows
	 * @reason Enables global consistency with the apotheosis enchanting system, even outside the table.
	 * @param pRandom The random
	 * @param pItemStack The stack being enchanted
	 * @param pLevel The enchanting level
	 * @param pAllowTreasure If treasure enchantments are allowed.
	 * @return A list of enchantments to apply to this item.
	 */
	@Overwrite
	public static List<EnchantmentInstance> selectEnchantment(Random pRandom, ItemStack pItemStack, int pLevel, boolean pAllowTreasure) {
		return RealEnchantmentHelper.selectEnchantment(pRandom, pItemStack, pLevel, 0, 0, 0, pAllowTreasure);
	}

}
