package shadows.apotheosis.spawn.modifiers;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.spawn.SpawnerModifiers;

public class ModifierSync {
	public static final RecipeType<ModifierRecipe> TYPE = RecipeType.register(Apotheosis.MODID + ":spawner_modifiers");
	public static final Serializer SERIALIZER = new Serializer();

	public static class ModifierRecipe extends SingleItemRecipe {

		protected final SpawnerModifier modif;

		public ModifierRecipe(SpawnerModifier modif) {
			super(TYPE, SERIALIZER, new ResourceLocation(Apotheosis.MODID, modif.getId()), "", Ingredient.EMPTY, ItemStack.EMPTY);
			this.modif = modif;
		}

		@Override
		public boolean matches(Container inv, Level worldIn) {
			return false;
		}

	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ModifierRecipe> {
		@Override
		public ModifierRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			return null;
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, ModifierRecipe recipe) {
			buffer.writeUtf(recipe.modif.getId());
			recipe.modif.getIngredient().toNetwork(buffer);
			buffer.writeInt(recipe.modif.value);
			buffer.writeInt(recipe.modif.min);
			buffer.writeInt(recipe.modif.max);
		}

		@Override
		public ModifierRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			SpawnerModifier modif = SpawnerModifiers.MODIFIERS.get(buffer.readUtf(50));
			modif.sync(Ingredient.fromNetwork(buffer), buffer.readInt(), buffer.readInt(), buffer.readInt());
			return new ModifierRecipe(modif);
		}

	}
}
