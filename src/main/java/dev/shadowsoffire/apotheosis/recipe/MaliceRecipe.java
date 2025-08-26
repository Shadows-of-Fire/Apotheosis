package dev.shadowsoffire.apotheosis.recipe;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.socket.ReactiveSmithingRecipe;
import dev.shadowsoffire.apotheosis.util.ApothSmithingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class MaliceRecipe extends ApothSmithingRecipe implements ReactiveSmithingRecipe {

    public MaliceRecipe() {
        super(Ingredient.EMPTY, Ingredient.of(Items.SIGIL_OF_MALICE.value()), ItemStack.EMPTY);
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(SmithingRecipeInput inv, Level level) {
        ItemStack base = inv.getItem(BASE);
        ItemStack sigils = inv.getItem(ADDITION);
        return base.getCount() == 1 && sigils.is(Items.SIGIL_OF_MALICE) && AffixHelper.getAffixes(base).size() >= 2 && !base.getOrDefault(Apoth.Components.TOUCHED_BY_MALICE, false);
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(SmithingRecipeInput inv, HolderLookup.Provider regs) {
        ItemStack base = inv.getItem(BASE).copy();
        base.set(Apoth.Components.MALICE_MARKER, true);
        return base;
    }

    @Override
    public void onCraft(Container inv, Player player, ItemStack output) {
        if (!player.level().isClientSide && !output.isEmpty()) {
            AffixHelper.applyMalice(player, output);
            output.remove(Components.MALICE_MARKER);
        }
        player.playSound(Apoth.Sounds.MALICE.value(), 1.0F, player.getRandom().nextFloat() * 0.4F + 0.8F);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Apoth.RecipeSerializers.MALICE.value();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

}
