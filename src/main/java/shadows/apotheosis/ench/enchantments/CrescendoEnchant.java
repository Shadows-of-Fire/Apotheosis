package shadows.apotheosis.ench.enchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.mixin.CrossbowItemMixin;

public class CrescendoEnchant extends Enchantment {

	public CrescendoEnchant() {
		super(Rarity.RARE, EnchantmentCategory.CROSSBOW, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinCost(int level) {
		return 45 + (level - 1) * 20;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 50;
	}

	@Override
	public Component getFullname(int level) {
		return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_GREEN);
	}

	/**
	 * Handles the usage of the Crescendo of Bolts enchantment.
	 * The enchantment gives the crossbow extra shots per charge, one per enchantment level.
	 * Called from {@link CrossbowItem#use}, before the first return.
	 * Injected by {@link CrossbowItemMixin}
	 */
	public static void onArrowFired(ItemStack crossbow) {
		int level = EnchantmentHelper.getItemEnchantmentLevel(Apoth.Enchantments.CRESCENDO, crossbow);
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

	private static ThreadLocal<ListTag> nbt = new ThreadLocal<>();

	/**
	 * Early hook for the Crescendo of Bolts enchantment.
	 * This is required to retain the ammunition's nbt data, as it is thrown away before {@link EnchHooks#onArrowFired}.
	 * Injected by apothasm/crossbow.js
	 */
	public static void preArrowFired(ItemStack crossbow) {
		int level = EnchantmentHelper.getItemEnchantmentLevel(Apoth.Enchantments.CRESCENDO, crossbow);
		if (level > 0) {
			nbt.set(crossbow.getTag().getList("ChargedProjectiles", Tag.TAG_COMPOUND).copy());
		}
	}

	/**
	 * Arrow fired hook for the Crescendo of Bolts enchantment.
	 * This is required to mark generated arrows as creative-only so arrows are not duplicated.
	 * Injected by apothasm/crossbow-arrows.js
	 */
	public static void markGeneratedArrows(Projectile arrow, ItemStack crossbow) {
		if (crossbow.getTag().getInt("shots") > 0 && arrow instanceof AbstractArrow arr) {
			arr.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
		}
	}
}