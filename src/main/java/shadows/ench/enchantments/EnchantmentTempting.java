package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;

public class EnchantmentTempting extends Enchantment {

	public EnchantmentTempting() {
		super(Rarity.UNCOMMON, null, new EntityEquipmentSlot[0]);
		setName(Apotheosis.MODID + ".tempting");
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof ItemHoe;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.getItem() instanceof ItemHoe;
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 0;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return 500;
	}

}
