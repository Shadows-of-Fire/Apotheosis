package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import shadows.apotheosis.ench.EnchModule;

public class TemptingEnchant extends Enchantment {

	public TemptingEnchant() {
		super(Rarity.UNCOMMON, EnchModule.HOE, new EquipmentSlotType[0]);
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 0;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return 80;
	}

}