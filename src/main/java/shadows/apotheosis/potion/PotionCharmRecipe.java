package shadows.apotheosis.potion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.placebo.recipe.RecipeHelper;

public class PotionCharmRecipe extends ShapedRecipe {

	protected final IntList potionSlots = new IntArrayList();
	protected final Ingredient potion = makePotionIngredient();

	public PotionCharmRecipe(List<Object> ingredients, int width, int height) {
		super(new ResourceLocation(Apotheosis.MODID, "potion_charm"), "", width, height, makeIngredients(ingredients), new ItemStack(Apoth.Items.POTION_CHARM.get()));
		for (int i = 0; i < ingredients.size(); i++) {
			if (ingredients.get(i).equals("potion")) this.potionSlots.add(i);
		}
	}

	private static Ingredient makePotionIngredient() {
		List<ItemStack> potionStacks = new ArrayList<>();
		for (Potion p : ForgeRegistries.POTIONS) {
			if (p.getEffects().size() != 1 || p.getEffects().get(0).getEffect().isInstantenous()) continue;
			ItemStack potion = new ItemStack(Items.POTION);
			PotionUtils.setPotion(potion, p);
			potionStacks.add(potion);
		}
		return Ingredient.of(potionStacks.toArray(new ItemStack[0]));
	}

	private static NonNullList<Ingredient> makeIngredients(List<Object> ingredients) {
		List<Object> realIngredients = new ArrayList<>();
		Ingredient potion = makePotionIngredient();

		for (Object o : ingredients) {
			if (o.equals("potion")) realIngredients.add(potion);
			else realIngredients.add(o);
		}

		return RecipeHelper.createInput(Apotheosis.MODID, true, realIngredients.toArray());
	}

	public Ingredient getPotionIngredient() {
		return this.potion;
	}

	public IntList getPotionSlots() {
		return this.potionSlots;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv) {
		ItemStack out = super.assemble(inv);
		PotionUtils.setPotion(out, PotionUtils.getPotion(inv.getItem(4)));
		return out;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		if (super.matches(inv, world)) {
			List<Potion> potions = this.potionSlots.intStream().mapToObj(s -> inv.getItem(s)).map(PotionUtils::getPotion).collect(Collectors.toList());
			if (potions.size() > 0 && potions.stream().allMatch(p -> p != null && p.getEffects().size() == 1 && !p.getEffects().get(0).getEffect().isInstantenous())) {
				return potions.stream().distinct().count() == 1;
			}
		}
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer implements RecipeSerializer<PotionCharmRecipe> {

		public static final Serializer INSTANCE = new Serializer();

		@Override
		public PotionCharmRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
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
		public PotionCharmRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			int width = buffer.readByte();
			int height = buffer.readByte();
			int potions = buffer.readByte();
			IntList potionSlots = new IntArrayList();
			for (int i = 0; i < potions; i++) {
				potionSlots.add(buffer.readByte());
			}

			List<Object> inputs = new ArrayList<>(width * height);

			for (int i = 0; i < width * height; i++) {
				if (!potionSlots.contains(i)) inputs.add(i, Ingredient.fromNetwork(buffer));
				else inputs.add("potion");
			}

			return new PotionCharmRecipe(inputs, width, height);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, PotionCharmRecipe recipe) {
			buffer.writeByte(recipe.getRecipeWidth());
			buffer.writeByte(recipe.getRecipeHeight());
			buffer.writeByte(recipe.potionSlots.size());
			for (int i : recipe.potionSlots) {
				buffer.writeByte(i);
			}

			List<Ingredient> inputs = recipe.getIngredients();
			for (int i = 0; i < inputs.size(); i++) {
				if (!recipe.potionSlots.contains(i)) inputs.get(i).toNetwork(buffer);
			}
		}

	}

}