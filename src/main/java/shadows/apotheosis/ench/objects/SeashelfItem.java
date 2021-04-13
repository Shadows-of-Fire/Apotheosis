package shadows.apotheosis.ench.objects;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

public class SeashelfItem extends BlockItem {

	public SeashelfItem(Block block) {
		super(block, new Item.Properties().group(Apotheosis.APOTH_GROUP));
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return !stack.isEnchanted() && stack.getCount() == 1 && enchantment == ApotheosisObjects.SEA_INFUSION;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}

	@Override
	public int getItemEnchantability(ItemStack stack) {
		return 65;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		super.fillItemGroup(group, items);
		if (this.isInGroup(group)) {
			ItemStack s = new ItemStack(this);
			ListNBT list = new ListNBT();
			CompoundNBT tag = new CompoundNBT();
			tag.putString("id", "apotheosis:sea_infusion");
			tag.putShort("lvl", (short) 5);
			list.add(tag);
			s.setTagInfo("Enchantments", list);
			items.add(s);
		}
	}

}