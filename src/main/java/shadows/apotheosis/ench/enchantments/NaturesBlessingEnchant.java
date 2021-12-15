package shadows.apotheosis.ench.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import shadows.apotheosis.ench.EnchModule;

public class NaturesBlessingEnchant extends Enchantment {

	public NaturesBlessingEnchant() {
		super(Rarity.RARE, EnchModule.HOE, new EquipmentSlot[0]);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.getItem() instanceof HoeItem;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinCost(int level) {
		return 30 + level * 10;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 30;
	}

}