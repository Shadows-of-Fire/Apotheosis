package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.EnumMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingMenu;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

record GemUpgradeMatch(PurityUpgradeRecipe recipe, int leftSlot, int rightSlot, SizedIngredient leftIng, SizedIngredient rightIng) {

    void execute(Container matInv, EnumMap<Purity, Integer> map) {
        map.put(recipe.purity(), map.get(recipe.purity()) - 2);
        map.put(recipe.purity().next(), map.get(recipe.purity().next()) + 1);
        matInv.removeItem(leftSlot, leftIng.count());
        matInv.removeItem(rightSlot, rightIng.count());
        matInv.setChanged();
    }

    @Nullable
    static GemUpgradeMatch findMatch(Level level, Purity purity, EnumMap<Purity, Integer> map, Container matInv) {
        Purity prev = Purity.values()[purity.ordinal() - 1];
        if (map.get(prev) < 2) return null;

        List<RecipeHolder<GemCuttingRecipe>> recipes = GemCuttingMenu.getRecipes(level);

        for (RecipeHolder<GemCuttingRecipe> holder : recipes) {
            if (holder.value() instanceof PurityUpgradeRecipe rec && rec.purity() == prev) {
                int leftSlot = -1, rightSlot = -1;
                SizedIngredient leftIng = null, rightIng = null;

                for (int i = 0; i < matInv.getContainerSize(); i++) {
                    ItemStack stack = matInv.getItem(i);
                    if (stack.isEmpty()) continue;
                    if (leftIng == null) {
                        leftIng = GemCuttingRecipe.getMatch(stack, rec.left());
                        if (leftIng != null) {
                            leftSlot = i;
                            continue;
                        }
                    }

                    if (rightIng == null) {
                        rightIng = GemCuttingRecipe.getMatch(stack, rec.right());
                        if (rightIng != null) {
                            rightSlot = i;
                            continue;
                        }
                    }
                }

                if (leftIng == null || rightIng == null) continue;

                return new GemUpgradeMatch(rec, leftSlot, rightSlot, leftIng, rightIng);
            }
        }

        return null;
    }
}
