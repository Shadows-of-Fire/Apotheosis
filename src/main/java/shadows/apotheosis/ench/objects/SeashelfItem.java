package shadows.apotheosis.ench.objects;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

public class SeashelfItem extends BlockItem {

	public SeashelfItem(Block block) {
		super(block, new Item.Properties().tab(Apotheosis.APOTH_GROUP));
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
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		super.fillItemCategory(group, items);
		if (this.allowdedIn(group)) {
			ItemStack s = new ItemStack(this);
			ListTag list = new ListTag();
			CompoundTag tag = new CompoundTag();
			tag.putString("id", "apotheosis:sea_infusion");
			tag.putShort("lvl", (short) 5);
			list.add(tag);
			s.addTagElement("Enchantments", list);
			items.add(s);
		}
	}

}