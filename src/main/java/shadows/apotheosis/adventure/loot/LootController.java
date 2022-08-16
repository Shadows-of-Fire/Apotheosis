package shadows.apotheosis.adventure.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth.Affixes;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootRarity.LootRule;

public class LootController {

	public static ItemStack createLootItem(ItemStack stack, LootRarity rarity, Random rand) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) return stack;
		return createLootItem(stack, cat, rarity, rand);
	}

	public static ItemStack createLootItem(ItemStack stack, LootCategory cat, LootRarity rarity, Random rand) {
		Set<Affix> selected = new HashSet<>();
		MutableInt sockets = new MutableInt(0);
		float durability = 0;
		for (LootRule rule : rarity.rules()) {
			if (rule.type() == AffixType.DURABILITY) durability = rule.chance();
			else rule.execute(stack, rarity, selected, sockets, rand);
		}

		Map<Affix, AffixInstance> loaded = new HashMap<>();
		List<AffixInstance> nameList = new ArrayList<>(selected.size());
		for (Affix a : selected) {
			AffixInstance inst = new AffixInstance(a, stack, rarity, rand.nextFloat());
			loaded.put(a, inst);
			nameList.add(inst);
		}

		// Socket and Durability handling, which is non-standard.
		if (sockets.intValue() > 0) {
			loaded.put(Affixes.SOCKET, new AffixInstance(Affixes.SOCKET, stack, rarity, sockets.intValue()));
		}

		if (durability > 0) {
			loaded.put(Affixes.DURABLE, new AffixInstance(Affixes.DURABLE, stack, rarity, durability));
		}

		Collections.shuffle(nameList);
		TranslatableComponent name = (TranslatableComponent) new TranslatableComponent(nameList.size() > 1 ? "%s %s %s" : "%s %s", "", "", "").withStyle(Style.EMPTY.withColor(rarity.color()));
		name.getArgs()[0] = nameList.get(0).getName(true);
		if (nameList.size() > 1) name.getArgs()[2] = nameList.get(1).getName(false);

		AffixHelper.setRarity(stack, rarity);
		AffixHelper.setAffixes(stack, loaded);
		AffixHelper.setName(stack, name);

		return stack;
	}

	public static ItemStack createRandomLootItem(Random rand, int rarityOffset, float luck) {
		LootRarity rarity = LootRarity.random(rand, rarityOffset);
		AffixLootEntry entry = AffixLootManager.getRandomEntry(rand, luck);
		return createLootItem(entry.getStack(), entry.getType(), rarity, rand);
	}

}