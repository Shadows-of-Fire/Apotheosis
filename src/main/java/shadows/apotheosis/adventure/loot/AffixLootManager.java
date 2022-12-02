package shadows.apotheosis.adventure.loot;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.WeightedJsonReloadListener;

/**
 * Core loot registry.  Handles the management of all Affixes, LootEntries, and generation of loot items.
 */
public class AffixLootManager extends WeightedJsonReloadListener<AffixLootEntry> {

	//Formatter::off
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE)
			.registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE)
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.registerTypeAdapter(LootRarity.class, new LootRarity.Serializer())
			.setPrettyPrinting().create();
	//Formatter::on

	public static final AffixLootManager INSTANCE = new AffixLootManager();

	private AffixLootManager() {
		super(AdventureModule.LOGGER, "affix_loot_entries", false, false);
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, new PSerializer.Builder<AffixLootEntry>("Affix Loot Entry").json(obj -> GSON.fromJson(obj, AffixLootEntry.class), e -> GSON.toJsonTree(e).getAsJsonObject()));
	}

	@Override
	protected void validateItem(AffixLootEntry item) {
		super.validateItem(item);
		Preconditions.checkArgument(!item.stack.isEmpty());
		Preconditions.checkArgument(item.type != null);
		Preconditions.checkArgument(!item.type.isNone());
	}

}