package dev.shadowsoffire.apotheosis.village.wanderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.BasicItemListing;

public class WandererTrade extends BasicItemListing implements JsonTrade {

    public static Codec<WandererTrade> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            ItemAdapter.CODEC.fieldOf("input_1").forGetter(trade -> trade.price),
            PlaceboCodecs.nullableField(ItemAdapter.CODEC, "input_2", ItemStack.EMPTY).forGetter(trade -> trade.price2),
            ItemAdapter.CODEC.fieldOf("output").forGetter(trade -> trade.forSale),
            PlaceboCodecs.nullableField(Codec.INT, "max_trades", 1).forGetter(trade -> trade.maxTrades),
            PlaceboCodecs.nullableField(Codec.INT, "xp", 0).forGetter(trade -> trade.xp),
            PlaceboCodecs.nullableField(Codec.FLOAT, "price_mult", 1F).forGetter(trade -> trade.priceMult),
            PlaceboCodecs.nullableField(Codec.BOOL, "rare", false).forGetter(trade -> trade.rare))
        .apply(inst, WandererTrade::new));

    protected final boolean rare;

    public WandererTrade(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int xp, float priceMult, boolean rare) {
        super(price, price2, forSale, maxTrades, xp, priceMult);
        this.rare = rare;
    }

    @Override
    public boolean isRare() {
        return this.rare;
    }

    @Override
    public Codec<? extends JsonTrade> getCodec() {
        return CODEC;
    }

}
