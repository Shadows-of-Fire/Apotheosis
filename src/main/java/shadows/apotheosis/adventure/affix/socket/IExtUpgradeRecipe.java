package shadows.apotheosis.adventure.affix.socket;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IExtUpgradeRecipe {

    public void onCraft(Container inv, Player player, ItemStack output);

}
