package dev.shadowsoffire.apotheosis.village.wanderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.BasicItemListing;

public class WandererTrade extends BasicItemListing implements JsonTrade {

    public static Codec<WandererTrade> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            ItemAdapter.CODEC.fieldOf("input_1").forGetter(trade -> trade.price),
            ItemAdapter.CODEC.optionalFieldOf("input_2", ItemStack.EMPTY).forGetter(trade -> trade.price2),
            ItemAdapter.CODEC.fieldOf("output").forGetter(trade -> trade.forSale),
            Codec.INT.optionalFieldOf("max_trades", 1).forGetter(trade -> trade.maxTrades),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(trade -> trade.xp),
            Codec.FLOAT.optionalFieldOf("price_mult", 1F).forGetter(trade -> trade.priceMult),
            Codec.BOOL.optionalFieldOf("rare", false).forGetter(trade -> trade.rare))
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
