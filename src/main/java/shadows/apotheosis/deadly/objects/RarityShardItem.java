package shadows.apotheosis.deadly.objects;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

	@Override
	public Component getName(ItemStack stack) {
		return new TranslatableComponent(getDescriptionId()).withStyle(Style.EMPTY.withColor(rarity.getColor()));
	}

}
