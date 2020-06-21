package shadows.apotheosis.spawn.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class EnchantmentCapturing extends Enchantment {

	public EnchantmentCapturing() {
		super(Rarity.VERY_RARE, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public int getMinEnchantability(int level) {
		return 35 + (level - 1) * 9;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 9;
	}

}
