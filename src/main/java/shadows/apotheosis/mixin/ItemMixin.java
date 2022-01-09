package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.item.Item;
import shadows.apotheosis.ench.table.IEnchantableItem;

@Mixin(Item.class)
public class ItemMixin implements IEnchantableItem {

	@Overwrite
	public int getEnchantmentValue() {
		return 1;
	}

}
