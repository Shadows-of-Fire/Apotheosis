package dev.shadowsoffire.apotheosis.village.wanderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.ItemAdapter;
import dev.shadowsoffire.placebo.json.PSerializer;
import net.minecraft.resources.ResourceLocation;
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

    public static final PSerializer<WandererTrade> SERIALIZER = PSerializer.fromCodec("Villager Trade", CODEC);

    protected ResourceLocation id;
    protected final boolean rare;

    public WandererTrade(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int xp, float priceMult, boolean rare) {
        super(price, price2, forSale, maxTrades, xp, priceMult);
        this.rare = rare;
    }

    @Override
    public void setId(ResourceLocation id) {
        if (this.id != null) throw new UnsupportedOperationException();
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public boolean isRare() {
        return this.rare;
    }

    @Override
    public PSerializer<? extends JsonTrade> getSerializer() {
        return SERIALIZER;
    }

}
