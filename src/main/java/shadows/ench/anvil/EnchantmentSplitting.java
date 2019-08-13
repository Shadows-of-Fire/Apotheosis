package shadows.ench.anvil;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import shadows.ench.EnchModule;

public class EnchantmentSplitting extends Enchantment {

	public EnchantmentSplitting() {
		super(Rarity.RARE, EnchModule.ANVIL, new EquipmentSlotType[0]);
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 20 + enchantmentLevel * 8;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return getMinEnchantability(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

}
