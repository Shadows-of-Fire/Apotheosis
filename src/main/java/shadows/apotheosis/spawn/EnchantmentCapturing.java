package shadows.apotheosis.spawn;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.EquipmentSlotType;

public class EnchantmentCapturing extends Enchantment {

	protected EnchantmentCapturing() {
		super(Rarity.VERY_RARE, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public int getMinEnchantability(int level) {
		return Enchantments.FORTUNE.getMinEnchantability(level);
	}

	@Override
	public int getMaxEnchantability(int level) {
		return Enchantments.FORTUNE.getMaxEnchantability(level) + (level == getMaxLevel() ? 50 : 0);
	}

}
