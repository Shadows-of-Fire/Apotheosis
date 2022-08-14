package shadows.apotheosis.adventure.affix.trades;

import java.util.Random;

import com.google.gson.annotations.SerializedName;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.village.wanderer.JsonTrade;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;

public class AffixTrade extends TypeKeyedBase<JsonTrade> implements JsonTrade {

	@SerializedName("rarity_offset")
	protected final int rarityOffset;
	protected final boolean rare;

	public AffixTrade(int rarityOffset, boolean rare) {
		this.rarityOffset = rarityOffset;
		this.rare = rare;
	}

	@Override
	public MerchantOffer getOffer(Entity pTrader, Random pRand) {
		ItemStack affixItem = LootController.createRandomLootItem(pRand, rarityOffset, 0);
		affixItem.getTag().putBoolean("apoth_merchant", true);
		ItemStack stdItem = affixItem.copy();
		stdItem.setTag(null);
		LootRarity rarity = AffixHelper.getRarity(affixItem);
		ItemStack emeralds = new ItemStack(Items.EMERALD, 8 + rarity.ordinal() * 8);
		if (rarity.isAtLeast(LootRarity.MYTHIC)) {
			emeralds = new ItemStack(Items.EMERALD_BLOCK, 20 + (rarity == LootRarity.ANCIENT ? 30 : 0));
		}
		return new MerchantOffer(stdItem, emeralds, affixItem, 1, 100, 1);
	}

	@Override
	public boolean isRare() {
		return rare;
	}

}
