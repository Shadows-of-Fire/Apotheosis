package shadows.apotheosis.ench.objects;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import shadows.apotheosis.Apotheosis;

public class ApothShearsItem extends ShearsItem {

	public ApothShearsItem() {
		super(new Item.Properties().durability(238).tab(CreativeModeTab.TAB_TOOLS));
		this.setRegistryName("minecraft", "shears");
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
		return super.canApplyAtEnchantingTable(stack, ench) || ench == Enchantments.UNBREAKING || ench == Enchantments.BLOCK_EFFICIENCY | ench == Enchantments.BLOCK_FORTUNE;
	}

	@Override
	public int getEnchantmentValue() {
		return 15;
	}

	@Override
	public String getCreatorModId(ItemStack itemStack) {
		return Apotheosis.MODID;
	}

}