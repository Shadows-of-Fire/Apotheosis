package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class EnchantmentReflective extends Enchantment {

	public EnchantmentReflective() {
		super(Rarity.RARE, null, new EquipmentSlotType[] { EquipmentSlotType.OFFHAND, EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 1 + enchantmentLevel * 15;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return getMinEnchantability(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.getItem().isShield(stack, null);
	}

}
