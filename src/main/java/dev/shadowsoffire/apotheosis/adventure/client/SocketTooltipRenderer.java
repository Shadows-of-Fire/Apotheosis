package dev.shadowsoffire.apotheosis.adventure.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class SocketTooltipRenderer implements ClientTooltipComponent {

    public static final ResourceLocation SOCKET = new ResourceLocation(Apotheosis.MODID, "textures/gui/socket.png");

    private final SocketComponent comp;
    private final int spacing = Minecraft.getInstance().font.lineHeight + 2;

    public SocketTooltipRenderer(SocketComponent comp) {
        this.comp = comp;
    }

    @Override
    public int getHeight() {
        return this.spacing * this.comp.gems.size();
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
    public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
        for (int i = 0; i < this.comp.gems.size(); i++) {
            gfx.blit(SOCKET, x, y + this.spacing * i, 0, 0, 0, 9, 9, 9, 9);
        }
        for (GemInstance inst : this.comp.gems()) {
            if (inst.isValid()) {
                PoseStack pose = gfx.pose();
                pose.pushPose();
                pose.scale(0.5F, 0.5F, 1);
                gfx.renderFakeItem(inst.gemStack(), 2 * x + 1, 2 * y + 1);
                pose.popPose();
            }
            y += this.spacing;
        }
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, BufferSource pBufferSource) {
        for (int i = 0; i < this.comp.gems.size(); i++) {
            pFont.drawInBatch(getSocketDesc(this.comp.gems.get(i)), pX + 12, pY + 1 + this.spacing * i, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }

    public static Component getSocketDesc(GemInstance inst) {
        if (!inst.isValid()) return Component.translatable("socket.apotheosis.empty");
        return inst.getSocketBonusTooltip();
    }

    public static record SocketComponent(ItemStack socketed, SocketedGems gems) implements TooltipComponent {}

}
