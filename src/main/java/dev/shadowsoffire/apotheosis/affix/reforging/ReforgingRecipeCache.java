package dev.shadowsoffire.apotheosis.affix.reforging;

import java.util.Collection;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

/**
 * Client-side cache of {@link ReforgingRecipe} instances. Populated from the server-pushed recipe
 * sync when reloading, since custom recipe types aren't included in the vanilla recipe packet.
 */
public final class ReforgingRecipeCache {

    private static volatile List<RecipeHolder<ReforgingRecipe>> RECIPES = List.of();

    private ReforgingRecipeCache() {}

    public static void rebuildFromMap(RecipeMap map) {
        Collection<RecipeHolder<ReforgingRecipe>> holders = map.byType(RecipeTypes.REFORGING);
        RECIPES = List.copyOf(holders);
    }

    public static void clear() {
        RECIPES = List.of();
    }

    public static List<RecipeHolder<ReforgingRecipe>> all() {
        return RECIPES;
    }
}
