package shadows.ench.asm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;
import shadows.ench.EnchModule;
import shadows.ench.EnchantmentInfo;
import shadows.ench.objects.ItemTypedBook;

/**
 * ASM methods for the enchantment module.
 * @author Shadows
 *
 */
public class EnchHooks {

	/**
	 * Allows for special handling of randomly generated enchantments.
	 * Called from {@link EnchantmentHelper#getEnchantmentDatas(int, ItemStack, boolean)}
	 * Injected by javascript/enchantment-datas.js
	 */
	public static List<EnchantmentData> getEnchantmentDatas(int power, ItemStack stack, boolean allowTreasure) {
		List<EnchantmentData> list = new ArrayList<>();
		boolean isBook = stack.getItem() == Items.BOOK;
		boolean typedBook = stack.getItem() instanceof ItemTypedBook;
		for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
			if (enchantment.isTreasureEnchantment() && !allowTreasure || EnchModule.BLACKLISTED_ENCHANTS.contains(enchantment)) continue;
			if (enchantment.canApplyAtEnchantingTable(stack) || isBook && enchantment.isAllowedOnBooks() || typedBook && stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
				EnchantmentInfo info = EnchModule.getEnchInfo(enchantment);
				for (int i = info.getMaxLevel(); i > info.getMinLevel() - 1; --i) {
					if (power >= info.getMinPower(i) && power <= info.getMaxPower(i)) {
						list.add(new EnchantmentData(enchantment, i));
						break;
					}
				}
			}
		}
		return list;
	}

	/**
	 * Allows checking if an item with Tempting can make an animal follow.
	 * Called from {@link TemptGoal#isTempting(ItemStack)}
	 * Injected by javascript/tempting.js
	 */
	public static boolean isTempting(boolean was, ItemStack stack) {
		if (EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.TEMPTING, stack) > 0) return true;
		return was;
	}

	/**
	 * Enables application of the reflective defenses enchantment.
	 * Called from {@link LivingEntity#blockUsingShield(LivingEntity)}
	 * Injected by javascript/reflective.js
	 */
	public static void reflectiveHook(LivingEntity user, LivingEntity attacker) {
		int level;
		if ((level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.REFLECTIVE, user.getActiveItemStack())) > 0) {
			if (user.world.rand.nextInt(Math.max(1, 7 - level)) == 0) {
				DamageSource src = user instanceof PlayerEntity ? DamageSource.causePlayerDamage((PlayerEntity) user) : DamageSource.MAGIC;
				attacker.attackEntityFrom(src, level * 1.6F);
				user.getActiveItemStack().damageItem(10, attacker, (e) -> {
					e.sendBreakAnimation(EquipmentSlotType.OFFHAND);
				});
			}
		}
	}

	/**
	 * Replaces the call to {@link Enchantment#getMaxLevel()} in {@link ContainerRepair#updateRepairOutput()}
	 * Injected by javascript/container-repair.js
	 */
	public static int getMaxLevel(Enchantment ench) {
		if (!Apotheosis.enableEnch) return ench.getMaxLevel();
		return EnchModule.getEnchInfo(ench).getMaxLevel();
	}

}
