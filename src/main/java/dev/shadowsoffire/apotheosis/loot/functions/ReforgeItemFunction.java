package dev.shadowsoffire.apotheosis.loot.functions;

import java.util.List;
import java.util.Set;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function which reforges the generated item, provided it is affixable.
 * <p>
 * This function allows specifying a set of rarities that the item may be reforged to, and will roll among them using the appropriate tiered weights.
 */
public class ReforgeItemFunction extends ContextualLootFunction {

    public static final MapCodec<ReforgeItemFunction> CODEC = RecordCodecBuilder.mapCodec(inst -> commonFields(inst)
        .and(
            PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities))
        .apply(inst, ReforgeItemFunction::new));

    private final Set<DynamicHolder<LootRarity>> rarities;

    public ReforgeItemFunction(List<LootItemCondition> predicates, Set<DynamicHolder<LootRarity>> rarities) {
        super(predicates);
        this.rarities = rarities;
    }

    @Override
    public MapCodec<ReforgeItemFunction> codec() {
        return CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx, GenContext gCtx) {
        LootCategory cat = LootCategory.forItem(stack);
        if (!cat.isNone()) {
            LootRarity rarity = LootRarity.randomFromHolders(gCtx, this.rarities);
            ItemStack reforged = LootController.createLootItem(stack, rarity, gCtx);
            return reforged;
        }
        return stack;
    }

}
