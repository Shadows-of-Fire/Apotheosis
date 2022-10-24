package shadows.apotheosis.adventure.loot;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AffixLootPoolEntry extends LootPoolSingletonContainer {
	public static final Serializer SERIALIZER = new Serializer();
	public static final LootPoolEntryType TYPE = new LootPoolEntryType(SERIALIZER);

	@Nullable
	private final LootRarity rarity;

	public AffixLootPoolEntry(@Nullable LootRarity rarity, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
		super(weight, quality, conditions, functions);
		this.rarity = rarity;
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
		ItemStack stack = LootController.createRandomLootItem(ctx.getRandom(), null, ctx.getLuck(), ctx.getLevel());
		list.accept(stack);
	}

	@Override
	public LootPoolEntryType getType() {
		return TYPE;
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<AffixLootPoolEntry> {

		@Override
		protected AffixLootPoolEntry deserialize(JsonObject obj, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] lootConditions, LootItemFunction[] lootFunctions) {
			LootRarity rarity = LootRarity.byId(GsonHelper.getAsString(obj, "rarity", ""));
			return new AffixLootPoolEntry(rarity, weight, quality, lootConditions, lootFunctions);
		}

		@Override
		public void serializeCustom(JsonObject object, AffixLootPoolEntry e, JsonSerializationContext conditions) {
			if (e.rarity != null) object.addProperty("rarity", e.rarity.id());
			super.serializeCustom(object, e, conditions);
		}

	}
}