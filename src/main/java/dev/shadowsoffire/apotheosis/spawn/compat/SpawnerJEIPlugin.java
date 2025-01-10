package dev.shadowsoffire.apotheosis.spawn.compat;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.spawn.SpawnerModule;
import dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerModifier;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

@JeiPlugin
public class SpawnerJEIPlugin implements IModPlugin {

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        if (!Apotheosis.enableSpawner) return;
        List<SpawnerModifier> recipes = new ArrayList<>(Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeTypes.MODIFIER));
        recipes.sort((r1, r2) -> r1.getOffhandInput() == Ingredient.EMPTY ? r2.getOffhandInput() == Ingredient.EMPTY ? 0 : -1 : 1);

        reg.addRecipes(SpawnerCategory.TYPE, recipes);
        if (SpawnerModule.spawnerSilkLevel == -1) {
            reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.spawner.no_silk"));
        }
        else if (SpawnerModule.spawnerSilkLevel == 0) {
            reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.spawner.always_drop"));
        }
        else reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK,
            Component.translatable("info.apotheosis.spawner", ((MutableComponent) Enchantments.SILK_TOUCH.getFullname(SpawnerModule.spawnerSilkLevel)).withStyle(ChatFormatting.DARK_BLUE).getString()));
        if (SpawnerModule.enableCapturingEnchantmentJeiInfo) {
            for (Item i : ForgeRegistries.ITEMS) {
                if (i instanceof SpawnEggItem)
                    reg.addIngredientInfo(new ItemStack(i), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.capturing"));
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        if (!Apotheosis.enableSpawner) return;
        reg.addRecipeCatalyst(new ItemStack(Blocks.SPAWNER), SpawnerCategory.TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        if (!Apotheosis.enableSpawner) return;
        reg.addRecipeCategories(new SpawnerCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Apotheosis.MODID, "spawner");
    }

}
