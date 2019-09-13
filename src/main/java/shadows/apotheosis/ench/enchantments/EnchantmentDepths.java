package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.EfficiencyEnchantment;
import net.minecraft.inventory.EquipmentSlotType;

public class EnchantmentDepths extends EfficiencyEnchantment {

	public EnchantmentDepths() {
		super(Rarity.RARE, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 1 + 13 * (enchantmentLevel - 1);
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return getMinEnchantability(enchantmentLevel) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 7;
	}

}
