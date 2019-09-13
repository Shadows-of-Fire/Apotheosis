package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import shadows.apotheosis.ench.EnchModule;

public class EnchantmentTempting extends Enchantment {

	public EnchantmentTempting() {
		super(Rarity.UNCOMMON, EnchModule.HOE, new EquipmentSlotType[0]);
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 0;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return 80;
	}

}
