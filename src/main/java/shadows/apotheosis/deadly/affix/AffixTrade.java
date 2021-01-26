package shadows.apotheosis.deadly.affix;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import shadows.apotheosis.deadly.reload.AffixLootManager;

public class AffixTrade implements ITrade {

	@Override
	public MerchantOffer getOffer(Entity merchant, Random rand) {
		LootRarity rarity = LootRarity.random(rand);
		ItemStack stack = AffixLootManager.getRandomEntry(rand, rarity);
		stack = AffixLootManager.genLootItem(stack, rand, rarity);
		stack.getTag().putBoolean("apoth_merchant", true);
		return new MerchantOffer(new ItemStack(stack.getItem()), new ItemStack(Items.EMERALD, rarity.ordinal() * 10), 1, 100, 1);
	}

}