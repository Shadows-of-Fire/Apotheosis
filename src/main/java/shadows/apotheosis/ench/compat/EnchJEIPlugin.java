package shadows.apotheosis.ench.compat;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.table.EnchantingRecipe;

@JeiPlugin
public class EnchJEIPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "enchantment");
	}

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enableEnch) return;
		ItemStack enchDiaSword = new ItemStack(Items.DIAMOND_SWORD);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SHARPNESS, 1), enchDiaSword);
		ItemStack cursedDiaSword = new ItemStack(Items.DIAMOND_SWORD);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.BINDING_CURSE, 1), cursedDiaSword);
		ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SHARPNESS, 1), enchBook);
		IVanillaRecipeFactory factory = reg.getVanillaRecipeFactory();
		//Formatter::off
		reg.addRecipes(ImmutableList.of(
			factory.createAnvilRecipe(
				enchDiaSword,
				ImmutableList.of(new ItemStack(Blocks.COBWEB)),
				ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD))),
			factory.createAnvilRecipe(
				cursedDiaSword,
				ImmutableList.of(new ItemStack(ApotheosisObjects.PRISMATIC_WEB)),
				ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD))),
			factory.createAnvilRecipe(
				enchDiaSword,
				ImmutableList.of(new ItemStack(ApotheosisObjects.SCRAP_TOME)),
				ImmutableList.of(enchBook)),
			factory.createAnvilRecipe(
				new ItemStack(Blocks.DAMAGED_ANVIL),
				ImmutableList.of(new ItemStack(Blocks.IRON_BLOCK)),
				ImmutableList.of(new ItemStack(Blocks.ANVIL)))),
			VanillaRecipeCategoryUid.ANVIL);
		//Formatter::on
		reg.addIngredientInfo(new ItemStack(Blocks.ENCHANTING_TABLE), VanillaTypes.ITEM, new TranslationTextComponent("info.apotheosis.enchanting"));
		reg.addIngredientInfo(new ItemStack(ApotheosisObjects.ENCHANTMENT_LIBRARY), VanillaTypes.ITEM, new TranslationTextComponent("info.apotheosis.library"));
		List<EnchantingRecipe> recipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(EnchantingRecipe.TYPE);
		recipes.sort((r1, r2) -> Float.compare(r1.getRequirements().eterna, r2.getRequirements().eterna));
		reg.addRecipes(recipes, EnchantingCategory.UID);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration reg) {
		reg.registerSubtypeInterpreter(ApotheosisObjects.HELLSHELF.asItem(), new ShelfSubtypes(stk -> EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.HELL_INFUSION, stk)));
		reg.registerSubtypeInterpreter(ApotheosisObjects.SEASHELF.asItem(), new ShelfSubtypes(stk -> EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.SEA_INFUSION, stk)));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		if (!Apotheosis.enableEnch) return;
		reg.addRecipeCatalyst(new ItemStack(Blocks.ENCHANTING_TABLE), EnchantingCategory.UID);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		if (!Apotheosis.enableEnch) return;
		reg.addRecipeCategories(new EnchantingCategory(reg.getJeiHelpers().getGuiHelper()));
	}

	private class ShelfSubtypes implements IIngredientSubtypeInterpreter<ItemStack> {

		private final Function<ItemStack, Integer> getter;

		private ShelfSubtypes(Function<ItemStack, Integer> getter) {
			this.getter = getter;
		}

		@Override
		public String apply(ItemStack stack, UidContext context) {
			if (getter.apply(stack) >= 3) return "infused";
			return "std";
		}

	}

}