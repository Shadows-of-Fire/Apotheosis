package shadows.apotheosis.potion.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.compat.EnchantingCategory;
import shadows.apotheosis.ench.table.EnchantingRecipe;
import shadows.apotheosis.potion.PotionCharmItem;
import shadows.apotheosis.potion.PotionCharmRecipe;
import shadows.apotheosis.potion.PotionEnchantingRecipe;

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
		EnchantingCategory.registerExtension(PotionEnchantingRecipe.class, new PotionCharmEnchantingWrapper());
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration reg) {
		if (!Apotheosis.enablePotion) return;
		reg.registerSubtypeInterpreter(Apoth.Items.POTION_CHARM, new PotionCharmSubtypes());
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
		public ResourceLocation getRegistryName() {
			return this.recipe.getId();
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
			Potion potion = PotionUtils.getPotion(focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst().map(IFocus::getTypedValue).map(ITypedIngredient::getIngredient).orElse(ItemStack.EMPTY));
			List<List<ItemStack>> recipeInputs = this.recipe.getIngredients().stream().map(i -> Arrays.asList(i.getItems())).collect(Collectors.toCollection(ArrayList::new));
			if (potion != Potions.EMPTY) {
				for (int i : this.recipe.getPotionSlots()) {
					recipeInputs.set(i, Arrays.asList(PotionUtils.setPotion(new ItemStack(Items.POTION), potion)));
				}
			}
			ItemStack output = new ItemStack(Apoth.Items.POTION_CHARM);
			PotionUtils.setPotion(output, potion);
			craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, recipeInputs, 3, 3);
			if (potion != Potions.EMPTY) {
				craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, Arrays.asList(output));
			} else {
				List<ItemStack> potionStacks = new ArrayList<>();
				for (Potion p : ForgeRegistries.POTIONS) {
					if (p.getEffects().size() != 1 || p.getEffects().get(0).getEffect().isInstantenous()) continue;
					ItemStack charm = new ItemStack(Apoth.Items.POTION_CHARM);
					PotionUtils.setPotion(charm, p);
					potionStacks.add(charm);
				}
				craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, potionStacks);
			}
		}

	}

	private class PotionCharmEnchantingWrapper implements EnchantingCategory.Extension<PotionEnchantingRecipe> {

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, IRecipeSlotBuilder input, IRecipeSlotBuilder output, EnchantingRecipe recipe, IFocusGroup focuses) {
			Potion potion = PotionUtils.getPotion(focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst().map(IFocus::getTypedValue).map(ITypedIngredient::getIngredient).orElse(ItemStack.EMPTY));
			if (potion != Potions.EMPTY) {
				ItemStack out = new ItemStack(Apoth.Items.POTION_CHARM);
				PotionUtils.setPotion(out, potion);
				ItemStack in = out.copy();
				out.getOrCreateTag().putBoolean("Unbreakable", true);
				input.addIngredient(VanillaTypes.ITEM_STACK, in);
				output.addIngredient(VanillaTypes.ITEM_STACK, out);
			} else {
				List<ItemStack> potionStacks = new ArrayList<>();
				List<ItemStack> unbreakable = new ArrayList<>();
				for (Potion p : ForgeRegistries.POTIONS) {
					if (p.getEffects().size() != 1 || p.getEffects().get(0).getEffect().isInstantenous()) continue;
					ItemStack charm = new ItemStack(Apoth.Items.POTION_CHARM);
					PotionUtils.setPotion(charm, p);
					potionStacks.add(charm);
					ItemStack copy = charm.copy();
					copy.getOrCreateTag().putBoolean("Unbreakable", true);
					unbreakable.add(copy);
				}
				input.addIngredients(VanillaTypes.ITEM_STACK, potionStacks);
				output.addIngredients(VanillaTypes.ITEM_STACK, unbreakable);
			}
			builder.createFocusLink(input, output);
		}

	}

	private class PotionCharmSubtypes implements IIngredientSubtypeInterpreter<ItemStack> {

		@Override
		public String apply(ItemStack stack, UidContext context) {
			if (context != UidContext.Recipe) {
				if (!PotionCharmItem.hasPotion(stack)) return NONE;
				Potion p = PotionUtils.getPotion(stack);
				MobEffectInstance contained = p.getEffects().get(0);
				return contained.getEffect().getRegistryName() + "@" + contained.getAmplifier() + "@" + contained.getDuration();
			}
			return NONE;
		}

	}

}