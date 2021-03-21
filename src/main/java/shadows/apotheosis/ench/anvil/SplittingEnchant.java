package shadows.apotheosis.ench.anvil;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import shadows.apotheosis.ench.EnchModule;

public class SplittingEnchant extends Enchantment {

	public SplittingEnchant() {
		super(Rarity.RARE, EnchModule.ANVIL, new EquipmentSlotType[0]);
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 20 + enchantmentLevel * 8;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return this.getMinEnchantability(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

}