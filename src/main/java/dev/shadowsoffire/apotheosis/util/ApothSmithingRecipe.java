package dev.shadowsoffire.apotheosis.util;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public abstract class ApothSmithingRecipe implements SmithingRecipe {

    public static final int TEMPLATE = 0, BASE = 1, ADDITION = 2;

    /**
     * Placeholder ingredient for recipes where the base match is handled by a custom {@link #matches(SmithingRecipeInput, Level)} override.
     * 26.1 forbids empty Ingredients, so subclasses that historically passed {@code Ingredient.of()} pass this instead.
     * <p>
     * The smthing menu relies on {@link Ingredient#items()} to determine what may be placed in the input slot, so we need to make this placeholder accept anything
     * to trick it.
     */
    public static final Ingredient BASE_PLACEHOLDER = new Ingredient(new AffixItemIngredient(RarityRegistry.INSTANCE.emptyHolder()));

    protected final Ingredient base;
    protected final Ingredient addition;
    protected final ItemStack result;

    public ApothSmithingRecipe(Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
        this.base = pBase;
        this.addition = pAddition;
        this.result = pResult;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.empty();
    }

    @Override
    public Ingredient baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.of(this.addition);
    }

}
