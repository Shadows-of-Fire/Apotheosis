package dev.shadowsoffire.apotheosis.adventure.client;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class DropDownList<T> extends AbstractWidget {

    public static final int NO_SELECTION = -1;

    protected final int maxDisplayedEntries;
    protected final int baseHeight;

    protected List<T> entries;
    protected float scrollOffs;
    protected boolean scrolling;
    protected int startIndex;
    protected boolean isOpen = false;
    protected int selected = NO_SELECTION;

    public DropDownList(int x, int y, int width, int height, Component narrationMsg, List<T> entries, int maxDisplayedEntries) {
        super(x, y, width, height, narrationMsg);
        this.entries = entries;
        this.maxDisplayedEntries = maxDisplayedEntries;
        this.baseHeight = height;
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int selected = this.getSelected();
        if (selected != NO_SELECTION) {
            this.renderEntry(gfx, this.getX(), this.getY(), mouseX, mouseY, this.entries.get(selected));
        }

        if (this.isOpen) {
            for (int i = this.startIndex; i < this.startIndex + this.maxDisplayedEntries && i < this.entries.size(); i++) {
                this.renderEntry(gfx, this.getX(), this.getY() + this.baseHeight * (1 + i - this.startIndex), mouseX, mouseY, this.entries.get(i));
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.isOpen) {
            if (this.entries.isEmpty()) {
                return;
            }
            this.isOpen = true;
            this.height = this.baseHeight * (1 + Math.min(this.entries.size(), this.maxDisplayedEntries));
        }
        else {
            this.selected = this.getHoveredSlot(mouseX, mouseY);
            this.height = this.baseHeight;
            this.isOpen = false;
        }
    }

    public static boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public int getHoveredSlot(double mouseX, double mouseY) {
        if (isHovering(this.getX(), this.getY(), this.width, this.baseHeight, mouseX, mouseY)) {
            return this.getSelected();
        }

        if (this.isOpen) {
            for (int i = 0; i < this.maxDisplayedEntries; i++) {
                if (this.startIndex + i < this.entries.size()) {
                    if (isHovering(this.getX(), this.getY() + (i + 1) * this.baseHeight, this.width, this.baseHeight, mouseX, mouseY)) {
                        return this.startIndex + i;
                    }
                }
            }
        }

        return -1;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int barTop = this.getX() + 14;
            int barBot = barTop + 103;
            this.scrollOffs = ((float) pMouseY - barTop - 6F) / (barBot - barTop - 12F) - 0.12F;
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5D);
            return true;
        }
        else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.isScrollBarActive()) {
            int i = this.getOffscreenRows();
            this.scrollOffs = (float) (this.scrollOffs - pDelta / i);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * i + 0.5D);
        }
        return true;
    }

    private boolean isScrollBarActive() {
        return this.isOpen() && this.entries.size() > this.maxDisplayedEntries;
    }

    protected int getOffscreenRows() {
        return this.entries.size() - this.maxDisplayedEntries;
    }

    public int getSelected() {
        return Mth.clamp(this.selected, NO_SELECTION, this.entries.size() - 1);
    }

    public void setSelected(int selected) {
        this.selected = Mth.clamp(selected, NO_SELECTION, this.entries.size() - 1);
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    protected abstract void renderEntry(GuiGraphics gfx, int x, int y, int mouseX, int mouseY, T entry);

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    public void setEntries(List<T> entries) {
        this.entries = entries;
        this.selected = this.entries.isEmpty() ? NO_SELECTION : 0;
        this.height = this.baseHeight;
        this.startIndex = 0;
        this.isOpen = false;
    }

}
