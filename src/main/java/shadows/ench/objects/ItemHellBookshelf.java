package shadows.ench.objects;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;
import shadows.ench.EnchModule;

public class ItemHellBookshelf extends BlockItem {

	public ItemHellBookshelf(Block block) {
		super(block, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS));
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return !stack.isEnchanted() && stack.getCount() == 1 && enchantment == ApotheosisObjects.HELL_INFUSION;
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
		return 40;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		super.fillItemGroup(group, items);
		if (isInGroup(group)) {
			ItemStack s = new ItemStack(this);
			EnchantmentHelper.setEnchantments(ImmutableMap.of(ApotheosisObjects.HELL_INFUSION, EnchModule.getEnchInfo(ApotheosisObjects.HELL_INFUSION).getMaxLevel()), s);
			items.add(s);
		}
	}

}
