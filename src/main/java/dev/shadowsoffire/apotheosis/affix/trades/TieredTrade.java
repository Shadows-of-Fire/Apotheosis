package dev.shadowsoffire.apotheosis.affix.trades;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.systems.wanderer.WandererTrade;
import dev.shadowsoffire.placebo.systems.wanderer.WandererTradesRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;

public record TieredTrade(Map<WorldTier, WandererTrade> trades) implements WandererTrade {

    public static final Codec<TieredTrade> CODEC = WorldTier.mapCodec(WandererTradesRegistry.INSTANCE.elementCodec()).xmap(TieredTrade::new, TieredTrade::trades).fieldOf("trades").codec();

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        Player player = trader.level().getNearestPlayer(trader, -1);
        if (player != null) {
            WorldTier tier = WorldTier.getTier(player);
            WandererTrade trade = trades.get(tier);
            if (trade != null) {
                return trade.getOffer(trader, random);
            }
        }
        // As a fallback we generate a random affix item. Provides a better UX than a missing item entirely.
        return new AutomaticAffixTrade(Set.of(), List.of(), false).getOffer(trader, random);
    }

    @Override
    public Codec<? extends WandererTrade> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isRare() {
        return trades.values().stream().anyMatch(WandererTrade::isRare);
    }

}
