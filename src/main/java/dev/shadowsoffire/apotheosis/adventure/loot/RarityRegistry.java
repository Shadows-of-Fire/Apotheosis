package dev.shadowsoffire.apotheosis.adventure.loot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity.LootRule;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Handles loading the configurable portion of rarities.
 */
public class RarityRegistry extends WeightedDynamicRegistry<LootRarity> {

    public static final RarityRegistry INSTANCE = new RarityRegistry();

    /**
     * Sorted list of all rarities.
     */
    protected List<DynamicHolder<LootRarity>> ordered = new ArrayList<>();

    protected BiMap<Item, DynamicHolder<LootRarity>> materialMap = HashBiMap.create();

    private RarityRegistry() {
        super(AdventureModule.LOGGER, "rarities", true, false);
    }

    /**
     * Checks if a given item is a rarity material.
     *
     * @param stack The item being checked.
     * @return True if the item is a rarity material.
     */
    public static boolean isMaterial(Item item) {
        return getMaterialRarity(item).isBound();
    }

    /**
     * Returns the rarity associated with the passed rarity material.
     * <p>
     * May be unbound.
     */
    public static DynamicHolder<LootRarity> getMaterialRarity(Item item) {
        return INSTANCE.materialMap.getOrDefault(item, INSTANCE.emptyHolder());
    }

    /**
     * Returns the minimum rarity based on the ordinals.
     * <p>
     * Guaranted to be {@linkplain DynamicHolder#isBound() bound}.
     */
    public static DynamicHolder<LootRarity> getMinRarity() {
        return INSTANCE.ordered.get(0);
    }

    /**
     * Returns the maximum rarity based on the ordinals.
     * <p>
     * Guaranted to be {@linkplain DynamicHolder#isBound() bound}.
     */
    public static DynamicHolder<LootRarity> getMaxRarity() {
        return INSTANCE.ordered.get(INSTANCE.ordered.size() - 1);
    }

    /**
     * Returns the rarity for a particular ordinal.
     * <p>
     * Guaranted to be {@linkplain DynamicHolder#isBound() bound}.
     *
     * @throws IndexOutOfBoundsException if the ordinal is invalid.
     */
    public static DynamicHolder<LootRarity> byOrdinal(int i) {
        return INSTANCE.ordered.get(i);
    }

    public static ResourceLocation convertId(String s) {
        return s.contains(":") ? new ResourceLocation(s) : Apotheosis.loc(s);
    }

    public static DynamicHolder<LootRarity> byLegacyId(String s) {
        return INSTANCE.holder(convertId(s));
    }

    public static DynamicHolder<LootRarity> prev(DynamicHolder<LootRarity> rarity) {
        if (rarity == RarityRegistry.getMinRarity()) return rarity;
        return RarityRegistry.byOrdinal(rarity.get().ordinal() - 1);
    }

    public static DynamicHolder<LootRarity> next(DynamicHolder<LootRarity> rarity) {
        if (rarity == RarityRegistry.getMaxRarity()) return rarity;
        return RarityRegistry.byOrdinal(rarity.get().ordinal() + 1);
    }

    public List<DynamicHolder<LootRarity>> getOrderedRarities() {
        return this.ordered;
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.ordered = new ArrayList<>();
        this.materialMap = HashBiMap.create();
    }

    @Override
    protected void onReload() {
        super.onReload();
        this.ordered = this.registry.values().stream().sorted(Comparator.comparing(LootRarity::ordinal)).map(this::holder).toList();

        int lastOrdinal = -1;
        for (DynamicHolder<LootRarity> r : this.ordered) {
            if (r.get().ordinal() != lastOrdinal + 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("Rarity ordinal order is inconsistent. The ordinals must start at zero and be continuous up to the max value.\n");
                for (var rarity : this.ordered) {
                    sb.append(rarity.getId() + " | " + rarity.get().ordinal() + "\n");
                }
                throw new RuntimeException(sb.toString());
            }
            lastOrdinal = r.get().ordinal();
        }

        for (DynamicHolder<LootRarity> r : this.ordered) {
            DynamicHolder<LootRarity> old = this.materialMap.put(r.get().getMaterial(), r);
            if (old != null) {
                throw new RuntimeException("Two rarities may not share the same rarity material: " + r.getId() + " conflicts with " + old.getId());
            }
        }
        this.ordered = ImmutableList.copyOf(this.ordered);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("rarity"), LootRarity.LOAD_CODEC);
    }

    @Override
    protected void validateItem(ResourceLocation key, LootRarity item) {
        super.validateItem(key, item);
        Preconditions.checkNotNull(item.getColor());
        Preconditions.checkArgument(item.getMaterial() != null && item.getMaterial() != Items.AIR);
        Preconditions.checkArgument(item.getWeight() >= 0, "A rarity may not have negative weight!");
        Preconditions.checkArgument(item.getQuality() >= 0, "A rarity may not have negative quality!");
        Preconditions.checkArgument(!item.getRules().isEmpty(), "A rarity may not have no rules!");
    }

    public void validateLootRules() {
        for (LootRarity rarity : this.registry.values()) {
            Map<AffixType, List<LootRule>> sorted = new HashMap<>();
            rarity.getRules().stream().filter(r -> r.type().needsValidation()).forEach(rule -> {
                sorted.computeIfAbsent(rule.type(), r -> new ArrayList<>());
                sorted.get(rule.type()).add(rule);
            });
            sorted.forEach((type, rules) -> {
                for (LootCategory cat : LootCategory.VALUES) {
                    if (cat.isNone()) continue;
                    List<Affix> affixes = AffixRegistry.INSTANCE.getValues().stream().filter(a -> a.canApplyTo(ItemStack.EMPTY, cat, rarity) && a.getType() == type).toList();

                    if (affixes.size() < rules.size()) {
                        var errMsg = new StringBuilder();
                        errMsg.append("Insufficient number of affixes to satisfy the loot rules (ignoring backup rules) of rarity " + this.getKey(rarity) + " for category " + cat.getName());
                        errMsg.append("Required: " + rules.size());
                        errMsg.append("; Provided: " + affixes.size());
                        // errMsg.append("The following affixes exist for this category/rarity combination: ");
                        // affixes.forEach(a -> errMsg.append(a.getId() + " "));
                        AdventureModule.LOGGER.error(errMsg.toString());
                    }
                }
            });
        }
    }

}
