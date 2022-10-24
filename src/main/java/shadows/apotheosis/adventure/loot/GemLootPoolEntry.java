package shadows.apotheosis.adventure.loot;

import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import shadows.apotheosis.adventure.affix.socket.Gem;
import shadows.apotheosis.adventure.affix.socket.GemItem;
import shadows.apotheosis.adventure.affix.socket.GemManager;

public class GemLootPoolEntry extends LootPoolSingletonContainer {
	public static final Serializer SERIALIZER = new Serializer();
	public static final LootPoolEntryType TYPE = new LootPoolEntryType(SERIALIZER);

	public GemLootPoolEntry(int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
		super(weight, quality, conditions, functions);
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
		Gem gem = GemManager.INSTANCE.getRandomItem(ctx.getRandom(), ctx.getLevel());
		ItemStack stack = GemItem.fromGem(gem, ctx.getRandom());
		list.accept(stack);
	}

	@Override
	public LootPoolEntryType getType() {
		return TYPE;
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<GemLootPoolEntry> {

		@Override
		protected GemLootPoolEntry deserialize(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] lootConditions, LootItemFunction[] lootFunctions) {
			return new GemLootPoolEntry(weight, quality, lootConditions, lootFunctions);
		}

		@Override
		public void serializeCustom(JsonObject object, GemLootPoolEntry e, JsonSerializationContext conditions) {
			super.serializeCustom(object, e, conditions);
		}

	}
}