package shadows.ench.compat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;
import shadows.ench.EnchModule;

@JEIPlugin
public class EnchJEIPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry reg) {
		if (!Apotheosis.enableEnch) return;
		ItemStack enchDiaSword = new ItemStack(Items.DIAMOND_SWORD);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SHARPNESS, 1), enchDiaSword);
		ItemStack cursedDiaSword = new ItemStack(Items.DIAMOND_SWORD);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.BINDING_CURSE, 1), cursedDiaSword);
		ItemStack scrapTome = new ItemStack(ApotheosisObjects.SCRAP_TOME);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SHARPNESS, 1), scrapTome);
		IVanillaRecipeFactory factory = reg.getJeiHelpers().getVanillaRecipeFactory();
		//Formatter::off
		if(EnchModule.allowWeb)
		reg.addRecipes(ImmutableList.of(
			factory.createAnvilRecipe(
				enchDiaSword,
				ImmutableList.of(new ItemStack(Blocks.WEB)),
				ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD))),
			factory.createAnvilRecipe(
				cursedDiaSword,
				ImmutableList.of(new ItemStack(ApotheosisObjects.PRISMATIC_WEB)),
				ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD))),
			factory.createAnvilRecipe(
				enchDiaSword,
				ImmutableList.of(new ItemStack(ApotheosisObjects.SCRAP_TOME)),
				ImmutableList.of(scrapTome)),
			factory.createAnvilRecipe(
				new ItemStack(Blocks.ANVIL, 1, 1),
				ImmutableList.of(new ItemStack(Blocks.IRON_BLOCK)),
				ImmutableList.of(new ItemStack(Blocks.ANVIL)))),
			VanillaRecipeCategoryUid.ANVIL);
		//Formatter::on
		reg.addIngredientInfo(new ItemStack(Blocks.ENCHANTING_TABLE), VanillaTypes.ITEM, "info.apotheosis.enchanting");
		reg.addIngredientInfo(new ItemStack(ApotheosisObjects.PRISMATIC_ALTAR), VanillaTypes.ITEM, "info.apotheosis.altar");
	}

}
