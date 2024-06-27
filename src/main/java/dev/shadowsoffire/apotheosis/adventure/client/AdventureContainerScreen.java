package dev.shadowsoffire.apotheosis.adventure.client;

import dev.shadowsoffire.apotheosis.util.DrawsOnLeft;
import dev.shadowsoffire.placebo.screen.PlaceboContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base class implementing common functionality for dark-mode menus used in the Adventure Module.
 */
public abstract class AdventureContainerScreen<T extends AbstractContainerMenu> extends PlaceboContainerScreen<T> implements DrawsOnLeft {

    public AdventureContainerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {}

    @Override
    public int getSlotColor(int index) {
        return 0x40FFFFFF;
    }

}
