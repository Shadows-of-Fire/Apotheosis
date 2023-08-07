package dev.shadowsoffire.apotheosis.adventure.loot;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.placebo.reload.WeightedJsonReloadListener;

/**
 * Core loot registry. Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class AffixLootManager extends WeightedJsonReloadListener<AffixLootEntry> {

    public static final AffixLootManager INSTANCE = new AffixLootManager();

    private AffixLootManager() {
        super(AdventureModule.LOGGER, "affix_loot_entries", false, false);
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT, AffixLootEntry.SERIALIZER);
    }

    @Override
    protected void validateItem(AffixLootEntry item) {
        super.validateItem(item);
        Preconditions.checkArgument(!item.stack.isEmpty());
        Preconditions.checkArgument(!item.getType().isNone());
    }

}
