package dev.shadowsoffire.apotheosis.loot.modifiers;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class AffixLootModifier extends ContextualLootModifier {

    public static final MapCodec<AffixLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
        .and(AffixTableEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries))
        .apply(inst, AffixLootModifier::new));

    protected final List<AffixTableEntry> entries;

    public AffixLootModifier(LootItemCondition[] conditions, int priority, List<AffixTableEntry> entries) {
        super(conditions, priority);
        this.entries = entries;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx, GenContext gCtx) {
        for (AffixTableEntry entry : this.entries) {
            if (entry.pattern.matches(ctx.getQueriedLootTableId())) {
                if (ctx.getRandom().nextFloat() <= entry.chance()) {

                    AffixLootEntry lootEntry;
                    if (!entry.entries.isEmpty()) {
                        List<Weighted<AffixLootEntry>> resolved = entry.entries.stream().mapMulti(TieredWeights.wrapFilterHolders(gCtx)).toList();
                        lootEntry = WeightedRandom.getRandomItem(ctx.getRandom(), resolved, Weighted::weight).get().value();
                    }
                    else {
                        lootEntry = AffixLootRegistry.INSTANCE.getRandomItem(gCtx);
                    }

                    // Resolve the rarity for the entry, preferring a rarity from this loot modifier if available, but using the loot entry otherwise.
                    LootRarity rarity;
                    if (!entry.rarities.isEmpty()) {
                        rarity = LootRarity.randomFromHolders(gCtx, entry.rarities);
                    }
                    else {
                        rarity = LootRarity.random(gCtx, lootEntry.rarities());
                    }

                    ItemStack affixItem = LootController.createLootItem(lootEntry.stack(), rarity, gCtx);
                    if (!affixItem.isEmpty()) {
                        NameHelper.setItemName(ctx.getRandom(), affixItem);
                        affixItem.set(Components.FROM_CHEST, true);
                        generatedLoot.add(affixItem);
                    }
                }
                break;
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    /**
     * Unwraps the holder to its object, if present, otherwise returns null and logs an error.
     */
    @Nullable
    private AffixLootEntry unwrap(DynamicHolder<AffixLootEntry> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("An AffixLootModifier failed to resolve the AffixLootEntry {}!", holder.getId());
            return null;
        }
        return holder.get();
    }

    /**
     * Represents a single table + application chance + an optional set of rarities for the {@link AffixConvertLootModifier}.
     *
     * @param pattern  The loot pattern matcher that determines which tables this entry applies to.
     * @param chance   The chance of this entry applying, when a table is matched.
     * @param entries  A pool of potential entries; if empty, all entries may be queried.
     * @param rarities A pool of potential rarities; if empty, all rarities may be queried.
     */
    public static record AffixTableEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<AffixLootEntry>> entries, Set<DynamicHolder<LootRarity>> rarities) {

        public static final Codec<AffixTableEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(AffixTableEntry::pattern),
            Codec.floatRange(0, 1).fieldOf("chance").forGetter(AffixTableEntry::chance),
            PlaceboCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(AffixTableEntry::entries),
            PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(AffixTableEntry::rarities))
            .apply(inst, AffixTableEntry::new));

    }
}
