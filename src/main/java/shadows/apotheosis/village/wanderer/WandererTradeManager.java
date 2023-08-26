package shadows.apotheosis.village.wanderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraftforge.common.BasicItemListing;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.trades.AffixTrade;
import shadows.apotheosis.village.VillageModule;
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
        this.registerSerializer(Apotheosis.loc("basic_trade"), BasicJsonTrade.SERIALIZER);
        this.registerSerializer(Apotheosis.loc("affix"), AffixTrade.SERIALIZER);
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.normTrades.clear();
        this.rareTrades.clear();
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
