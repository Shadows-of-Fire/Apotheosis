package dev.shadowsoffire.apotheosis.socket;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface ReactiveSmithingRecipe {

    /**
     * Reacts to the crafting of a smithing recipe. This is called when {@link Slot#onTake} is called after the recipe output is picked up.
     * <p>
     * Due to unfortunate design decisions by Mojang, when shift-click-crafting, the output will be {@link ItemStack#EMPTY} and is not mutable.
     * 
     * @param inv    The smithing menu input inventory
     * @param player The crafting player
     * @param output The output stack, when crafting normally; otherwise {@link ItemStack#EMPTY}
     */
    public void onCraft(Container inv, Player player, ItemStack output);

}
