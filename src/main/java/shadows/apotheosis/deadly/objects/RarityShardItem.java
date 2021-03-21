package shadows.apotheosis.deadly.objects;

import net.minecraft.item.Item;
import shadows.apotheosis.deadly.affix.LootRarity;

public class RarityShardItem extends Item {

	protected final LootRarity rarity;

	public RarityShardItem(LootRarity rarity, Properties properties) {
		super(properties);
		this.rarity = rarity;
	}

	public LootRarity getRarity() {
		return this.rarity;
	}

}
