package shadows.apotheosis.deadly.loot;

import java.util.Locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.crafting.CraftingHelper;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 */
public class LootEntry extends WeightedRandom.Item {

	protected final ItemStack stack;
	protected final EquipmentType type;

	public LootEntry(ItemStack stack, EquipmentType type, int weight) {
		super(weight);
		this.stack = stack;
		this.type = type;
	}

	public ItemStack getStack() {
		return stack;
	}

	public EquipmentType getType() {
		return type;
	}

	public static LootEntry deserialize(JsonObject obj) {
		JsonElement stack = obj.get("stack");
		JsonElement type = obj.get("type");
		JsonElement weight = obj.get("weight");
		ItemStack _stack = CraftingHelper.getItemStack(stack.getAsJsonObject(), true);
		EquipmentType _type = EquipmentType.valueOf(type.getAsString().toUpperCase(Locale.ROOT));
		int _weight = weight.getAsInt();
		return new LootEntry(_stack, _type, _weight);
	}

}
