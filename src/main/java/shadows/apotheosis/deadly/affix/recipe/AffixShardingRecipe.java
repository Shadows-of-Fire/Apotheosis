package shadows.apotheosis.deadly.affix.recipe;

import java.util.Locale;

import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.deadly.affix.AffixHelper;

public class AffixShardingRecipe extends SoulfireCookingRecipe {

	public static final RecipeSerializer<AffixShardingRecipe> SERIALIZER = new Serializer();

	protected final LootRarity rarity;
	protected final Ingredient ing;

	public AffixShardingRecipe(ResourceLocation id, LootRarity rarity) {
		super(id, "", Ingredient.EMPTY, new ItemStack(DeadlyModule.RARITY_SHARDS.get(rarity)), 0, 200);
		this.rarity = rarity;
		ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
		AffixHelper.addLore(stack, new TranslatableComponent("info.apotheosis.any_of_rarity", new TranslatableComponent("rarity.apoth." + rarity.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(rarity.getColor()))));
		this.ing = Ingredient.of(stack);
	}

	@Override
	protected boolean matches(ItemStack stack) {
		return AffixHelper.getRarity(stack) == this.rarity;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, this.ing);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AffixShardingRecipe> {

		@Override
		public AffixShardingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			LootRarity rarity = LootRarity.valueOf(GsonHelper.getAsString(json, "rarity"));
			return new AffixShardingRecipe(recipeId, rarity);
		}

		@Override
		public AffixShardingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			LootRarity rarity = LootRarity.values()[buffer.readByte()];
			return new AffixShardingRecipe(recipeId, rarity);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AffixShardingRecipe recipe) {
			buffer.writeByte(recipe.rarity.ordinal());
		}
	}

}
