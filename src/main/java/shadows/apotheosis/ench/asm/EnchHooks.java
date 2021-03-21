package shadows.apotheosis.ench.asm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
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
	public static List<EnchantmentData> getEnchantmentDatas(int power, ItemStack stack, boolean allowTreasure) {
		List<EnchantmentData> list = new ArrayList<>();
		boolean isBook = stack.getItem() == Items.BOOK;
		boolean typedBook = stack.getItem() instanceof TomeItem;
		for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
			if (enchantment.isTreasureEnchantment() && !allowTreasure) continue;
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
	 * Injected by apothasm/tempting.js
	 */
	public static boolean isTempting(boolean was, ItemStack stack) {
		if (EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.TEMPTING, stack) > 0) return true;
		return was;
	}

	/**
	 * Enables application of the reflective defenses enchantment.
	 * Called from {@link LivingEntity#blockUsingShield(LivingEntity)}
	 * Injected by apothasm/reflective.js
	 */
	public static void reflectiveHook(LivingEntity user, LivingEntity attacker) {
		int level;
		if ((level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.REFLECTIVE, user.getActiveItemStack())) > 0) {
			if (user.world.rand.nextInt(Math.max(2, 7 - level)) == 0) {
				DamageSource src = user instanceof PlayerEntity ? DamageSource.causePlayerDamage((PlayerEntity) user).setMagicDamage().setDamageBypassesArmor() : DamageSource.MAGIC;
				attacker.attackEntityFrom(src, level * 1.6F);
				user.getActiveItemStack().damageItem(10, attacker, e -> {
					e.sendBreakAnimation(EquipmentSlotType.OFFHAND);
				});
			}
		}
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
		float clamped = MathHelper.clamp(enchantModifiers, 0, 20);
		float remaining = MathHelper.clamp(enchantModifiers - 20, 0, 44);
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
	public static int getTicksCaughtDelay(FishingBobberEntity bobber) {
		int lowBound = Math.max(1, 100 - bobber.lureSpeed * 10);
		int highBound = Math.max(lowBound, 600 - bobber.lureSpeed * 60);
		return MathHelper.nextInt(bobber.rand, lowBound, highBound);
	}

	/**
	 * Handles the usage of the Crescendo of Bolts enchantment.
	 * The enchantment gives the crossbow extra shots per charge, one per enchantment level.
	 * Called from {@link CrossbowItem#onItemRightClick}, before the first return.
	 * Injected by apothasm/crossbow.js
	 */
	public static void onArrowFired(ItemStack crossbow) {
		int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.CRESCENDO, crossbow);
		if (level > 0 && nbt.get() != null) {
			int shots = crossbow.getTag().getInt("shots");
			if (shots < level) {
				crossbow.getTag().putInt("shots", shots + 1);
				CrossbowItem.setCharged(crossbow, true);
				crossbow.getTag().put("ChargedProjectiles", nbt.get());
			} else {
				crossbow.getTag().remove("shots");
			}
			nbt.set(null);
		}
	}

	private static ThreadLocal<ListNBT> nbt = new ThreadLocal<>();

	/**
	 * Early hook for the Crescendo of Bolts enchantment.
	 * This is required to retain the ammunition's nbt data, as it is thrown away before {@link EnchHooks#onArrowFired}.
	 * Injected by apothasm/crossbow.js
	 */
	public static void preArrowFired(ItemStack crossbow) {
		int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.CRESCENDO, crossbow);
		if (level > 0) {
			nbt.set(crossbow.getTag().getList("ChargedProjectiles", Constants.NBT.TAG_COMPOUND).copy());
		}
	}

	/**
	 * Arrow fired hook for the Crescendo of Bolts enchantment.
	 * This is required to mark generated arrows as creative-only so arrows are not duplicated.
	 * Injected by apothasm/crossbow-arrows.js
	 */
	public static void markGeneratedArrows(ProjectileEntity arrow, ItemStack crossbow) {
		if (crossbow.getTag().getInt("shots") > 0 && arrow instanceof AbstractArrowEntity) {
			((AbstractArrowEntity) arrow).pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
		}
	}

}