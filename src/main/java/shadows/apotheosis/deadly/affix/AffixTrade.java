package shadows.apotheosis.deadly.affix;

import java.util.Random;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import shadows.apotheosis.deadly.reload.AffixLootManager;

public class AffixTrade implements ItemListing {

	@Override
	public MerchantOffer getOffer(Entity merchant, Random rand) {
		LootRarity rarity = LootRarity.random(rand);
		AffixLootEntry entry = AffixLootManager.getRandomEntry(rand);
		ItemStack stack = entry.getStack().copy();
		stack = AffixLootManager.genLootItem(stack, rand, entry.getType(), rarity);
		stack.getTag().putBoolean("apoth_merchant", true);
		return new MerchantOffer(new ItemStack(stack.getItem()), new ItemStack(Items.EMERALD, rarity.ordinal() * 10), stack, 1, 100, 1);
	}

}