package shadows.ench;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;

public class ItemHellBookshelf extends ItemBlock {

	public ItemHellBookshelf(Block block) {
		super(block);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return !stack.hasEffect() && stack.getCount() == 1 && enchantment == ApotheosisObjects.HELL_INFUSION;
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
