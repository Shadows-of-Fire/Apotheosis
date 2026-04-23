package dev.shadowsoffire.apotheosis.client;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class SocketTooltipRenderer implements ClientTooltipComponent {

    public static final Identifier SOCKET = Apotheosis.loc("textures/gui/socket.png");

    private final SocketComponent comp;

    public SocketTooltipRenderer(SocketComponent comp) {
        this.comp = comp;
    }

    @Override
    public int getHeight(Font font) {
        return spacing(font) * this.comp.gems.size();
    }

    @Override
    public int getWidth(Font font) {
        int maxWidth = 0;
        for (GemInstance inst : this.comp.gems.gems()) {
            maxWidth = Math.max(maxWidth, font.width(getSocketDesc(inst)) + 12);
        }
        return maxWidth;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor gfx) {
        int spacing = spacing(font);
        for (int i = 0; i < this.comp.gems.size(); i++) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, SOCKET, x, y + spacing * i, 0, 0, 9, 9, 9, 9);
        }
        for (GemInstance inst : this.comp.gems()) {
            if (inst.isValid()) {
                gfx.pose().pushMatrix();
                gfx.pose().scale(0.5F, 0.5F);
                gfx.fakeItem(inst.gemStack(), 2 * x + 1, 2 * y + 1);
                gfx.pose().popMatrix();
            }
            y += spacing;
        }
    }

    @Override
    public void extractText(GuiGraphicsExtractor gfx, Font font, int x, int y) {
        int spacing = spacing(font);
        for (int i = 0; i < this.comp.gems.size(); i++) {
            gfx.text(font, getSocketDesc(this.comp.gems.get(i)), x + 12, y + 1 + spacing * i, 0xFFAABBCC, true);
        }
    }

    private static int spacing(Font font) {
        return font.lineHeight + 2;
    }

    public static Component getSocketDesc(GemInstance inst) {
        if (!inst.isValid()) {
            return Component.translatable("socket.apotheosis.empty");
        }
        return inst.getSocketBonusTooltip(AdventureModuleClient.tooltipCtx());
    }

    public static record SocketComponent(ItemStack socketed, SocketedGems gems) implements TooltipComponent {}

}
