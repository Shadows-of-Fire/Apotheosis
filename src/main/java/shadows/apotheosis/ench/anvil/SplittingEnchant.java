package shadows.apotheosis.ench.anvil;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import shadows.apotheosis.ench.EnchModule;

public class SplittingEnchant extends Enchantment {

	public SplittingEnchant() {
		super(Rarity.RARE, EnchModule.ANVIL, new EquipmentSlotType[0]);
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 20 + enchantmentLevel * 8;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return this.getMinCost(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

}