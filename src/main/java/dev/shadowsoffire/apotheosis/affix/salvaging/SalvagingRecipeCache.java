package dev.shadowsoffire.apotheosis.affix.salvaging;

import java.util.Collection;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

/**
 * Client-side cache of {@link SalvagingRecipe} instances. Populated from a server-pushed sync
 * whenever recipes are reloaded, since custom recipe types aren't sent by the vanilla recipe packet.
 */
public final class SalvagingRecipeCache {

    private static volatile List<RecipeHolder<SalvagingRecipe>> RECIPES = List.of();

    private SalvagingRecipeCache() {}

    public static void rebuildFromMap(RecipeMap map) {
        Collection<RecipeHolder<SalvagingRecipe>> holders = map.byType(RecipeTypes.SALVAGING);
        RECIPES = List.copyOf(holders);
    }

    public static void replace(List<RecipeHolder<SalvagingRecipe>> recipes) {
        RECIPES = List.copyOf(recipes);
    }

    public static void clear() {
        RECIPES = List.of();
    }

    public static List<RecipeHolder<SalvagingRecipe>> findMatch(ItemStack stack) {
        return RECIPES.stream().filter(r -> r.value().getInput().test(stack)).toList();
    }

    public static List<RecipeHolder<SalvagingRecipe>> all() {
        return RECIPES;
    }
}
