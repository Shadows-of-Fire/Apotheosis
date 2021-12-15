package shadows.apotheosis.village.wanderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.BasicItemListing;
import shadows.apotheosis.util.JsonUtil;
import shadows.apotheosis.village.VillageModule;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.NBTAdapter;

public class WandererTradeManager extends SimpleJsonResourceReloadListener {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(BasicItemListing.class, BasicItemListingAdapter.INSTANCE).registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE).registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE).create();

	public static final WandererTradeManager INSTANCE = new WandererTradeManager();

	protected final Map<ResourceLocation, BasicItemListing> registry = new HashMap<>();
	protected final List<BasicItemListing> normTrades = new ArrayList<>();
	protected final List<BasicItemListing> rareTrades = new ArrayList<>();

	public WandererTradeManager() {
		super(GSON, "wanderer_trades");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profiler) {
		this.normTrades.clear();
		this.rareTrades.clear();
		this.registry.clear();
		objects.forEach((id, obj) -> {
			try {
				if (!JsonUtil.checkAndLogEmpty(obj, id, "Wanderer Trade", VillageModule.LOGGER)) this.register(id, GSON.fromJson(obj, BasicItemListing.class), obj.getAsJsonObject().has("rare"));
			} catch (Exception e) {
				VillageModule.LOGGER.error("Failed to load Wandering Trader trade {}.", id.toString());
				e.printStackTrace();
			}
		});
		VillageModule.LOGGER.info("Loaded {} normal and {} rare Wandering Trader trade options.", this.normTrades.size(), this.rareTrades.size());
	}

	protected void register(ResourceLocation id, BasicItemListing trade, boolean rare) {
		MerchantOffer offer = trade.getOffer(null, null);
		if (offer.getResult() == null || offer.getResult().isEmpty() || offer.getMaxUses() == 0) return;
		if (!this.registry.containsKey(id)) {
			this.registry.put(id, trade);
			if (rare) this.rareTrades.add(trade);
			else this.normTrades.add(trade);
		} else VillageModule.LOGGER.error("Attempted to register a wanderer trade with name {}, but it already exists!", id);
	}

	public List<BasicItemListing> getNormalTrades() {
		return this.normTrades;
	}

	public List<BasicItemListing> getRareTrades() {
		return this.rareTrades;
	}

}
