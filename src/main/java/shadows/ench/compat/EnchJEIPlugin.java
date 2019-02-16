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
import shadows.ApotheosisObjects;
import shadows.ench.EnchModule;
import shadows.placebo.util.PlaceboUtil;

@JEIPlugin
public class EnchJEIPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry reg) {
		ItemStack s = new ItemStack(Items.DIAMOND_SWORD);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SHARPNESS, 1), s);
		ItemStack s2 = new ItemStack(Items.DIAMOND_SWORD);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.BINDING_CURSE, 1), s2);
		IVanillaRecipeFactory factory = reg.getJeiHelpers().getVanillaRecipeFactory();
		reg.addRecipes(ImmutableList.of(
				factory.createAnvilRecipe(s, ImmutableList.of(new ItemStack(Blocks.WEB)), ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD))),
				factory.createAnvilRecipe(s2, ImmutableList.of(new ItemStack(ApotheosisObjects.PRISMATIC_WEB)), ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD))),
				factory.createAnvilRecipe(new ItemStack(Blocks.ANVIL, 1, 1), PlaceboUtil.asList(EnchModule.blockIron.getMatchingStacks()), ImmutableList.of(new ItemStack(Blocks.ANVIL)))
				), VanillaRecipeCategoryUid.ANVIL);
		reg.addIngredientInfo(new ItemStack(Blocks.ENCHANTING_TABLE), VanillaTypes.ITEM, "info.apotheosis.enchanting");
	}

}
