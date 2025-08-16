package dev.shadowsoffire.apotheosis.data;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;

/**
 * Holds the identifiers for the default rarities for use in datagen.
 */
public class Rarities {

    public static final DynamicHolder<LootRarity> COMMON = RarityRegistry.INSTANCE.holder(Apotheosis.loc("common"));
    public static final DynamicHolder<LootRarity> UNCOMMON = RarityRegistry.INSTANCE.holder(Apotheosis.loc("uncommon"));
    public static final DynamicHolder<LootRarity> RARE = RarityRegistry.INSTANCE.holder(Apotheosis.loc("rare"));
    public static final DynamicHolder<LootRarity> EPIC = RarityRegistry.INSTANCE.holder(Apotheosis.loc("epic"));
    public static final DynamicHolder<LootRarity> MYTHIC = RarityRegistry.INSTANCE.holder(Apotheosis.loc("mythic"));

}
