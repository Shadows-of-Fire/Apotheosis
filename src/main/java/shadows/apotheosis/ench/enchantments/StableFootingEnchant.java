package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class StableFootingEnchant extends Enchantment {

	public StableFootingEnchant() {
		super(Rarity.RARE, EnchantmentType.ARMOR_FEET, new EquipmentSlotType[] { EquipmentSlotType.FEET });
	}

	@Override
	public int getMinCost(int level) {
		return 40;
	}

	@Override
	public int getMaxCost(int level) {
		return 100;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

}