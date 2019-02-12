package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;

public class EnchantmentLifeMend extends Enchantment {

	public EnchantmentLifeMend() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.BREAKABLE, EntityEquipmentSlot.values());
		setName(Apotheosis.MODID + ".life_mending");
	}

	@Override
	public int getMinEnchantability(int level) {
		return 80 + level * 15;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return getMinEnchantability(level) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean isCurse() {
		return true;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.getItem().isShield(stack, null) || super.canApplyAtEnchantingTable(stack);
	}

}
