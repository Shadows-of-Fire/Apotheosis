package dev.shadowsoffire.apotheosis.adventure.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SimpleTexButton extends Button {

    protected final ResourceLocation texture;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int textureWidth;
    protected final int textureHeight;
    protected Component inactiveMessage = CommonComponents.EMPTY;

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, 256, 256, pOnPress);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, pTextureWidth, pTextureHeight, pOnPress, CommonComponents.EMPTY);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Component pMessage) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, pTextureWidth, pTextureHeight, pOnPress, DEFAULT_NARRATION, pMessage);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Button.CreateNarration pOnTooltip,
        Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.textureWidth = pTextureWidth;
        this.textureHeight = pTextureHeight;
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.texture = texture;
    }

    public SimpleTexButton setInactiveMessage(Component msg) {
        this.inactiveMessage = msg;
        return this;
    }

    @Override
    public void setPosition(int pX, int pY) {
        this.setX(pX);
        this.setY(pY);
    }

    @Override
    public void renderWidget(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        int yTex = this.yTexStart;
        if (!this.isActive()) {
            yTex += this.height;
        }
        else if (this.isHovered()) {
            yTex += this.height * 2;
        }

        RenderSystem.enableDepthTest();
        gfx.blit(this.texture, this.getX(), this.getY(), this.xTexStart, yTex, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered()) {
            this.renderToolTip(gfx, pMouseX, pMouseY);
        }
    }

    public void renderToolTip(GuiGraphics gfx, int pMouseX, int pMouseY) {
        if (this.getMessage() != CommonComponents.EMPTY && this.isHovered()) {
            Component primary = this.getMessage();
            if (!this.active) {
                primary = primary.copy().withStyle(ChatFormatting.GRAY);
            }
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(primary);
            if (!this.active && this.inactiveMessage != CommonComponents.EMPTY) {
                tooltips.add(this.inactiveMessage);
            }
            gfx.renderComponentTooltip(Minecraft.getInstance().font, tooltips, pMouseX, pMouseY);
        }
    }

}
