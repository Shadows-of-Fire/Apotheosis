package shadows.ench.objects;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import shadows.Apotheosis;

public class ItemShearsExt extends ShearsItem {

	public ItemShearsExt() {
		super(new Item.Properties().maxDamage(238).group(ItemGroup.TOOLS));
		setRegistryName("minecraft", "shears");
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
		return ench.canApply(stack) || ench == Enchantments.UNBREAKING || ench == Enchantments.EFFICIENCY;
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
