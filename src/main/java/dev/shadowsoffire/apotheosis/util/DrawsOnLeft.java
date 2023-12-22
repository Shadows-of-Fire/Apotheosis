package dev.shadowsoffire.apotheosis.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

/**
 * Implement this on a screen class to be able to call {@link #drawOnLeft(GuiGraphics, List, int)}
 */
public interface DrawsOnLeft {

    /**
     * Renders a list of text as a tooltip attached to the left edge of the currently open container screen.
     */
    default void drawOnLeft(GuiGraphics gfx, List<Component> list, int y) {
        if (list.isEmpty()) return;
        int xPos = ths().getGuiLeft() - 16 - list.stream().map(ths().font::width).max(Integer::compare).get();
        int maxWidth = 9999;
        if (xPos < 0) {
            maxWidth = ths().getGuiLeft() - 6;
            xPos = -8;
        }

        List<FormattedText> split = new ArrayList<>();
        int lambdastupid = maxWidth;
        list.forEach(comp -> split.addAll(ths().font.getSplitter().splitLines(comp, lambdastupid, comp.getStyle())));

        gfx.renderComponentTooltip(ths().font, split, xPos, y, ItemStack.EMPTY);
    }

    default AbstractContainerScreen<?> ths() {
        return (AbstractContainerScreen<?>) this;
    }

    public static void draw(AbstractContainerScreen<?> screen, GuiGraphics gfx, List<Component> list, int y) {
        ((DrawsOnLeft) screen).drawOnLeft(gfx, list, y);
    }

}
