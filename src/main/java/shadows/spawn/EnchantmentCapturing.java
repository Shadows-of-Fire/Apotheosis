package shadows.spawn;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentCapturing extends Enchantment {

	protected EnchantmentCapturing() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName("spw.capturing");
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public int getMinEnchantability(int level) {
		return Enchantments.FORTUNE.getMinEnchantability(level);
	}

	@Override
	public int getMaxEnchantability(int level) {
		return Enchantments.FORTUNE.getMaxEnchantability(level) + (level == getMaxLevel() ? 50 : 0);
	}

}
