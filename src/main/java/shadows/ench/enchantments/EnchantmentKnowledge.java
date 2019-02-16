package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentKnowledge extends Enchantment {

	public EnchantmentKnowledge() {
		super(Rarity.RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName("apotheosis.knowledge");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 50 + (level - 1) * 12;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}
}