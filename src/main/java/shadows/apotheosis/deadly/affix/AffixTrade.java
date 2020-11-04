package shadows.apotheosis.deadly.affix;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.util.WeightedRandom;
import shadows.apotheosis.deadly.reload.AffixLootManager;

public class AffixTrade implements ITrade {

	@Override
	public MerchantOffer getOffer(Entity merchant, Random rand) {
		LootRarity rarity = LootRarity.random(rand);
		AffixLootEntry entry = WeightedRandom.getRandomItem(rand, AffixLootManager.getEntries());
		ItemStack loot = AffixLootManager.genLootItem(entry.getStack().copy(), rand, rarity);
		loot.getTag().putBoolean("apoth_merchant", true);
		ItemStack price1 = new ItemStack(Items.EMERALD, 4 + rarity.ordinal() * 12);
		return new MerchantOffer(price1, ItemStack.EMPTY, loot, 1, 30, 1);
	}

}