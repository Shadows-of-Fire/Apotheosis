package dev.shadowsoffire.apotheosis.village.fletching;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FletchingScreen extends AbstractContainerScreen<FletchingContainer> {
    public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/fletching_table.png");

    public FletchingScreen(FletchingContainer container, Inventory player, Component title) {
        super(container, player, title);
        this.titleLabelX = 47;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics gfx, int x, int y, float partialTicks) {
        this.renderBackground(gfx);
        super.render(gfx, x, y, partialTicks);
        this.renderTooltip(gfx, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }
}
