package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.item.Item;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.table.IEnchantableItem;

@Mixin(Item.class)
public class ItemMixin implements IEnchantableItem {

	/**
	 * @author Shadows
	 * @reason Enables all items to be enchantable by default.
	 * @return
	 */
	@Overwrite
	public int getEnchantmentValue() {
		return Apotheosis.enableEnch ? 1 : 0;
	}

}
