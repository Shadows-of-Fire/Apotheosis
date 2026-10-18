package dev.shadowsoffire.apotheosis.loot;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent.UpdateCause;

/**
 * Core loot registry. Handles the management of all Affixes, LootEntries, and generation of loot items.
 * <p>
 * Note that entries are intentionally <i>not</i> validated to have a valid {@link LootCategory} at registration time
 * (see {@link AffixLootEntry#getType()}). An item's category may be supplied by a NeoForge Data Map (e.g. a
 * datapack-defined {@code loot_category_overrides} entry), and Data Maps are only written into their owning registry
 * after every reload listener - including this one - has already finished its own {@code apply()} pass. Rejecting
 * entries at that point would incorrectly discard entries whose category is only defined via a data map, even though
 * they resolve correctly once loot generation actually runs. See {@link #registerDeferredValidation()} for a
 * best-effort, non-fatal check that runs once Data Maps are actually ready.
 */
public class AffixLootRegistry extends TieredDynamicRegistry<AffixLootEntry> {

    public static final AffixLootRegistry INSTANCE = new AffixLootRegistry();

    private AffixLootRegistry() {
        super(Apotheosis.LOGGER, "affix_loot_entries", false, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("affix_loot_entry"), AffixLootEntry.CODEC);
    }

    @Override
    @Nullable
    public AffixLootEntry getRandomItem(GenContext ctx) {
        return this.getRandomItem(ctx, Constraints.eval(ctx));
    }

    /**
     * Registers a listener that logs any registered entry whose {@link LootCategory} is still {@link LootCategory#isNone()}
     * once NeoForge has finished applying Data Maps for the item registry. The backing registry is immutable by this
     * point, so offending entries cannot be removed; this only surfaces genuinely broken entries (e.g. typos) instead
     * of leaving them silently inert.
     */
    public static void registerDeferredValidation() {
        NeoForge.EVENT_BUS.addListener(AffixLootRegistry::onDataMapsUpdated);
    }

    private static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        if (event.getCause() != UpdateCause.SERVER_RELOAD) return;
        event.ifRegistry(Registries.ITEM, reg -> {
            for (AffixLootEntry entry : INSTANCE.getValues()) {
                if (entry.getType().isNone()) {
                    Apotheosis.LOGGER.error("The affix_loot_entries entry for item {} resolves to no valid loot category; it will never be selected.",
                        entry.stack().getItemHolder().getKey().location());
                }
            }
        });
    }

}

