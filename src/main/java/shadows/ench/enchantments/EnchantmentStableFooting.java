package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import shadows.Apotheosis;

public class EnchantmentStableFooting extends Enchantment {

	public EnchantmentStableFooting() {
		super(Rarity.RARE, EnumEnchantmentType.ARMOR_FEET, new EntityEquipmentSlot[] { EntityEquipmentSlot.FEET });
		setName(Apotheosis.MODID + ".stable_footing");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 40;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return 100;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

}
