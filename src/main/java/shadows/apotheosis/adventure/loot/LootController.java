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

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.loot.LootRarity.LootRule;

public class LootController {

	public static ItemStack createLootItem(ItemStack stack, LootRarity rarity, Random rand) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) return stack;
		Set<Affix> selected = new HashSet<>();
		MutableInt sockets = new MutableInt(0);
		for (LootRule rule : rarity.rules()) {
			rule.execute(stack, rarity, selected, sockets, rand);
		}

		Map<Affix, AffixInstance> loaded = new HashMap<>();
		List<AffixInstance> nameList = new ArrayList<>(selected.size());
		for (Affix a : selected) {
			AffixInstance inst = new AffixInstance(a, stack, rarity, rand.nextFloat());
			loaded.put(a, inst);
			nameList.add(inst);
		}

		if (sockets.intValue() > 0) {
			AffixInstance inst = new AffixInstance(Apoth.Affixes.SOCKET, stack, rarity, sockets.intValue());
			loaded.put(Apoth.Affixes.SOCKET, inst);
		}

		Collections.shuffle(nameList);
		Component name = stack.getItem().getName(stack);
		name = nameList.get(0).chainName(name, false);
		if (nameList.size() > 1) name = nameList.get(1).chainName(name, true);

		AffixHelper.setRarity(stack, rarity);
		AffixHelper.setAffixes(stack, loaded);
		stack.setHoverName(name);

		return stack;
	}

}