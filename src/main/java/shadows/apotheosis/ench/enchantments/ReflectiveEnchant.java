package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.ench.EnchModule;

public class ReflectiveEnchant extends Enchantment {

	public ReflectiveEnchant() {
		super(Rarity.RARE, EnchModule.SHIELD, new EquipmentSlotType[] { EquipmentSlotType.OFFHAND, EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 1 + enchantmentLevel * 18;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return this.getMinCost(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return super.canApplyAtEnchantingTable(stack) || stack.getItem().isShield(stack, null);
	}

}