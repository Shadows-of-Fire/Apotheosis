package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;

public class EnchantmentNatureBless extends Enchantment {

	public EnchantmentNatureBless() {
		super(Rarity.RARE, null, new EquipmentSlotType[0]);
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
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinEnchantability(int level) {
		return 25 + 10 * level;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 30;
	}

}
