package dev.shadowsoffire.apotheosis.village.wanderer;

import dev.shadowsoffire.placebo.json.PSerializer.PSerializable;
import dev.shadowsoffire.placebo.reload.TypeKeyed;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;

public interface JsonTrade extends ItemListing, TypeKeyed, PSerializable<JsonTrade> {

    boolean isRare();

}
