package shadows.apotheosis.village.wanderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.BasicItemListing;
import shadows.placebo.json.PSerializer;

public class BasicJsonTrade extends BasicItemListing implements JsonTrade {

	protected ResourceLocation id;
	protected PSerializer<JsonTrade> serializer;

	protected final boolean rare;

	public BasicJsonTrade(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int xp, float priceMult, boolean rare) {
		super(price, price2, forSale, maxTrades, xp, priceMult);
		this.rare = rare;
	}

	@Override
	public void setId(ResourceLocation id) {
		if (this.id != null) throw new UnsupportedOperationException();
		this.id = id;
	}

	@Override
	public void setSerializer(PSerializer<JsonTrade> serializer) {
		if (this.serializer != null) throw new UnsupportedOperationException();
		this.serializer = serializer;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public PSerializer<JsonTrade> getSerializer() {
		return this.serializer;
	}

	@Override
	public boolean isRare() {
		return this.rare;
	}

}
