package shadows.apotheosis.deadly.affix.recipe;

import java.util.Locale;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.LootRarity;

public class AffixShardingRecipe extends SoulfireCookingRecipe {

	public static final IRecipeSerializer<AffixShardingRecipe> SERIALIZER = new Serializer();

	protected final LootRarity rarity;
	protected final Ingredient ing;

	public AffixShardingRecipe(ResourceLocation id, LootRarity rarity) {
		super(id, "", Ingredient.EMPTY, new ItemStack(DeadlyModule.RARITY_SHARDS.get(rarity)), 0, 200);
		this.rarity = rarity;
		ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
		AffixHelper.addLore(stack, new TranslationTextComponent("info.apotheosis.any_of_rarity", new TranslationTextComponent("rarity.apoth." + rarity.name().toLowerCase(Locale.ROOT))));
		this.ing = Ingredient.fromStacks(stack);
	}

	@Override
	protected boolean matches(ItemStack stack) {
		return AffixHelper.getRarity(stack) == this.rarity;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.from(Ingredient.EMPTY, this.ing);
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AffixShardingRecipe> {

		@Override
		public AffixShardingRecipe read(ResourceLocation recipeId, JsonObject json) {
			LootRarity rarity = LootRarity.valueOf(JSONUtils.getString(json, "rarity"));
			return new AffixShardingRecipe(recipeId, rarity);
		}

		@Override
		public AffixShardingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			LootRarity rarity = LootRarity.values()[buffer.readByte()];
			return new AffixShardingRecipe(recipeId, rarity);
		}

		@Override
		public void write(PacketBuffer buffer, AffixShardingRecipe recipe) {
			buffer.writeByte(recipe.rarity.ordinal());
		}
	}

}
