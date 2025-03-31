package dev.shadowsoffire.apotheosis.affix.trades;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.systems.wanderer.WandererTrade;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public class AffixTrade implements WandererTrade {

    public static Codec<AffixTrade> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            ItemCost.CODEC.fieldOf("input_1").forGetter(trade -> trade.price),
            ItemCost.CODEC.optionalFieldOf("input_2").forGetter(trade -> trade.price2),
            PlaceboCodecs.setOf(LootRarity.CODEC).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
            AffixLootRegistry.INSTANCE.holderCodec().listOf().fieldOf("entries").forGetter(a -> a.entries),
            Codec.BOOL.optionalFieldOf("rare", false).forGetter(trade -> trade.rare))
        .apply(inst, AffixTrade::new));

    /**
     * Input items
     */
    protected final ItemCost price;
    protected final Optional<ItemCost> price2;

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

    /**
     * If this trade is part of the "rare" trade list or not.
     */
    protected final boolean rare;

    public AffixTrade(ItemCost price, Optional<ItemCost> price2, Set<LootRarity> rarities, List<DynamicHolder<AffixLootEntry>> entries, boolean rare) {
        this.price = price;
        this.price2 = price2;
        this.rarities = rarities;
        this.entries = entries;
        this.rare = rare;
    }

    @Override
    @Nullable
    public MerchantOffer getOffer(Entity trader, RandomSource rand) {
        if (trader.level().isClientSide) {
            return null;
        }
        Player player = trader.level().getNearestPlayer(trader, -1);
        if (player == null) {
            return null;
        }
        GenContext ctx = GenContext.forPlayer(rand, player);

        ItemStack affixItem;
        if (this.entries.isEmpty()) {
            LootRarity selectedRarity = LootRarity.random(ctx, this.rarities);
            affixItem = LootController.createRandomLootItem(ctx, selectedRarity);
        }
        else {
            List<Wrapper<AffixLootEntry>> resolved = this.entries.stream().map(this::unwrap).filter(Objects::nonNull).map(e -> e.<AffixLootEntry>wrap(ctx.tier(), ctx.luck())).toList();
            AffixLootEntry entry = WeightedRandom.getRandomItem(rand, resolved).get().data();
            LootRarity selectedRarity = LootRarity.random(ctx, this.rarities.isEmpty() ? entry.rarities() : this.rarities);
            affixItem = LootController.createLootItem(entry.stack(), selectedRarity, ctx);
        }

        if (affixItem.isEmpty()) {
            return null;
        }
        affixItem.set(Components.FROM_TRADER, true);
        return new MerchantOffer(this.price, this.price2, affixItem, 1, 100, 1);
    }

    @Override
    public boolean isRare() {
        return this.rare;
    }

    @Override
    public Codec<? extends WandererTrade> getCodec() {
        return CODEC;
    }

    /**
     * Unwraps the holder to its object, if present, otherwise returns null and logs an error.
     */
    private AffixLootEntry unwrap(DynamicHolder<AffixLootEntry> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("An AffixTrade failed to resolve the Affix Loot Entry {}!", holder.getId());
            return null;
        }
        return holder.get();
    }

}
