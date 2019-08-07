package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class EnchantmentBerserk extends Enchantment {

	public EnchantmentBerserk() {
		super(Rarity.VERY_RARE, EnchantmentType.ARMOR, new EquipmentSlotType[] { EquipmentSlotType.CHEST, EquipmentSlotType.LEGS });
	}

	@Override
	public int getMinEnchantability(int level) {
		return 60 + level * 15;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 4;
	}

	@Override
	public boolean isCurse() {
		return true;
	}

}
