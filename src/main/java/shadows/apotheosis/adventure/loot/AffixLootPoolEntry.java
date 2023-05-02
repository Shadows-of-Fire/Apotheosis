package shadows.apotheosis.adventure.loot;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.placebo.json.ListenerCallback;

public class AffixLootPoolEntry extends LootPoolSingletonContainer {
	public static final Serializer SERIALIZER = new Serializer();
	public static final LootPoolEntryType TYPE = new LootPoolEntryType(SERIALIZER);
	private static Set<AffixLootPoolEntry> awaitingLoad = Collections.newSetFromMap(new WeakHashMap<>());
	static {
		AffixLootManager.INSTANCE.registerCallback(ListenerCallback.reloadOnly(r -> {
			awaitingLoad.forEach(AffixLootPoolEntry::resolve);
		}));
	}

	@Nullable
	private final LootRarity rarity;
	private final List<ResourceLocation> entries;
	private List<AffixLootEntry> resolvedEntries = Collections.emptyList();

	public AffixLootPoolEntry(@Nullable LootRarity rarity, List<ResourceLocation> entries, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
		super(weight, quality, conditions, functions);
		this.rarity = rarity;
		this.entries = entries;
		if (!this.entries.isEmpty()) awaitingLoad.add(this);
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
		ItemStack stack;
		if (this.resolvedEntries.isEmpty()) {
			var player = GemLootPoolEntry.findPlayer(ctx);
			if (player == null) return;
			stack = LootController.createRandomLootItem(ctx.getRandom(), this.rarity, player, ctx.getLevel());
		} else {
			AffixLootEntry entry = WeightedRandom.getRandomItem(ctx.getRandom(), this.resolvedEntries.stream().map(e -> e.<AffixLootEntry>wrap(ctx.getLuck())).toList()).get().getData();
			stack = LootController.createLootItem(entry.getStack().copy(), this.rarity == null ? LootRarity.random(ctx.getRandom(), ctx.getLuck(), entry) : this.rarity, ctx.getRandom());
		}
		if (!stack.isEmpty()) list.accept(stack);
	}

	@Override
	public LootPoolEntryType getType() {
		return TYPE;
	}

	private void resolve() {
		this.resolvedEntries = this.entries.stream().map(id -> printErrorOnNull(AffixLootManager.INSTANCE.getValue(id), id)).filter(Predicates.notNull()).toList();
	}

	private <T> T printErrorOnNull(T t, ResourceLocation id) {
		if (t == null) AdventureModule.LOGGER.error("An AffixLootPoolEntry failed to resolve the Affix Entry {}!", id);
		return t;
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<AffixLootPoolEntry> {

		@Override
		protected AffixLootPoolEntry deserialize(JsonObject obj, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] lootConditions, LootItemFunction[] lootFunctions) {
			LootRarity rarity = LootRarity.byId(GsonHelper.getAsString(obj, "rarity", ""));
			List<String> entries = context.deserialize(GsonHelper.getAsJsonArray(obj, "entries", new JsonArray()), new TypeToken<List<String>>() {
			}.getType());
			return new AffixLootPoolEntry(rarity, entries.stream().map(ResourceLocation::new).toList(), weight, quality, lootConditions, lootFunctions);
		}

		@Override
		public void serializeCustom(JsonObject object, AffixLootPoolEntry e, JsonSerializationContext ctx) {
			if (e.rarity != null) object.addProperty("rarity", e.rarity.id());
			object.add("entries", ctx.serialize(e.entries));
			super.serializeCustom(object, e, ctx);
		}

	}
}