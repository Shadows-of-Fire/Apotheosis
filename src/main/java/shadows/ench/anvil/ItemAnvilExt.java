package shadows.ench.anvil;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemAnvilBlock;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;

public class ItemAnvilExt extends ItemAnvilBlock {

	public ItemAnvilExt(Block block) {
		super(block);
		setRegistryName("minecraft", "anvil");
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
