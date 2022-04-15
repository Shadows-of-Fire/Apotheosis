package shadows.apotheosis.util;

import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class UnenchantedIngredient extends Ingredient {

	private final IItemProvider item;

	public UnenchantedIngredient(IItemProvider item) {
		super(Stream.of(new Ingredient.SingleItemList(new ItemStack(item))));
		this.item = item;
	}

	@Override
	public boolean test(ItemStack stack) {
		return super.test(stack) && !stack.isEnchanted();
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public JsonElement toJson() {
		return new JsonObject();
	}

	public static class Serializer implements IIngredientSerializer<UnenchantedIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public UnenchantedIngredient parse(PacketBuffer buffer) {
			ItemStack stack = buffer.readItem();
			return new UnenchantedIngredient(stack.getItem());
		}

		@Override
		public UnenchantedIngredient parse(JsonObject json) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("item").getAsString()));
			return new UnenchantedIngredient(item);
		}

		@Override
		public void write(PacketBuffer buffer, UnenchantedIngredient ingredient) {
			buffer.writeItem(new ItemStack(ingredient.item));
		}
	}

}