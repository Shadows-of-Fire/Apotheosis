package shadows.apotheosis.spawn.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.ench.EnchModule;

public class CapturingEnchant extends Enchantment {

	public CapturingEnchant() {
		super(Rarity.VERY_RARE, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public int getMinCost(int level) {
		return 28 + (level - 1) * 15;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 15;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return super.canApplyAtEnchantingTable(stack) || EnchModule.AXE.canEnchant(stack.getItem());
	}

}