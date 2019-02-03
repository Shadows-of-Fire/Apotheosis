package shadows.anvil;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;

public class ItemAnvilExt extends ItemBlock {

	public ItemAnvilExt(Block block) {
		super(block);
		setRegistryName("minecraft", "anvil");
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return !stack.hasEffect() && enchantment == Enchantments.UNBREAKING;
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
