package shadows.apotheosis.ench.compat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

@JeiPlugin
public class EnchJEIPlugin implements IModPlugin {

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
		reg.addIngredientInfo(new ItemStack(ApotheosisObjects.PRISMATIC_ALTAR), VanillaTypes.ITEM, new TranslationTextComponent("info.apotheosis.altar"));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, "enchantment");
	}

}