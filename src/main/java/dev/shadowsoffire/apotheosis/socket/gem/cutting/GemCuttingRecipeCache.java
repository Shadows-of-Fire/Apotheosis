package dev.shadowsoffire.apotheosis.socket.gem.cutting;

import java.util.Collection;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

/**
 * Client-side cache of {@link GemCuttingRecipe} instances. Populated from the server-pushed recipe
 * sync when reloading, since custom recipe types aren't included in the vanilla recipe packet.
 */
public final class GemCuttingRecipeCache {

    private static volatile List<RecipeHolder<GemCuttingRecipe>> RECIPES = List.of();

    private GemCuttingRecipeCache() {}

    public static void rebuildFromMap(RecipeMap map) {
        Collection<RecipeHolder<GemCuttingRecipe>> holders = map.byType(RecipeTypes.GEM_CUTTING);
        RECIPES = List.copyOf(holders);
    }

    public static void clear() {
        RECIPES = List.of();
    }

    public static List<RecipeHolder<GemCuttingRecipe>> all() {
        return RECIPES;
    }
}
