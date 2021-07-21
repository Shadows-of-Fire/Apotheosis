package shadows.apotheosis.spawn.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

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
		return 35 + (level - 1) * 15;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 15;
	}

}