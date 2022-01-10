package shadows.apotheosis.ench.table;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.ench.table.EnchantingStatManager.Stats;

public class EnchantingRecipe implements Recipe<Container> {

	public static final RecipeType<EnchantingRecipe> TYPE = RecipeType.register("apotheosis:enchanting");
	public static final Serializer SERIALIZER = new Serializer();

	protected final ResourceLocation id;
	protected final ItemStack output;
	protected final Ingredient input;
	protected final Stats requirements;
	protected final int displayLevel;

	/**
	 * Defines an Enchanting Recipe.
	 * @param id The Recipe ID
	 * @param output The output ItemStack
	 * @param input The input Ingredient
	 * @param requirements The Level, Quanta, and Arcana requirements respectively.
	 * @param displayLevel The level to show on the fake "Infusion" Enchantment that will show up.
	 */
	public EnchantingRecipe(ResourceLocation id, ItemStack output, Ingredient input, Stats requirements, int displayLevel) {
		this.id = id;
		this.output = output;
		this.input = input;
		this.requirements = requirements;
		this.displayLevel = displayLevel;
	}

	public boolean matches(ItemStack input, float eterna, float quanta, float arcana) {
		return this.input.test(input) && eterna >= requirements.eterna && quanta >= requirements.quanta && arcana >= requirements.arcana;
	}

	public Stats getRequirements() {
		return this.requirements;
	}

	public Ingredient getInput() {
		return this.input;
	}

	public int getDisplayLevel() {
		return this.displayLevel;
	}

	@Override
	public boolean matches(Container pContainer, Level pLevel) {
		return false;
	}

	@Override
	public ItemStack assemble(Container pContainer) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return output;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return TYPE;
	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<EnchantingRecipe> {

		private static final Gson GSON = new GsonBuilder().create();

		@Override
		public EnchantingRecipe fromJson(ResourceLocation id, JsonObject obj) {
			ItemStack output = CraftingHelper.getItemStack(obj.get("result").getAsJsonObject(), true, true);
			Ingredient input = Ingredient.fromJson(obj.get("input"));
			Stats stats = GSON.fromJson(obj.get("requirements"), Stats.class);
			int displayLevel = obj.get("display_level").getAsInt();
			return new EnchantingRecipe(id, output, input, stats, displayLevel);
		}

		@Override
		public EnchantingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			ItemStack output = buf.readItem();
			Ingredient input = Ingredient.fromNetwork(buf);
			Stats stats = Stats.read(buf);
			int displayLevel = buf.readByte();
			return new EnchantingRecipe(id, output, input, stats, displayLevel);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, EnchantingRecipe recipe) {
			buf.writeItem(recipe.output);
			recipe.input.toNetwork(buf);
			recipe.requirements.write(buf);
			buf.writeByte(recipe.displayLevel);
		}

	}

}
