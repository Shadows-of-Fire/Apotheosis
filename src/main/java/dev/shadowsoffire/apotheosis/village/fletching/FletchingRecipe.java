package dev.shadowsoffire.apotheosis.village.fletching;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.village.VillageModule;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class FletchingRecipe implements Recipe<CraftingContainer> {

    protected final ResourceLocation id;
    protected final ItemStack output;
    protected final NonNullList<Ingredient> inputs = NonNullList.withSize(3, Ingredient.EMPTY);

    public FletchingRecipe(ResourceLocation id, ItemStack output, List<Ingredient> inputs) {
        this.id = id;
        this.output = output;
        for (int i = 0; i < Math.min(3, inputs.size()); i++) {
            this.inputs.set(i, inputs.get(i));
        }
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        for (int i = 0; i < 3; i++) {
            if (!this.inputs.get(i).test(inv.getItem(i))) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess reg) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 1 && height == 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess reg) {
        return this.output;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return VillageModule.FLETCHING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypes.FLETCHING;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public static class Serializer implements RecipeSerializer<FletchingRecipe> {
        public static final ResourceLocation NAME = new ResourceLocation(Apotheosis.MODID, "fletching");

        @Override
        public FletchingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            NonNullList<Ingredient> nonnulllist = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for fletching recipe");
            }
            else if (nonnulllist.size() > 3) {
                throw new JsonParseException("Too many ingredients for fletching recipe, max 3");
            }
            else {
                ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
                return new FletchingRecipe(recipeId, itemstack, nonnulllist);
            }
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < ingredientArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
                if (!ingredient.isEmpty()) {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        @Override
        public FletchingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(3, Ingredient.EMPTY);
            for (int j = 0; j < nonnulllist.size(); ++j) {
                nonnulllist.set(j, Ingredient.fromNetwork(buffer));
            }
            ItemStack itemstack = buffer.readItem();
            return new FletchingRecipe(recipeId, itemstack, nonnulllist);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FletchingRecipe recipe) {
            for (Ingredient ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.output);
        }

    }

}
