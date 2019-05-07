package shadows.potion;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import shadows.Apotheosis;

public class EnchantmentTrueInfinity extends Enchantment {

	protected EnchantmentTrueInfinity() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.BOW, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName("apotheosis.true_infinity");
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return Apotheosis.enableEnch ? 65 : 31;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return 135;
	}

}
