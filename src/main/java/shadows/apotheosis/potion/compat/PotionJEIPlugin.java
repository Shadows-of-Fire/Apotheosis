package shadows.apotheosis.potion.compat;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Size2i;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.potion.PotionCharmItem;
import shadows.apotheosis.potion.PotionCharmRecipe;

@JeiPlugin
public class PotionJEIPlugin implements IModPlugin {

	ICraftingGridHelper gridHelper;

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enablePotion) return;
		this.gridHelper = reg.getJeiHelpers().getGuiHelper().createCraftingGridHelper(1);
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration reg) {
		if (!Apotheosis.enablePotion) return;
		reg.getCraftingCategory().addCategoryExtension(PotionCharmRecipe.class, PotionCharmRecipeWrapper::new);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration reg) {
		if (!Apotheosis.enablePotion) return;
		reg.registerSubtypeInterpreter(ApotheosisObjects.POTION_CHARM, new PotionCharmSubtypes());
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "potion");
	}

	private class PotionCharmRecipeWrapper implements ICustomCraftingCategoryExtension {

		private final PotionCharmRecipe recipe;

		PotionCharmRecipeWrapper(PotionCharmRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void setIngredients(IIngredients ingredients) {
			ingredients.setInputIngredients(this.recipe.getIngredients());
			ingredients.setOutput(VanillaTypes.ITEM, this.recipe.getRecipeOutput());
		}

		@Override
		public ResourceLocation getRegistryName() {
			return this.recipe.getId();
		}

		@Override
		public Size2i getSize() {
			return new Size2i(3, 3);
		}

		@Override
		public void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients) {
			IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
			ItemStack focus = recipeLayout.getFocus(VanillaTypes.ITEM).getValue();
			Potion potion = PotionUtils.getPotionFromItem(focus);
			List<List<ItemStack>> recipeInputs = ingredients.getInputs(VanillaTypes.ITEM);
			List<List<ItemStack>> clones = new ArrayList<>();
			recipeInputs.forEach(l -> {
				List<ItemStack> cloneList = new ArrayList<>();
				l.stream().map(ItemStack::copy).map(s -> PotionUtils.addPotionToItemStack(s, potion)).forEach(cloneList::add);
				clones.add(cloneList);
			});
			ItemStack output = new ItemStack(ApotheosisObjects.POTION_CHARM);
			PotionUtils.addPotionToItemStack(output, potion);
			Size2i size = this.getSize();
			PotionJEIPlugin.this.gridHelper.setInputs(guiItemStacks, clones, size.width, size.height);
			guiItemStacks.set(0, output);
		}

	}

	private class PotionCharmSubtypes implements ISubtypeInterpreter {

		@Override
		public String apply(ItemStack stack) {
			return ISubtypeInterpreter.NONE;
		}

		@Override
		public String apply(ItemStack stack, UidContext context) {
			if (context != UidContext.Recipe) {
				if (!PotionCharmItem.hasPotion(stack)) return ISubtypeInterpreter.NONE;
				Potion p = PotionUtils.getPotionFromItem(stack);
				EffectInstance contained = p.getEffects().get(0);
				return contained.getPotion().getRegistryName() + "@" + contained.getAmplifier() + "@" + contained.getDuration();
			}
			return ISubtypeInterpreter.NONE;
		}

	}

}