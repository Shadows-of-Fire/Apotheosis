package dev.shadowsoffire.apotheosis.adventure.loot;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * Core loot registry. Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class AffixLootRegistry extends WeightedDynamicRegistry<AffixLootEntry> {

    public static final AffixLootRegistry INSTANCE = new AffixLootRegistry();

    private AffixLootRegistry() {
        super(AdventureModule.LOGGER, "affix_loot_entries", false, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("affix_loot_entry"), AffixLootEntry.CODEC);
    }

    @Override
    protected void validateItem(ResourceLocation key, AffixLootEntry item) {
        super.validateItem(key, item);
        Preconditions.checkArgument(!item.stack.isEmpty(), "Empty itemstacks are not permitted.");
        Preconditions.checkArgument(!item.getType().isNone(), "Items without a valid loot category are not permitted.");
        Preconditions.checkArgument(item.getMinRarity().ordinal() <= item.getMaxRarity().ordinal(), "The minimum rarity must be lower or equal to the max rarity.");
    }

}
