package shadows.apotheosis.potion.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Size2i;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.potion.PotionCharmRecipe;

@JeiPlugin
public class PotionJEIPlugin implements IModPlugin {

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enablePotion) return;
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration reg) {
		if (!Apotheosis.enablePotion) return;
		reg.getCraftingCategory().addCategoryExtension(PotionCharmRecipe.class, PotionCharmRecipeWrapper::new);
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "potion");
	}

	private class PotionCharmRecipeWrapper implements ICraftingCategoryExtension {

		private final PotionCharmRecipe recipe;

		PotionCharmRecipeWrapper(PotionCharmRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void setIngredients(IIngredients ing) {
			ing.setInputLists(VanillaTypes.ITEM, recipe.getIngredients().stream().map(i -> i.getMatchingStacks()).map(a -> Arrays.asList(a)).collect(Collectors.toList()));
			List<ItemStack> outputs = new ArrayList<>();
			for (ItemStack s : recipe.getIngredients().get(4).getMatchingStacks()) {
				ItemStack out = new ItemStack(ApotheosisObjects.POTION_CHARM);
				PotionUtils.addPotionToItemStack(out, PotionUtils.getPotionFromItem(s));
				AffixHelper.addLore(out, new TranslationTextComponent("info.apotheosis.req_3_potions"));
				outputs.add(out);
			}
			ing.setOutputLists(VanillaTypes.ITEM, Arrays.asList(outputs));
		}

		@Override
		public ResourceLocation getRegistryName() {
			return recipe.getId();
		}

		@Override
		public Size2i getSize() {
			return new Size2i(3, 3);
		}

	}

}