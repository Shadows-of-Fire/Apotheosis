package dev.shadowsoffire.apotheosis.loot;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class GemLootPoolEntry extends ContextualLootPoolEntry {
    public static final MapCodec<GemLootPoolEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        PlaceboCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(a -> a.purities),
        PlaceboCodecs.setOf(GemRegistry.INSTANCE.holderCodec()).optionalFieldOf("gems", Set.of()).forGetter(a -> a.gems))
        .and(LootPoolSingletonContainer.singletonFields(inst))
        .apply(inst, GemLootPoolEntry::new));

    public static final LootPoolEntryType TYPE = new LootPoolEntryType(CODEC);

    private final Set<Purity> purities;
    private final Set<DynamicHolder<Gem>> gems;

    public GemLootPoolEntry(Set<Purity> purities, Set<DynamicHolder<Gem>> gems, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.purities = purities;
        this.gems = gems;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> list, LootContext ctx, GenContext gCtx) {
        Gem gem;

        if (!this.gems.isEmpty()) {
            List<Wrapper<Gem>> resolved = this.gems.stream().map(this::unwrap).filter(Objects::nonNull).map(e -> e.<Gem>wrap(gCtx.tier(), gCtx.luck())).toList();
            gem = WeightedRandom.getRandomItem(ctx.getRandom(), resolved).get().data();
        }
        else {
            gem = GemRegistry.INSTANCE.getRandomItem(gCtx);
        }

        Purity purity = Purity.random(gCtx, this.purities);
        ItemStack stack = gem.toStack(purity);
        list.accept(stack);
    }

    @Override
    public LootPoolEntryType getType() {
        return TYPE;
    }

    /**
     * Unwraps the holder to its object, if present, otherwise returns null and logs an error.
     */
    private Gem unwrap(DynamicHolder<Gem> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("A GemLootPoolEntry failed to resolve the Gem {}!", holder.getId());
            return null;
        }
        return holder.get();
    }

    public static LootPoolSingletonContainer.Builder<?> builder(Set<Purity> purities, Set<DynamicHolder<Gem>> gems) {
        return LootPoolSingletonContainer.simpleBuilder(ctor(purities, gems));
    }

    private static EntryConstructor ctor(Set<Purity> purities, Set<DynamicHolder<Gem>> gems) {
        return (weight, quality, conditions, functions) -> new GemLootPoolEntry(purities, gems, weight, quality, conditions, functions);
    }
}
