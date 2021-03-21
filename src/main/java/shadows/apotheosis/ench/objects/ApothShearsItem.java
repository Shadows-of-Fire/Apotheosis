package shadows.apotheosis.ench.objects;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import shadows.apotheosis.Apotheosis;

public class ApothShearsItem extends ShearsItem {

	public ApothShearsItem() {
		super(new Item.Properties().maxDamage(238).group(ItemGroup.TOOLS));
		this.setRegistryName("minecraft", "shears");
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
		return super.canApplyAtEnchantingTable(stack, ench) || ench == Enchantments.UNBREAKING || ench == Enchantments.EFFICIENCY;
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