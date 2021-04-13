package shadows.apotheosis.potion;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.Apotheosis;

public class LuckyFootItem extends Item {

	public LuckyFootItem() {
		super(new Item.Properties().maxStackSize(1).group(Apotheosis.APOTH_GROUP));
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

}