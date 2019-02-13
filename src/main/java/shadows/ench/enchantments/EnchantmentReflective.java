package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;

public class EnchantmentReflective extends Enchantment {

	public EnchantmentReflective() {
		super(Rarity.RARE, null, new EntityEquipmentSlot[] { EntityEquipmentSlot.OFFHAND });
		setName(Apotheosis.MODID + ".reflective");
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 1 + enchantmentLevel * 15;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return this.getMinEnchantability(enchantmentLevel) + 40;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.getItem().isShield(stack, null);
	}

}
