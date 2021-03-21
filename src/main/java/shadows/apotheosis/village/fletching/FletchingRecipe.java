package shadows.apotheosis.village.fletching;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.village.VillageModule;

public class FletchingRecipe implements IRecipe<CraftingInventory> {

	protected final ResourceLocation id;
	protected final ItemStack output;
	protected final List<Ingredient> inputs = NonNullList.withSize(3, Ingredient.EMPTY);

	public FletchingRecipe(ResourceLocation id, ItemStack output, List<Ingredient> inputs) {
		this.id = id;
		this.output = output;
		for (int i = 0; i < Math.min(3, inputs.size()); i++) {
			this.inputs.set(i, inputs.get(i));
		}
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		for (int i = 0; i < 3; i++) {
			if (!this.inputs.get(i).test(inv.getStackInSlot(i))) return false;
		}
		return true;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		return this.output.copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return width == 1 && height == 3;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return this.output;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return VillageModule.FLETCHING_SERIALIZER;
	}

	@Override
	public IRecipeType<?> getType() {
		return VillageModule.FLETCHING;
	}

	public List<Ingredient> getInputs() {
		return this.inputs;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FletchingRecipe> {
		public static final ResourceLocation NAME = new ResourceLocation(Apotheosis.MODID, "fletching");

		@Override
		public FletchingRecipe read(ResourceLocation recipeId, JsonObject json) {
			NonNullList<Ingredient> nonnulllist = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
			if (nonnulllist.isEmpty()) {
				throw new JsonParseException("No ingredients for fletching recipe");
			} else if (nonnulllist.size() > 3) {
				throw new JsonParseException("Too many ingredients for fletching recipe, max 3");
			} else {
				ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
				return new FletchingRecipe(recipeId, itemstack, nonnulllist);
			}
		}

		private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray) {
			NonNullList<Ingredient> nonnulllist = NonNullList.create();

			for (int i = 0; i < ingredientArray.size(); ++i) {
				Ingredient ingredient = Ingredient.deserialize(ingredientArray.get(i));
				if (!ingredient.hasNoMatchingItems()) {
					nonnulllist.add(ingredient);
				}
			}

			return nonnulllist;
		}

		@Override
		public FletchingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			NonNullList<Ingredient> nonnulllist = NonNullList.withSize(3, Ingredient.EMPTY);
			for (int j = 0; j < nonnulllist.size(); ++j) {
				nonnulllist.set(j, Ingredient.read(buffer));
			}
			ItemStack itemstack = buffer.readItemStack();
			return new FletchingRecipe(recipeId, itemstack, nonnulllist);
		}

		@Override
		public void write(PacketBuffer buffer, FletchingRecipe recipe) {
			for (Ingredient ingredient : recipe.inputs) {
				ingredient.write(buffer);
			}
			buffer.writeItemStack(recipe.output);
		}

	}

}