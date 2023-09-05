package dev.shadowsoffire.apotheosis.village.wanderer;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;

public interface JsonTrade extends ItemListing, CodecProvider<JsonTrade> {

    boolean isRare();

}
