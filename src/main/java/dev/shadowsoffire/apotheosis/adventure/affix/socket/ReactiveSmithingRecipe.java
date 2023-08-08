package dev.shadowsoffire.apotheosis.adventure.affix.socket;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ReactiveSmithingRecipe {

    public void onCraft(Container inv, Player player, ItemStack output);

}
