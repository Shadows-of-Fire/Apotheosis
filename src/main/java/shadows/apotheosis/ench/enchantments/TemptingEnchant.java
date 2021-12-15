package shadows.apotheosis.ench.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import shadows.apotheosis.ench.EnchModule;

public class TemptingEnchant extends Enchantment {

	public TemptingEnchant() {
		super(Rarity.UNCOMMON, EnchModule.HOE, new EquipmentSlot[0]);
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