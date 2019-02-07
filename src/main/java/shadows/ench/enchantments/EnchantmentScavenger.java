package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import shadows.Apotheosis;

public class EnchantmentScavenger extends Enchantment {

	public EnchantmentScavenger() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName(Apotheosis.MODID + ".scavenger");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 45 + level * level * 12;
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
