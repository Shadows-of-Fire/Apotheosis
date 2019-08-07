package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;

public class EnchantmentTempting extends Enchantment {

	public EnchantmentTempting() {
		super(Rarity.UNCOMMON, null, new EquipmentSlotType[0]);
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof HoeItem;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.getItem() instanceof HoeItem;
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 0;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return 80;
	}

}
