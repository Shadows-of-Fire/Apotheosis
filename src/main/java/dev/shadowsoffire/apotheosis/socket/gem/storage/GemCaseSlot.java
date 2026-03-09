package dev.shadowsoffire.apotheosis.socket.gem.storage;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GemCaseSlot extends Slot {

    private static Container emptyContainer = new SimpleContainer(0);

    private final GemCaseMenu menu;
    final Purity purity;

    public GemCaseSlot(GemCaseMenu menu, Purity purity, int x, int y) {
        super(emptyContainer, -1, x, y);
        this.menu = menu;
        this.purity = purity;
    }

    public void onTake(Player player, ItemStack stack) {
        if (!stack.isEmpty()) { // Technically empty should trigger some warnings, but shift-click always submits an empty stack.
            DynamicHolder<Gem> gem = GemItem.getGem(stack);
            Purity purity = GemItem.getPurity(stack);
            if (!gem.isBound() || gem.get() != this.menu.selectedGem || purity != this.purity) {
                Apotheosis.LOGGER.warn("Player {} tried to take a gem that doesn't match the selected gem or purity! (gem: {}, purity: {})", player.getName().getString(), gem.getId(), purity);
                return;
            }
            this.menu.extractGem(this.purity, stack.getCount());
        }
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
        ItemStack stack = GemItem.createStack(gem, purity, Math.min(count, 64));
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

    /**
     * This remove impl is not able to actually do the removals, and instead relies on the eventual call to `onTake` to do that.
     */
    public ItemStack remove(int amount) {
        Gem gem = this.menu.selectedGem;
        if (gem == null) {
            return ItemStack.EMPTY;
        }

        int count = this.menu.getGemCount(gem, purity);
        int toExtract = Math.min(count, amount);
        if (toExtract <= 0) {
            return ItemStack.EMPTY;
        }
        return GemItem.createStack(gem, purity, toExtract);
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
