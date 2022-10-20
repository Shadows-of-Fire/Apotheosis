package shadows.apotheosis.adventure.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import shadows.apotheosis.Apoth.Affixes;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootRarity.LootRule;

public class LootController {

	/**
	 * @see {@link LootController#createLootItem(ItemStack, LootCategory, LootRarity, Random)}
	 */
	public static ItemStack createLootItem(ItemStack stack, LootRarity rarity, RandomSource rand) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == LootCategory.NONE) return stack;
		return createLootItem(stack, cat, rarity, rand);
	}

	static Random jRand = new Random();

	/**
	 * Modifies an ItemStack with affixes of the target category and rarity.
	 * @param stack The ItemStack.
	 * @param cat The LootCategory.  Should be valid for the item being passed.
	 * @param rarity The target Rarity.
	 * @param rand The Random
	 * @return The modifed ItemStack (note the original is not preserved, but the stack is returned for simplicity).
	 */
	public static ItemStack createLootItem(ItemStack stack, LootCategory cat, LootRarity rarity, RandomSource rand) {
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
			loaded.put(Affixes.SOCKET.get(), new AffixInstance(Affixes.SOCKET.get(), stack, rarity, sockets.intValue()));
		}

		if (durability > 0) {
			loaded.put(Affixes.DURABLE.get(), new AffixInstance(Affixes.DURABLE.get(), stack, rarity, durability + AffixHelper.step(-0.07F, 14, 0.01F).get(rand.nextFloat())));
		}

		jRand.setSeed(rand.nextLong());
		Collections.shuffle(nameList, jRand);
		MutableComponent name = Component.translatable(nameList.size() > 1 ? "%s %s %s" : "%s %s", nameList.get(0).getName(true), "", nameList.size() > 1 ? nameList.get(1).getName(false) : "").withStyle(Style.EMPTY.withColor(rarity.color()));

		AffixHelper.setRarity(stack, rarity);
		AffixHelper.setAffixes(stack, loaded);
		AffixHelper.setName(stack, name);

		return stack;
	}

	/**
	 * Pulls a random LootRarity and AffixLootEntry, and generates an Affix Item
	 * @param rand Random
	 * @param rarity The rarity, or null if it should be randomly selected.
	 * @param luck The player's luck level
	 * @param level The world, since affix loot entries are per-dimension.
	 * @return An affix item, or an empty ItemStack if no entries were available for the dimension.
	 */
	public static ItemStack createRandomLootItem(RandomSource rand, @Nullable LootRarity rarity, float luck, ServerLevelAccessor level) {
		AffixLootEntry entry = AffixLootManager.INSTANCE.getRandomItem(rand, luck, level);
		if (entry == null) return ItemStack.EMPTY;
		if (rarity == null) rarity = LootRarity.random(rand, luck, entry);
		return createLootItem(entry.getStack(), entry.getType(), rarity, rand);
	}

}