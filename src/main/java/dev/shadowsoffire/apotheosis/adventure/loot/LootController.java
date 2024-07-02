package dev.shadowsoffire.apotheosis.adventure.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import dev.shadowsoffire.apotheosis.Apoth.Affixes;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity.LootRule;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;

public class LootController {

    /**
     * @see {@link LootController#createLootItem(ItemStack, LootCategory, LootRarity, Random)}
     */
    public static ItemStack createLootItem(ItemStack stack, LootRarity rarity, RandomSource rand) {
        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone()) return stack;
        return createLootItem(stack, cat, rarity, rand);
    }

    static Random jRand = new Random();

    /**
     * Modifies an ItemStack with affixes of the target category and rarity.
     *
     * @param stack  The ItemStack.
     * @param cat    The LootCategory. Should be valid for the item being passed.
     * @param rarity The target Rarity.
     * @param rand   The Random
     * @return The modifed ItemStack (note the original is not preserved, but the stack is returned for simplicity).
     */
    public static ItemStack createLootItem(ItemStack stack, LootCategory cat, LootRarity rarity, RandomSource rand) {
        Set<DynamicHolder<? extends Affix>> selected = new LinkedHashSet<>();
        MutableInt sockets = new MutableInt(0);
        float durability = 0;
        for (LootRule rule : rarity.getRules()) {
            if (rule.type() == AffixType.DURABILITY) durability = rule.chance();
            else rule.execute(stack, rarity, selected, sockets, rand);
        }

        // Prevent number of sockets from decreasing during a Reforge.
        sockets.setValue(Math.max(sockets.getValue(), SocketHelper.getSockets(stack)));

        Map<DynamicHolder<? extends Affix>, AffixInstance> loaded = new HashMap<>();
        List<AffixInstance> nameList = new ArrayList<>(selected.size());
        for (DynamicHolder<? extends Affix> a : selected) {
            AffixInstance inst = new AffixInstance(a, stack, RarityRegistry.INSTANCE.holder(rarity), rand.nextFloat());
            loaded.put(a, inst);
            nameList.add(inst);
        }
        if (nameList.size() == 0) {
            throw new RuntimeException(String.format("Failed to locate any affixes for %s{%s} with category %s and rarity %s.", stack.getItem(), stack.getTag(), cat, rarity));
        }

        // Socket and Durability handling, which is non-standard.
        if (sockets.intValue() > 0) {
            SocketHelper.setSockets(stack, sockets.intValue());
        }

        if (durability > 0) {
            loaded.put(Affixes.DURABLE, new AffixInstance(Affixes.DURABLE, stack, RarityRegistry.INSTANCE.holder(rarity), durability + AffixHelper.step(-0.07F, 14, 0.01F).get(rand.nextFloat())));
        }

        jRand.setSeed(rand.nextLong());
        Collections.shuffle(nameList, jRand);
        String key = nameList.size() > 1 ? "misc.apotheosis.affix_name.three" : "misc.apotheosis.affix_name.two";
        MutableComponent name = Component.translatable(key, nameList.get(0).getName(true), "", nameList.size() > 1 ? nameList.get(1).getName(false) : "").withStyle(Style.EMPTY.withColor(rarity.getColor()));

        AffixHelper.setRarity(stack, rarity);
        AffixHelper.setAffixes(stack, loaded);
        AffixHelper.setName(stack, name);

        return stack;
    }

    /**
     * Pulls a random LootRarity and AffixLootEntry, and generates an Affix Item
     *
     * @param rand   Random
     * @param rarity The rarity, or null if it should be randomly selected.
     * @param luck   The player's luck level
     * @param level  The world, since affix loot entries are per-dimension.
     * @return An affix item, or an empty ItemStack if no entries were available for the dimension.
     */
    public static ItemStack createRandomLootItem(RandomSource rand, @Nullable LootRarity rarity, Player player, ServerLevelAccessor level) {
        AffixLootEntry entry = AffixLootRegistry.INSTANCE.getRandomItem(rand, player.getLuck(), IDimensional.matches(level.getLevel()), IStaged.matches(player));
        if (entry == null) return ItemStack.EMPTY;
        if (rarity == null) rarity = LootRarity.random(rand, player.getLuck(), entry);
        return createLootItem(entry.getStack(), entry.getType(), rarity, rand);
    }

    /**
     * Returns the pool of available affixes for an item, given the existing affixes present.
     *
     * @param stack          The item stack the affixes may be applied to
     * @param rarity         The rarity of the item stack
     * @param currentAffixes The current affixes that are (or will be) applied to the item.
     * @param type           The type of affix to target
     * @return A list of available affixes for the item. May be empty.
     */
    public static List<DynamicHolder<? extends Affix>> getAvailableAffixes(ItemStack stack, LootRarity rarity, Set<DynamicHolder<? extends Affix>> currentAffixes, AffixType type) {
        LootCategory cat = LootCategory.forItem(stack);
        return AffixHelper.byType(type)
            .stream()
            .filter(a -> a.get().canApplyTo(stack, cat, rarity))
            .filter(a -> !currentAffixes.contains(a))
            .collect(Collectors.toList());
    }

}
