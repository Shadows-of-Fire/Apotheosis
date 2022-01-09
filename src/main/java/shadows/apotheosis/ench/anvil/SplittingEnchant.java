package shadows.apotheosis.ench.anvil;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import shadows.apotheosis.ench.EnchModule;

public class SplittingEnchant extends Enchantment {

	public SplittingEnchant() {
		super(Rarity.RARE, EnchModule.ANVIL, new EquipmentSlot[0]);
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 15;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return 200;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

}