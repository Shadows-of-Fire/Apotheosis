package shadows.apotheosis.village.wanderer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraftforge.common.BasicTrade;

public class BasicTradeAdapter implements JsonDeserializer<BasicTrade>, JsonSerializer<BasicTrade> {

	public static final BasicTradeAdapter INSTANCE = new BasicTradeAdapter();

	@Override
	public BasicTrade deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		ItemStack price1 = ctx.deserialize(obj.get("input_1"), ItemStack.class);
		ItemStack price2 = obj.has("input_2") ? ctx.deserialize(obj.get("input_2"), ItemStack.class) : ItemStack.EMPTY;
		ItemStack output = ctx.deserialize(obj.get("output"), ItemStack.class);
		int maxTrades = obj.has("max_trades") ? obj.get("max_trades").getAsInt() : 1;
		int xp = obj.has("xp") ? obj.get("xp").getAsInt() : 0;
		float priceMult = obj.has("price_mult") ? obj.get("price_mult").getAsFloat() : 1;
		return new BasicTrade(price1, price2, output, maxTrades, xp, priceMult);
	}

	@Override
	public JsonElement serialize(BasicTrade src, Type typeOfSrc, JsonSerializationContext ctx) {
		JsonObject obj = new JsonObject();
		MerchantOffer offer = src.getOffer(null, null);
		obj.add("input_1", ctx.serialize(offer.getBuyingStackFirst()));
		obj.add("input_2", ctx.serialize(offer.getBuyingStackSecond()));
		obj.add("output", ctx.serialize(offer.getSellingStack()));
		obj.addProperty("max_trades", offer.getMaxUses());
		obj.addProperty("xp", offer.getGivenExp());
		obj.addProperty("price_mult", offer.getPriceMultiplier());
		return obj;
	}
}
