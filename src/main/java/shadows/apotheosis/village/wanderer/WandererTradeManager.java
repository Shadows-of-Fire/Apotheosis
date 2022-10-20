package shadows.apotheosis.village.wanderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.BasicItemListing;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.trades.AffixTrade;
import shadows.apotheosis.village.VillageModule;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.PlaceboJsonReloadListener;

public class WandererTradeManager extends PlaceboJsonReloadListener<JsonTrade> {

	public static final WandererTradeManager INSTANCE = new WandererTradeManager();

	protected final Map<ResourceLocation, BasicItemListing> registry = new HashMap<>();
	protected final List<ItemListing> normTrades = new ArrayList<>();
	protected final List<ItemListing> rareTrades = new ArrayList<>();

	public WandererTradeManager() {
		super(VillageModule.LOGGER, "wanderer_trades", false, true);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(new ResourceLocation(Apotheosis.MODID, "basic_trade"), new PSerializer.Builder<JsonTrade>("Basic JSON Trade").withJsonDeserializer(e -> {
			JsonObject obj = e.getAsJsonObject();
			ItemStack price1 = ItemAdapter.ITEM_READER.fromJson(obj.get("input_1"), ItemStack.class);
			ItemStack price2 = obj.has("input_2") ? ItemAdapter.ITEM_READER.fromJson(obj.get("input_2"), ItemStack.class) : ItemStack.EMPTY;
			ItemStack output = ItemAdapter.ITEM_READER.fromJson(obj.get("output"), ItemStack.class);
			int maxTrades = GsonHelper.getAsInt(obj, "max_trades", 1);
			int xp = GsonHelper.getAsInt(obj, "xp", 0);
			float priceMult = GsonHelper.getAsFloat(obj, "price_mult", 1);
			boolean rare = GsonHelper.getAsBoolean(obj, "rare", false);
			return new BasicJsonTrade(price1, price2, output, maxTrades, xp, priceMult, rare);
		}));
		this.registerSerializer(new ResourceLocation(Apotheosis.MODID, "affix"), new PSerializer.Builder<JsonTrade>("Affix Trade").withJsonDeserializer(obj -> ItemAdapter.ITEM_READER.fromJson(obj, AffixTrade.class)));
	}

	@Override
	protected <T extends JsonTrade> void register(ResourceLocation key, T trade) {
		super.register(key, trade);
	}

	@Override
	protected void onReload() {
		super.onReload();
		this.getValues().forEach(trade -> {
			if (trade.isRare()) this.rareTrades.add(trade);
			else this.normTrades.add(trade);
		});
	}

	public List<ItemListing> getNormalTrades() {
		return this.normTrades;
	}

	public List<ItemListing> getRareTrades() {
		return this.rareTrades;
	}

}
