package shadows.apotheosis.village.wanderer;

import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import shadows.placebo.json.TypeKeyed;

public interface JsonTrade extends ItemListing, TypeKeyed<JsonTrade> {

    boolean isRare();

}
