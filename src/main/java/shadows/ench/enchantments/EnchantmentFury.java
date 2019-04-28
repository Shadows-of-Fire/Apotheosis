package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;

public class EnchantmentFury extends Enchantment {

	public EnchantmentFury() {
		super(Rarity.UNCOMMON, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		setName("apotheosis.fury");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 28 + level * 7;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 7;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof ItemAxe ? true : super.canApply(stack);
	}

}
