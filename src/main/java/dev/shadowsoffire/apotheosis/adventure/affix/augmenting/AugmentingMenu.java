package dev.shadowsoffire.apotheosis.adventure.affix.augmenting;

import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class AugmentingMenu extends BlockEntityMenu<AugmentingTableTile> {

    protected final Player player;
    protected InternalItemHandler itemInv = new InternalItemHandler(1);

    public AugmentingMenu(int id, Inventory inv, BlockPos pos) {
        super(Adventure.Menus.AUGMENTING.get(), id, inv, pos);
        this.player = inv.player;

        this.addSlot(new UpdatingSlot(this.itemInv, 0, 9, 9, AffixHelper::hasAffixes){
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack pStack) {
                return 1;
            }
        });

        this.addSlot(new UpdatingSlot(this.tile.inv, 0, 9, 34, stack -> stack.getItem() == Items.SIGIL_OF_ENHANCEMENT.get()));

        this.addPlayerSlots(inv, 8, 118);

        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && AffixHelper.hasAffixes(stack), 0, 1);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.getItem() == Items.SIGIL_OF_ENHANCEMENT.get(), 1, 2);
        this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9, true);
        this.registerInvShuffleRules();
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.clearContainer(pPlayer, new RecipeWrapper(this.itemInv));
    }

}
