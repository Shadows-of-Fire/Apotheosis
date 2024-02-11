package dev.shadowsoffire.apotheosis.adventure.affix.trades;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import dev.shadowsoffire.apotheosis.village.wanderer.JsonTrade;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ServerLevelAccessor;

public class AffixTrade implements JsonTrade {

    public static final Codec<AffixTrade> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(inst -> inst
        .group(
            ItemAdapter.CODEC.fieldOf("input_1").forGetter(a -> a.price),
            PlaceboCodecs.nullableField(ItemAdapter.CODEC, "input_2", ItemStack.EMPTY).forGetter(a -> a.price2),
            PlaceboCodecs.nullableField(RarityClamp.Simple.CODEC, "rarities", RarityClamp.UNCLAMPED).forGetter(a -> a.rarities),
            AffixLootRegistry.INSTANCE.holderCodec().listOf().fieldOf("entries").forGetter(a -> a.entries),
            PlaceboCodecs.nullableField(Codec.BOOL, "rare", false).forGetter(a -> a.rare))
        .apply(inst, AffixTrade::new)));

    /**
     * Input items
     */
    protected final ItemStack price, price2;

    /**
     * Rarity limitations. These are used in place of the rarities on the affix loot entry if supplied.<br>
     * May be omitted, in which case the entries' rarities will be used.
     */
    protected final RarityClamp.Simple rarities;

    /**
     * A list of entries that this trade may pull from.<br>
     * May be omitted, in which case all entries from the dimension the trader is in will be used.
     */
    protected final List<DynamicHolder<AffixLootEntry>> entries;

    /**
     * If this trade is part of the "rare" trade list or not.
     */
    protected final boolean rare;

    public AffixTrade(ItemStack price, ItemStack price2, RarityClamp.Simple rarities, List<DynamicHolder<AffixLootEntry>> entries, boolean rare) {
        this.price = price;
        this.price2 = price2;
        this.rarities = rarities;
        this.entries = entries;
        this.rare = rare;
    }

    @Override
    @Nullable
    public MerchantOffer getOffer(Entity trader, RandomSource rand) {
        if (trader.level().isClientSide) return null;
        Player player = trader.level().getNearestPlayer(trader, -1);
        if (player == null) return null;

        ItemStack affixItem;
        if (this.entries.isEmpty()) {
            LootRarity selectedRarity = LootRarity.random(rand, player.getLuck(), this.rarities);
            affixItem = LootController.createRandomLootItem(rand, selectedRarity, player, (ServerLevelAccessor) trader.level());
        }
        else {
            List<Wrapper<AffixLootEntry>> resolved = this.entries.stream().map(this::unwrap).filter(Objects::nonNull).map(e -> e.<AffixLootEntry>wrap(player.getLuck())).toList();
            AffixLootEntry entry = WeightedRandom.getRandomItem(rand, resolved).get().getData();
            LootRarity selectedRarity = LootRarity.random(rand, player.getLuck(), this.rarities == RarityClamp.UNCLAMPED ? entry : this.rarities);
            affixItem = LootController.createLootItem(entry.getStack().copy(), selectedRarity, rand);
        }

        if (affixItem.isEmpty()) return null;
        affixItem.getTag().putBoolean("apoth_merchant", true);
        return new MerchantOffer(this.price, this.price2, affixItem, 1, 100, 1);
    }

    @Override
    public boolean isRare() {
        return this.rare;
    }

    @Override
    public Codec<? extends JsonTrade> getCodec() {
        return CODEC;
    }

    /**
     * Unwraps the holder to its object, if present, otherwise returns null and logs an error.
     */
    private AffixLootEntry unwrap(DynamicHolder<AffixLootEntry> holder) {
        if (!holder.isBound()) {
            AdventureModule.LOGGER.error("An AffixTrade failed to resolve the Affix Loot Entry {}!", holder.getId());
            return null;
        }
        return holder.get();
    }

}
