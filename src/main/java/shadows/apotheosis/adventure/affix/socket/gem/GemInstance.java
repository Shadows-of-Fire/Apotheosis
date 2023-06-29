package shadows.apotheosis.adventure.affix.socket.gem;

import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.loot.LootRarity;

public record GemInstance(ItemStack gemStack, Gem gem, LootRarity rarity) {

	public GemInstance(ItemStack gemStack) {
		this(gemStack, GemItem.getGem(gemStack), GemItem.getLootRarity(gemStack));
	}

	public boolean isValid() {
		return this.gem != null && this.rarity != null;
	}

	public boolean isValidIn(ItemStack socketed) {
		return isValid() && this.gem.isValidIn(socketed, gemStack, rarity);
	}
}
