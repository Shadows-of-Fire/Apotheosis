package dev.shadowsoffire.apotheosis.village.wanderer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.BasicItemListing;

public class BasicItemListingAdapter implements JsonDeserializer<BasicItemListing>, JsonSerializer<BasicItemListing> {

    public static final BasicItemListingAdapter INSTANCE = new BasicItemListingAdapter();

    @Override
    public BasicItemListing deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        ItemStack price1 = ctx.deserialize(obj.get("input_1"), ItemStack.class);
        ItemStack price2 = obj.has("input_2") ? ctx.deserialize(obj.get("input_2"), ItemStack.class) : ItemStack.EMPTY;
        ItemStack output = ctx.deserialize(obj.get("output"), ItemStack.class);
        int maxTrades = obj.has("max_trades") ? obj.get("max_trades").getAsInt() : 1;
        int xp = obj.has("xp") ? obj.get("xp").getAsInt() : 0;
        float priceMult = obj.has("price_mult") ? obj.get("price_mult").getAsFloat() : 1;
        return new BasicItemListing(price1, price2, output, maxTrades, xp, priceMult);
    }

    @Override
    public JsonElement serialize(BasicItemListing src, Type typeOfSrc, JsonSerializationContext ctx) {
        JsonObject obj = new JsonObject();
        MerchantOffer offer = src.getOffer(null, null);
        obj.add("input_1", ctx.serialize(offer.getBaseCostA()));
        obj.add("input_2", ctx.serialize(offer.getCostB()));
        obj.add("output", ctx.serialize(offer.getResult()));
        obj.addProperty("max_trades", offer.getMaxUses());
        obj.addProperty("xp", offer.getXp());
        obj.addProperty("price_mult", offer.getPriceMultiplier());
        return obj;
    }
}
