package shadows.apotheosis.spawn.modifiers;

import com.google.gson.JsonObject;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SingleItemRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.spawn.SpawnerModifiers;

public class ModifierSync {
	public static final IRecipeType<ModifierRecipe> TYPE = IRecipeType.register(Apotheosis.MODID + ":spawner_modifiers");
	public static final Serializer SERIALIZER = new Serializer();

	public static class ModifierRecipe extends SingleItemRecipe {

		protected final SpawnerModifier modif;

		public ModifierRecipe(SpawnerModifier modif) {
			super(TYPE, SERIALIZER, new ResourceLocation(Apotheosis.MODID, modif.getId()), "", Ingredient.EMPTY, ItemStack.EMPTY);
			this.modif = modif;
		}

		@Override
		public boolean matches(IInventory inv, World worldIn) {
			return false;
		}

	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ModifierRecipe> {
		@Override
		public ModifierRecipe read(ResourceLocation recipeId, JsonObject json) {
			return null;
		}

		@Override
		public void write(PacketBuffer buffer, ModifierRecipe recipe) {
			buffer.writeString(recipe.modif.getId());
			recipe.modif.getIngredient().write(buffer);
			buffer.writeInt(recipe.modif.value);
			buffer.writeInt(recipe.modif.min);
			buffer.writeInt(recipe.modif.max);
		}

		@Override
		public ModifierRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			SpawnerModifier modif = SpawnerModifiers.MODIFIERS.get(buffer.readString(50));
			modif.sync(Ingredient.read(buffer), buffer.readInt(), buffer.readInt(), buffer.readInt());
			return new ModifierRecipe(modif);
		}

	}
}
