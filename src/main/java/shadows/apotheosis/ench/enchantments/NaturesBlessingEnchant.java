package shadows.apotheosis.ench.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.ench.EnchModule;

public class NaturesBlessingEnchant extends Enchantment {

	public NaturesBlessingEnchant() {
		super(Rarity.RARE, EnchModule.HOE, new EquipmentSlotType[0]);
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return stack.getItem() instanceof HoeItem;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinEnchantability(int level) {
		return 30 + level * 10;
	}

	@Override
	public int getMaxEnchantability(int level) {
		return this.getMinEnchantability(level) + 30;
	}

}