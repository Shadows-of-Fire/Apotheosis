package shadows.apotheosis.village.wanderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BasicTrade;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.placebo.util.json.ItemAdapter;
import shadows.placebo.util.json.NBTAdapter;

public class WandererTradeManager extends JsonReloadListener {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(BasicTrade.class, BasicTradeAdapter.INSTANCE).registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE).registerTypeAdapter(CompoundNBT.class, NBTAdapter.INSTANCE).create();

	public static final WandererTradeManager INSTANCE = new WandererTradeManager();

	protected final Map<ResourceLocation, BasicTrade> registry = new HashMap<>();
	protected final List<BasicTrade> normTrades = new ArrayList<>();
	protected final List<BasicTrade> rareTrades = new ArrayList<>();

	public WandererTradeManager() {
		super(GSON, "wanderer_trades");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager mgr, IProfiler profiler) {
		this.normTrades.clear();
		this.rareTrades.clear();
		this.registry.clear();
		objects.forEach((id, obj) -> {
			try {
				this.register(id, GSON.fromJson(obj, BasicTrade.class), obj.getAsJsonObject().has("rare"));
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load Wandering Trader trade {}.", id.toString());
				e.printStackTrace();
			}
		});
		DeadlyModule.LOGGER.info("Loaded {} normal and {} rare Wandering Trader trade options.", this.normTrades.size(), this.rareTrades.size());
	}

	protected void register(ResourceLocation id, BasicTrade trade, boolean rare) {
		if (!this.registry.containsKey(id)) {
			this.registry.put(id, trade);
			if (rare) this.rareTrades.add(trade);
			else this.normTrades.add(trade);
		} else DeadlyModule.LOGGER.error("Attempted to register a wanderer trade with name {}, but it already exists!", id);
	}

	public List<BasicTrade> getNormalTrades() {
		return this.normTrades;
	}

	public List<BasicTrade> getRareTrades() {
		return this.rareTrades;
	}

}
