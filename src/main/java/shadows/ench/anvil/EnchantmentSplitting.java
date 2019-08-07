package shadows.ench.anvil;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class EnchantmentSplitting extends Enchantment {

	public EnchantmentSplitting() {
		super(Rarity.RARE, null, new EquipmentSlotType[0]);
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 20 + enchantmentLevel * 8;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return getMinEnchantability(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return Block.getBlockFromItem(stack.getItem()) instanceof AnvilBlock;
	}

}
