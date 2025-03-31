package dev.shadowsoffire.apotheosis.loot.modifiers;

import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class AffixConvertLootModifier extends ContextualLootModifier {

    public static final MapCodec<AffixConvertLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
        .and(AffixConversionEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries))
        .apply(inst, AffixConvertLootModifier::new));

    protected final List<AffixConversionEntry> entries;

    public AffixConvertLootModifier(LootItemCondition[] conditions, List<AffixConversionEntry> entries) {
        super(conditions);
        this.entries = entries;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context, GenContext gCtx) {
        for (AffixConversionEntry entry : this.entries) {
            if (entry.pattern.matches(context.getQueriedLootTableId())) {
                RandomSource rand = context.getRandom();
                if (entry.chance() <= 0) {
                    break; // Entries with a chance of 0 are used to skip certain loot tables that would match other patterns.
                }

                for (ItemStack s : generatedLoot) {
                    if (!LootCategory.forItem(s).isNone() && AffixHelper.getAffixes(s).isEmpty() && rand.nextFloat() <= entry.chance()) {
                        // This modifies the stack in-place, so we don't need to re-set it into the list.
                        LootRarity rarity = LootRarity.randomFromHolders(gCtx, entry.rarities);
                        LootController.createLootItem(s, rarity, gCtx);
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
     * Represents a single table + application chance + an optional set of rarities for the {@link AffixConvertLootModifier}.
     *
     * @param pattern  The loot pattern matcher that determines which tables this entry applies to.
     * @param chance   The chance of this entry applying, when a table is matched.
     * @param rarities A pool of potential rarities; if empty, all rarities may be queried.
     */
    public static record AffixConversionEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<LootRarity>> rarities) {

        public static final Codec<AffixConversionEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(AffixConversionEntry::pattern),
            Codec.floatRange(0, 1).fieldOf("chance").forGetter(AffixConversionEntry::chance),
            PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(AffixConversionEntry::rarities))
            .apply(inst, AffixConversionEntry::new));

    }

}
