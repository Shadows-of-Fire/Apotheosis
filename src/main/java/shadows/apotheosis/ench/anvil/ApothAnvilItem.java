package shadows.apotheosis.ench.anvil;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import shadows.apotheosis.Apotheosis;

public class ApothAnvilItem extends BlockItem {

	public ApothAnvilItem(Block block) {
		super(block, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
		this.setRegistryName(block.getRegistryName());
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return stack.getCount() == 1 && (enchantment == Enchantments.UNBREAKING || enchantment.category.canEnchant(this));
	}

	@Override
	public String getCreatorModId(ItemStack itemStack) {
		return Apotheosis.MODID;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return stack.getCount() == 1;
	}

	@Override
	public int getItemEnchantability(ItemStack stack) {
		return 50;
	}

}