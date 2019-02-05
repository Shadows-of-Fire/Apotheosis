package shadows.ench;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import shadows.Apotheosis;

public class EnchantmentScavenger extends Enchantment {

	protected EnchantmentScavenger() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName(Apotheosis.MODID + ".scavenger");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 1 + level * 25;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

}
