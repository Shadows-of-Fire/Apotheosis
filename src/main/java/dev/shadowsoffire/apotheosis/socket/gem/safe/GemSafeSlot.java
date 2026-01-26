package dev.shadowsoffire.apotheosis.socket.gem.safe;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GemSafeSlot extends Slot {

    private static Container emptyContainer = new SimpleContainer(0);

    private final GemSafeMenu menu;
    final Purity purity;

    public GemSafeSlot(GemSafeMenu menu, Purity purity, int x, int y) {
        super(emptyContainer, -1, x, y);
        this.menu = menu;
        this.purity = purity;
    }

    public void onTake(Player player, ItemStack stack) {
        this.setChanged();
    }

    @Override // TODO: We could allow placement of the same gem, but for now prefer using the main input slot.
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    public ItemStack getItem() {
        Gem gem = this.menu.selectedGem;
        if (gem == null) {
            return ItemStack.EMPTY;
        }

        int count = this.menu.getGemCount(gem, purity);
        ItemStack stack = new ItemStack(Apoth.Items.GEM);
        GemItem.setGem(stack, gem);
        GemItem.setPurity(stack, purity);
        stack.setCount(Math.min(count, stack.getMaxStackSize()));
        return stack;
    }

    public boolean hasItem() {
        Gem gem = this.menu.selectedGem;
        if (gem == null) {
            return false;
        }

        return this.menu.getGemCount(gem, purity) > 0;
    }

    public void setByPlayer(ItemStack stack) {}

    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {}

    public void set(ItemStack stack) {}

    public void setChanged() {
        this.container.setChanged();
    }

    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    public int getMaxStackSize(ItemStack stack) {
        return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
    }

    public ItemStack remove(int amount) {
        Gem gem = this.menu.selectedGem;
        if (gem == null) {
            return ItemStack.EMPTY;
        }

        return this.menu.extractGem(purity, amount);
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    public boolean mayPickup(Player player) {
        return this.hasItem();
    }

    public boolean isActive() {
        Gem gem = this.menu.selectedGem;
        if (gem == null) {
            return false;
        }

        return this.purity.isAtLeast(gem.getMinPurity());
    }

    public boolean isSameInventory(Slot other) {
        return false;
    }

    public boolean allowModification(Player player) {
        return false;
    }

    public boolean isHighlightable() {
        return true;
    }

    public boolean isFake() {
        return false;
    }

}
