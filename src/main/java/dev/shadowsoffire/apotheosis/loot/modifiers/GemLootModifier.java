package dev.shadowsoffire.apotheosis.loot.modifiers;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class GemLootModifier extends ContextualLootModifier {

    public static final MapCodec<GemLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
        .and(GemTableEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries))
        .apply(inst, GemLootModifier::new));

    protected final List<GemTableEntry> entries;

    public GemLootModifier(LootItemCondition[] conditions, List<GemTableEntry> entries) {
        super(conditions);
        this.entries = entries;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx, GenContext gCtx) {
        for (GemTableEntry entry : this.entries) {
            if (entry.pattern.matches(ctx.getQueriedLootTableId())) {
                if (ctx.getRandom().nextFloat() <= entry.chance()) {
                    Purity purity = Purity.random(gCtx, entry.purities);

                    Gem gem;
                    if (!entry.gems.isEmpty()) {
                        List<Wrapper<Gem>> resolved = entry.gems.stream().map(this::unwrap).filter(Objects::nonNull).map(e -> e.<Gem>wrap(gCtx.tier(), gCtx.luck())).toList();
                        gem = WeightedRandom.getRandomItem(ctx.getRandom(), resolved).get().data();
                    }
                    else {
                        gem = GemRegistry.INSTANCE.getRandomItem(gCtx);
                    }

                    if (gem == null) {
                        Apotheosis.LOGGER.error("A GemLootModifier (entry {}) failed to resolve a gem for table {}!", entry.toString(), ctx.getQueriedLootTableId());
                        continue;
                    }

                    generatedLoot.add(gem.toStack(purity));
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
    private Gem unwrap(DynamicHolder<Gem> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("A GemLootModifier failed to resolve the Gem {}!", holder.getId());
            return null;
        }
        return holder.get();
    }

    /**
     * Represents a single table + application chance + optional sets of purities and gems for the {@link GemLootModifier}.
     *
     * @param pattern  The loot pattern matcher that determines which tables this entry applies to.
     * @param chance   The chance of this entry applying, when a table is matched.
     * @param gems     A pool of potential gems; if empty, all gems may be queried.
     * @param purities A pool of potential purities; if empty, all purities may be queried.
     */
    public static record GemTableEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<Gem>> gems, Set<Purity> purities) {

        public static final Codec<GemTableEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(GemTableEntry::pattern),
            Codec.floatRange(0, 1).fieldOf("chance").forGetter(GemTableEntry::chance),
            PlaceboCodecs.setOf(GemRegistry.INSTANCE.holderCodec()).optionalFieldOf("gems", Set.of()).forGetter(GemTableEntry::gems),
            PlaceboCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(GemTableEntry::purities))
            .apply(inst, GemTableEntry::new));

    }

}
