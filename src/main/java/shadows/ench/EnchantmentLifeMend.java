package shadows.ench;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import shadows.Apotheosis;

public class EnchantmentLifeMend extends Enchantment {

	protected EnchantmentLifeMend() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.BREAKABLE, EntityEquipmentSlot.values());
		setName(Apotheosis.MODID + ".life_mending");
	}

	public int getMinEnchantability(int level) {
		return level * 80;
	}

	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 50;
	}

	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean isCurse() {
		return true;
	}

}
