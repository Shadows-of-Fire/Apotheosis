package shadows.apotheosis.ench.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class InertEnchantment extends Enchantment {

	public InertEnchantment() {
		super(Rarity.VERY_RARE, null, new EquipmentSlot[0]);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return false;
	}

}
