package shadows.ench.enchantments;

import net.minecraft.enchantment.EnchantmentDigging;
import net.minecraft.inventory.EntityEquipmentSlot;
import shadows.Apotheosis;

public class EnchantmentDepths extends EnchantmentDigging {

	public EnchantmentDepths() {
		super(Rarity.UNCOMMON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName(Apotheosis.MODID + ".depth_miner");
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 1 + 13 * (enchantmentLevel - 1);
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return getMinEnchantability(enchantmentLevel) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 7;
	}

}
