package shadows.apotheosis.deadly.reload;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.minecraft.util.random.WeightedRandom;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.AffixLootEntry;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.util.JsonUtil;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class AffixLootManager extends PlaceboJsonReloadListener<AffixLootEntry> {

    public static final Gson GSON = BossArmorManager.GSON;

    public static final AffixLootManager INSTANCE = new AffixLootManager();

    private static final List<AffixLootEntry> ENTRIES = new ArrayList<>();

    private volatile int weight = 0;

    private AffixLootManager() {
        super(DeadlyModule.LOGGER, "affix_loot_entries", false, false);
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT,
                new SerializerBuilder<AffixLootEntry>("AffixLootEntry")
                        .withJsonDeserializer(jo -> {
                            try {
                                var lootItem = GSON.fromJson(jo, AffixLootEntry.class);
                                this.logger.info("Parsed affix loot entry item: {}", lootItem.getId());
                                return lootItem;
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                throw e;
                            }
                        }));
    }

    public static List<AffixLootEntry> getEntries() {
        return ENTRIES;
    }

    @Override
    protected <T extends AffixLootEntry> void register(ResourceLocation id, T item) {
        this.logger.info("Adding affix loot entry item: {}", item.getId());
        if (!this.registry.containsKey(id)) {
            this.registry.put(id, item);
            ENTRIES.add(item);
        } else this.logger.error("Attempted to register a affix loot entry with name {}, but it already exists!", id);
    }

    @Override
    protected void onReload() {
        super.onReload();
        INSTANCE.weight = WeightedRandom.getTotalWeight(ENTRIES);
        if(ENTRIES.size() == 0) this.logger.error("No Affix Loot Entries registered!");
    }

    /**
     * Selects a random loot entry itemstack from the list of entries.
     * @return A loot entry's stack, or a unique, if the rarity selected was ancient.
     */
    public static Optional<AffixLootEntry> getRandomEntry(Random rand) {
        return WeightedRandom.getRandomItem(rand, ENTRIES, INSTANCE.weight);
    }

    /**
     * Selects a random loot entry from the list of entries, filtered by type.
     * @return A loot entry's stack, or a unique, if the rarity selected was ancient.
     */
    public static Optional<AffixLootEntry> getRandomEntry(Random rand, LootCategory lootCategory) {
        if (lootCategory == null) return getRandomEntry(rand);
        return WeightedRandom.getRandomItem(rand, ENTRIES.stream().filter(p -> p.getType() == lootCategory).collect(Collectors.toList()));
    }

}