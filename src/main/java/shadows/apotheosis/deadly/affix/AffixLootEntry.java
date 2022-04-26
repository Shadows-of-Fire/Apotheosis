package shadows.apotheosis.deadly.affix;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.util.Weighted;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a lootCategory, which is used to determine possible affixes.
 */
public class AffixLootEntry extends Weighted implements PlaceboJsonReloadListener.TypeKeyed<AffixLootEntry> {

	protected final ItemStack stack;
	protected final LootCategory type;
	protected ResourceLocation id;
	private SerializerBuilder<AffixLootEntry>.Serializer serializer;

	public AffixLootEntry(ItemStack stack, LootCategory lootCategory, int weight) {
		super(weight);
		this.stack = stack;
		this.type = lootCategory;
	}

	public ItemStack getStack() {
		return this.stack;
	}

	public LootCategory getType() {
		return this.type;
	}
	@Override
	public void setId(ResourceLocation id) {
		if (this.id != null) throw new UnsupportedOperationException();
		this.id = id;
	}

	@Override
	public void setSerializer(SerializerBuilder<AffixLootEntry>.Serializer serializer) {
		if (this.serializer != null) throw new UnsupportedOperationException();
		this.serializer = serializer;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public SerializerBuilder<AffixLootEntry>.Serializer getSerializer() {
		return this.serializer;
	}
}