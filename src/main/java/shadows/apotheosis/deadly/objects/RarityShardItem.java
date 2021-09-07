package shadows.apotheosis.deadly.objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
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
	public ITextComponent getName(ItemStack stack) {
		return new TranslationTextComponent(getDescriptionId()).withStyle(Style.EMPTY.withColor(rarity.getColor()));
	}

}
