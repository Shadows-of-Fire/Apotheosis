package dev.shadowsoffire.apotheosis.village.wanderer;

import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import dev.shadowsoffire.placebo.json.TypeKeyed;

public interface JsonTrade extends ItemListing, TypeKeyed<JsonTrade> {

    boolean isRare();

}
