package shadows.apotheosis.adventure.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.socket.gem.GemInstance;

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
        for (ItemStack gem : this.comp.gems) {
            maxWidth = Math.max(maxWidth, font.width(getSocketDesc(this.comp.socketed, gem)) + 12);
        }
        return maxWidth;
    }

    @Override
    public void renderImage(Font pFont, int x, int y, PoseStack stack, ItemRenderer itemRenderer, int pBlitOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, SOCKET);
        for (int i = 0; i < this.comp.gems.size(); i++) {
            GuiComponent.blit(stack, x, y + this.spacing * i, pBlitOffset, 0, 0, 9, 9, 9, 9);
        }
        for (ItemStack gem : this.comp.gems()) {
            if (!gem.isEmpty()) {
                PoseStack mvStack = RenderSystem.getModelViewStack();
                mvStack.pushPose();
                mvStack.scale(0.5F, 0.5F, 1);
                itemRenderer.renderAndDecorateFakeItem(gem, 2 * x + 1, 2 * y + 1);
                mvStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }
            y += this.spacing;
        }
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, BufferSource pBufferSource) {
        for (int i = 0; i < this.comp.gems.size(); i++) {
            pFont.drawInBatch(getSocketDesc(this.comp.socketed, this.comp.gems.get(i)), pX + 12, pY + 1 + this.spacing * i, 0xAABBCC, true, pMatrix4f, pBufferSource, false, 0, 15728880);
        }
    }

    public static Component getSocketDesc(ItemStack socketed, ItemStack gemStack) {
        GemInstance inst = GemInstance.socketed(socketed, gemStack);
        if (!inst.isValid()) return Component.translatable("socket.apotheosis.empty");
        return inst.getSocketBonusTooltip();
    }

    public static record SocketComponent(ItemStack socketed, List<ItemStack> gems) implements TooltipComponent {}

}
