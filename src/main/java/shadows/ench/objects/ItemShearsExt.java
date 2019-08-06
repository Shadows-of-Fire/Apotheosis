package shadows.ench.objects;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;

public class ItemShearsExt extends ItemShears {

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
		return ench == Enchantments.UNBREAKING || ench == Enchantments.EFFICIENCY;
	}

	@Override
	public int getItemEnchantability() {
		return 15;
	}

	@Override
	public String getCreatorModId(ItemStack itemStack) {
		return Apotheosis.MODID;
	}

}
