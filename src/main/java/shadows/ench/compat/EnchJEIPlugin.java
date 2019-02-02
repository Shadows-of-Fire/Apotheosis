package shadows.ench.compat;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class EnchJEIPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry reg) {
		ItemStack s = new ItemStack(Items.DIAMOND_SWORD);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SHARPNESS, 1), s);
		reg.addRecipes(Collections.singleton(reg.getJeiHelpers().getVanillaRecipeFactory().createAnvilRecipe(s, ImmutableList.of(new ItemStack(Blocks.WEB)), ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD)))), VanillaRecipeCategoryUid.ANVIL);
		reg.addIngredientInfo(new ItemStack(Blocks.ENCHANTING_TABLE), VanillaTypes.ITEM, "info.apotheosis.enchanting");
	}

}
