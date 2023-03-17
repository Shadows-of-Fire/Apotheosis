package shadows.apotheosis.adventure.affix.socket.gem;

import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.loot.LootRarity;

public record GemInstance(ItemStack gemStack, Gem gem, LootRarity rarity, int facets) {

	public GemInstance(ItemStack gemStack) {
		this(gemStack, GemItem.getGem(gemStack), GemItem.getLootRarity(gemStack), GemItem.getFacets(gemStack));
	}

	// TODO: Remove
	public static GemInstance orLegacy(ItemStack gemStack) {
		return new GemInstance(gemStack, GemItem.getGemOrLegacy(gemStack), GemItem.getLootRarity(gemStack), GemItem.getFacets(gemStack));
	}

	public boolean isValid() {
		return this.gem != null && this.rarity != null && facets >= 0 && facets <= this.gem.getMaxFacets(rarity);
	}

	public boolean isValidIn(ItemStack socketed) {
		return isValid() && this.gem.isValidIn(socketed, gemStack, rarity);
	}

	public int maxFacets() {
		return this.gem.getMaxFacets(this.rarity);
	}

	public boolean isMaxed() {
		return this.facets == this.maxFacets();
	}
}
