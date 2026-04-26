package dev.shadowsoffire.apotheosis.affix.trades;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A {@link LootItemConditionalFunction} used as a {@code given_item_modifier} on a {@code minecraft:villager_trade}
 * to dynamically generate an affix-bearing ItemStack scaled to the trading player's world tier.
 * <p>
 * The function replaces the trade's base {@code gives} stack with a freshly-rolled affix item, and sets
 * {@link DataComponents#ADDITIONAL_TRADE_COST} so the trade's emerald cost scales with the player's {@code WorldTier}.
 */
public class AutomaticAffixTrade extends LootItemConditionalFunction {

    public static final MapCodec<AutomaticAffixTrade> CODEC = RecordCodecBuilder.mapCodec(inst -> commonFields(inst)
        .and(inst.group(
            PlaceboCodecs.setOf(LootRarity.CODEC).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
            AffixLootRegistry.INSTANCE.holderCodec().listOf().optionalFieldOf("entries", List.of()).forGetter(a -> a.entries)))
        .apply(inst, AutomaticAffixTrade::new));

    /**
     * Rarity limitations. These are used in place of the rarities on the affix loot entry if supplied.
     * <p>
     * May be omitted, in which case the entries' rarities will be used.
     */
    protected final Set<LootRarity> rarities;

    /**
     * A list of entries that this trade may pull from.
     * <p>
     * May be omitted, in which case all available entries will be used.
     */
    protected final List<DynamicHolder<AffixLootEntry>> entries;

    public AutomaticAffixTrade(List<LootItemCondition> predicates, Set<LootRarity> rarities, List<DynamicHolder<AffixLootEntry>> entries) {
        super(predicates);
        this.rarities = rarities;
        this.entries = entries;
    }

    @Override
    public MapCodec<AutomaticAffixTrade> codec() {
        return CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        Entity trader = ctx.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (trader == null || trader.level().isClientSide()) {
            return stack;
        }
        Player player = trader.level().getNearestPlayer(trader, -1);
        if (player == null) {
            return stack;
        }
        GenContext gCtx = GenContext.forPlayer(ctx.getRandom(), player);

        ItemStack affixItem;
        if (this.entries.isEmpty()) {
            LootRarity rarity = LootRarity.random(gCtx, this.rarities);
            affixItem = LootController.createRandomLootItem(gCtx, rarity);
        }
        else {
            List<Weighted<AffixLootEntry>> resolved = this.entries.stream()
                .map(this::unwrap)
                .filter(Objects::nonNull)
                .mapMulti(TieredWeights.wrapFilter(gCtx))
                .toList();
            if (resolved.isEmpty()) {
                return ItemStack.EMPTY;
            }
            Optional<Weighted<AffixLootEntry>> picked = WeightedRandom.getRandomItem(ctx.getRandom(), resolved, Weighted::weight);
            if (picked.isEmpty()) {
                return ItemStack.EMPTY;
            }
            AffixLootEntry entry = picked.get().value();
            LootRarity rarity = LootRarity.random(gCtx, this.rarities.isEmpty() ? entry.rarities() : this.rarities);
            affixItem = LootController.createLootItem(entry.stackTemplate().create(), rarity, gCtx);
        }

        if (affixItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        NameHelper.setItemName(ctx.getRandom(), affixItem);
        affixItem.set(Components.FROM_TRADER, true);
        int scaledCost = gCtx.tier().ordinal() * 7;
        if (scaledCost > 0) {
            affixItem.set(DataComponents.ADDITIONAL_TRADE_COST, scaledCost);
        }
        return affixItem;
    }

    @Nullable
    private AffixLootEntry unwrap(DynamicHolder<AffixLootEntry> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("An AutomaticAffixTrade failed to resolve the Affix Loot Entry {}!", holder.getId());
            return null;
        }
        return holder.get();
    }

}
