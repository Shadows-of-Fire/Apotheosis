package shadows.apotheosis.ench.objects;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

public class HellshelfItem extends BlockItem {

	public HellshelfItem(Block block) {
		super(block, new Item.Properties().tab(Apotheosis.APOTH_GROUP));
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
		super.fillItemCategory(group, items);
		if (this.allowdedIn(group)) {
			ItemStack s = new ItemStack(this);
			ListNBT list = new ListNBT();
			CompoundNBT tag = new CompoundNBT();
			tag.putString("id", "apotheosis:hell_infusion");
			tag.putShort("lvl", (short) 3);
			list.add(tag);
			s.addTagElement("Enchantments", list);
			items.add(s);
		}
	}

	@Override
	public String getDescriptionId(ItemStack pStack) {
		return EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.HELL_INFUSION, pStack) >= 3 ? "block.apotheosis.infused_hellshelf" : super.getDescriptionId();
	}

}