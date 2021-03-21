package shadows.apotheosis.potion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

public class PotionCharmRecipe extends ShapedRecipe {

	protected final IntList potionSlots = new IntArrayList();

	public PotionCharmRecipe(List<Object> ingredients, int width, int height) {
		super(new ResourceLocation(Apotheosis.MODID, "potion_charm"), "", width, height, makeIngredients(ingredients), new ItemStack(ApotheosisObjects.POTION_CHARM));
		for (int i = 0; i < ingredients.size(); i++) {
			if (ingredients.get(i).equals("potion")) this.potionSlots.add(i);
		}
	}

	private static NonNullList<Ingredient> makeIngredients(List<Object> ingredients) {
		List<ItemStack> potionStacks = new ArrayList<>();
		List<Object> realIngredients = new ArrayList<>();
		for (Potion p : ForgeRegistries.POTION_TYPES) {
			if (p.getEffects().size() != 1 || p.getEffects().get(0).getPotion().isInstant()) continue;
			ItemStack potion = new ItemStack(Items.POTION);
			PotionUtils.addPotionToItemStack(potion, p);
			potionStacks.add(potion);
		}
		Ingredient potion = Ingredient.fromStacks(potionStacks.toArray(new ItemStack[0]));

		for (Object o : ingredients) {
			if (o.equals("potion")) realIngredients.add(potion);
			else realIngredients.add(o);
		}

		return Apotheosis.HELPER.createInput(true, realIngredients.toArray());
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		ItemStack out = super.getCraftingResult(inv);
		PotionUtils.addPotionToItemStack(out, PotionUtils.getPotionFromItem(inv.getStackInSlot(4)));
		return out;
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		if (super.matches(inv, world)) {
			List<Potion> potions = this.potionSlots.stream().map(s -> inv.getStackInSlot(s)).map(PotionUtils::getPotionFromItem).collect(Collectors.toList());
			if (potions.size() > 0 && potions.stream().allMatch(p -> p != null && p.getEffects().size() == 1 && !p.getEffects().get(0).getPotion().isInstant())) {
				return potions.stream().distinct().count() == 1;
			}
		}
		return false;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<PotionCharmRecipe> {

		public static final Serializer INSTANCE = new Serializer();

		@Override
		public PotionCharmRecipe read(ResourceLocation recipeId, JsonObject json) {
			JsonArray inputs = json.get("recipe").getAsJsonArray();
			int width = 0, height = inputs.size();
			List<Object> ingredients = new ArrayList<>();
			for (JsonElement e : inputs) {
				JsonArray arr = e.getAsJsonArray();
				width = arr.size();
				for (JsonElement input : arr) {
					if (input.isJsonPrimitive() && input.getAsString().equals("potion")) ingredients.add("potion");
					else ingredients.add(CraftingHelper.getIngredient(input));
				}
			}
			return new PotionCharmRecipe(ingredients, width, height);
		}

		@Override
		public PotionCharmRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			int width = buffer.readByte();
			int height = buffer.readByte();
			int potions = buffer.readByte();
			IntList potionSlots = new IntArrayList();
			for (int i = 0; i < potions; i++) {
				potionSlots.add(buffer.readByte());
			}

			List<Object> inputs = new ArrayList<>(width * height);

			for (int i = 0; i < width * height; i++) {
				if (!potionSlots.contains(i)) inputs.add(i, Ingredient.read(buffer));
				else inputs.add("potion");
			}

			return new PotionCharmRecipe(inputs, width, height);
		}

		@Override
		public void write(PacketBuffer buffer, PotionCharmRecipe recipe) {
			buffer.writeByte(recipe.getRecipeWidth());
			buffer.writeByte(recipe.getRecipeHeight());
			buffer.writeByte(recipe.potionSlots.size());
			for (int i : recipe.potionSlots) {
				buffer.writeByte(i);
			}

			List<Ingredient> inputs = recipe.getIngredients();
			for (int i = 0; i < inputs.size(); i++) {
				if (!recipe.potionSlots.contains(i)) inputs.get(i).write(buffer);
			}
		}

	}

}