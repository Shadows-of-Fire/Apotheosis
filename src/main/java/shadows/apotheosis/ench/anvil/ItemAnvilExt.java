package shadows.apotheosis.ench.anvil;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

public class ItemAnvilExt extends BlockItem {

	public ItemAnvilExt(Block block) {
		super(block, new Item.Properties().group(ItemGroup.DECORATIONS));
		setRegistryName(block.getRegistryName());
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return stack.getCount() == 1 && (enchantment == Enchantments.UNBREAKING || enchantment == ApotheosisObjects.SPLITTING);
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
