package shadows.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;

public class EnchantmentShieldBash extends Enchantment {

	public EnchantmentShieldBash() {
		super(Rarity.RARE, null, new EntityEquipmentSlot[] { EntityEquipmentSlot.OFFHAND });
		setName(Apotheosis.MODID + ".shield_bash");
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 1 + (enchantmentLevel - 1) * 11;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel) {
		return this.getMinEnchantability(enchantmentLevel) + 20;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.getItem().isShield(stack, null) || super.canApplyAtEnchantingTable(stack);
	}

}