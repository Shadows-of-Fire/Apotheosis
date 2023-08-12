package dev.shadowsoffire.apotheosis.ench.compat;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.Ench;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

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

        reg.addRecipes(RecipeTypes.ANVIL, ImmutableList.of(
            factory.createAnvilRecipe(
                enchDiaSword,
                ImmutableList.of(new ItemStack(Blocks.COBWEB)),
                ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD))),
            factory.createAnvilRecipe(
                cursedDiaSword,
                ImmutableList.of(new ItemStack(dev.shadowsoffire.apotheosis.ench.Ench.Items.PRISMATIC_WEB.get())),
                ImmutableList.of(new ItemStack(Items.DIAMOND_SWORD))),
            factory.createAnvilRecipe(
                enchDiaSword,
                ImmutableList.of(new ItemStack(dev.shadowsoffire.apotheosis.ench.Ench.Items.SCRAP_TOME.get())),
                ImmutableList.of(enchBook)),
            factory.createAnvilRecipe(
                new ItemStack(Blocks.DAMAGED_ANVIL),
                ImmutableList.of(new ItemStack(Blocks.IRON_BLOCK)),
                ImmutableList.of(new ItemStack(Blocks.ANVIL)))));

        reg.addIngredientInfo(new ItemStack(Blocks.ENCHANTING_TABLE), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.enchanting"));
        reg.addIngredientInfo(new ItemStack(Ench.Blocks.LIBRARY.get()), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.library"));
        List<EnchantingRecipe> recipes = new ArrayList<>(Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(dev.shadowsoffire.apotheosis.Apoth.RecipeTypes.INFUSION));
        recipes.sort((r1, r2) -> Float.compare(r1.getRequirements().eterna(), r2.getRequirements().eterna()));
        reg.addRecipes(EnchantingCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        if (!Apotheosis.enableEnch) return;
        reg.addRecipeCatalyst(new ItemStack(Blocks.ENCHANTING_TABLE), EnchantingCategory.TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        if (!Apotheosis.enableEnch) return;
        reg.addRecipeCategories(new EnchantingCategory(reg.getJeiHelpers().getGuiHelper()));
    }

}
