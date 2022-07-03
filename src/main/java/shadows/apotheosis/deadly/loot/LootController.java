package shadows.apotheosis.deadly.loot;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;

public class LootController {

	public static ItemStack createLootItem(ItemStack stack, LootRarity rarity, Random rand) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) return stack;
		List<Affix> affixes = AffixHelper.getAffixesFor(cat, rarity);
		Collections.shuffle(affixes, rand);

		AffixHelper.applyAffix(stack, affixes.get(0), rand.nextFloat());
		Component name = stack.getItem().getName(stack);
		name = affixes.get(0).chainName(name, false);
		if (rand.nextFloat() < rand.nextFloat()) {
			AffixHelper.applyAffix(stack, affixes.get(1), rand.nextFloat());
			name = affixes.get(1).chainName(name, true);
		}

		AffixHelper.setRarity(stack, rarity);
		stack.setHoverName(name);

		return stack;
	}

}