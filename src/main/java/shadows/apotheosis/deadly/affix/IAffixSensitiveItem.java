package shadows.apotheosis.deadly.affix;

import net.minecraft.item.ItemStack;

/**
 * Allows an item to deny certain impacts of affix application.
 */
public interface IAffixSensitiveItem {

	/**
	 * Determines if the item will receive affix attribute modifiers.
	 */
	public boolean receivesAttributes(ItemStack stack);

	/**
	 * Determines if the item will receive tooltips caused by affixes.
	 */
	public boolean receivesTooltips(ItemStack stack);

}
