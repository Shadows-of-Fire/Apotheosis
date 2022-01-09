package shadows.apotheosis.ench.asm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.EnchantmentInfo;
import shadows.apotheosis.ench.objects.TomeItem;

/**
 * ASM methods for the enchantment module.
 * @author Shadows
 *
 */
public class EnchHooks {

	/**
	 * Allows for special handling of randomly generated enchantments.
	 * Called from {@link EnchantmentHelper#getEnchantmentDatas(int, ItemStack, boolean)}
	 * Injected by apothasm/enchantment-datas.js
	 */
	public static List<EnchantmentInstance> getEnchantmentDatas(int power, ItemStack stack, boolean allowTreasure) {
		List<EnchantmentInstance> list = new ArrayList<>();
		boolean isBook = stack.getItem() == Items.BOOK;
		boolean typedBook = stack.getItem() instanceof TomeItem;
		for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
			if (enchantment.isTreasureOnly() && !allowTreasure) continue;
			if (enchantment.canApplyAtEnchantingTable(stack) || isBook && enchantment.isAllowedOnBooks() || typedBook && stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
				EnchantmentInfo info = EnchModule.getEnchInfo(enchantment);
				for (int i = info.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
					if (power >= info.getMinPower(i) && power <= info.getMaxPower(i)) {
						list.add(new EnchantmentInstance(enchantment, i));
						break;
					}
				}
			}
		}
		return list;
	}

	/**
	 * Replaces the call to {@link Enchantment#getMaxLevel()} in {@link ContainerRepair#updateRepairOutput()}
	 * Injected by apothasm/container-repair.js
	 */
	public static int getMaxLevel(Enchantment ench) {
		if (!Apotheosis.enableEnch) return ench.getMaxLevel();
		return EnchModule.getEnchInfo(ench).getMaxLevel();
	}

	/**
	 * Override for protection calculations.  Removes the hard cap of total level 20.  Effectiveness is reduced past 20.
	 * Called from {@link CombatRules#getDamageAfterMagicAbsorb(float, float)}
	 * Injected by apothasm/combat-rules.js
	 */
	public static float getDamageAfterMagicAbsorb(float damage, float enchantModifiers) {
		float clamped = Mth.clamp(enchantModifiers, 0, 20);
		float remaining = Mth.clamp(enchantModifiers - 20, 0, 44);
		float factor = 1 - clamped / 25;
		if (remaining > 0) {
			factor -= 0.2F * remaining / 60;
		}
		return damage * factor;
	}

	/**
	 * Calculates the delay for catching a fish.  Ensures that the value never returns <= 0, so that it doesn't get infinitely locked.
	 * Called at the end of {@link FishingBobberEntity#catchingFish(BlockPos)}
	 * Injected by apothasm/fishing-bobber.js
	 */
	public static int getTicksCaughtDelay(FishingHook bobber) {
		int lowBound = Math.max(1, 100 - bobber.lureSpeed * 10);
		int highBound = Math.max(lowBound, 600 - bobber.lureSpeed * 60);
		return Mth.nextInt(bobber.random, lowBound, highBound);
	}

}