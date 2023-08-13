package shadows.apotheosis.potion;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;

public class LuckyFootItem extends Item {

    public LuckyFootItem() {
        super(new Item.Properties().stacksTo(1).tab(Apotheosis.APOTH_GROUP));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

}
