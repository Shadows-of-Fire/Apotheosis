package dev.shadowsoffire.apotheosis.socket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface ReactiveSmithingRecipe {

    /**
     * Reacts to the crafting of a smithing recipe. This is called when {@link Slot#onTake} is called after the recipe output is picked up.
     * <p>
     * Always runs on the logical server: as of 26.1, {@code SmithingMenu.createResult} skips the recipe lookup on the client,
     * so {@code RecipeCraftingHolder#getRecipeUsed} is only ever non-null on the server side. Implementations must perform any
     * client-facing effects (sounds, particles, etc.) via server-authoritative broadcasts.
     * <p>
     * Due to unfortunate design decisions by Mojang, when shift-click-crafting, the output will be {@link ItemStack#EMPTY} and is not mutable.
     *
     * @param inv    The smithing menu input inventory
     * @param player The crafting player (a {@link ServerPlayer})
     * @param output The output stack, when crafting normally; otherwise {@link ItemStack#EMPTY}
     */
    public void onCraft(Container inv, ServerPlayer player, ItemStack output);

}
