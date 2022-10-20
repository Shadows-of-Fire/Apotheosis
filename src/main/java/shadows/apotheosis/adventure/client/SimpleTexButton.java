package shadows.apotheosis.adventure.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

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
		this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, pTextureWidth, pTextureHeight, pOnPress, NO_TOOLTIP, pMessage);
	}

	public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Button.OnTooltip pOnTooltip, Component pMessage) {
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

	public void setPosition(int pX, int pY) {
		this.x = pX;
		this.y = pY;
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);
		int yTex = yTexStart;
		if (!this.isActive()) {
			yTex += this.height;
		} else if (this.isHoveredOrFocused()) {
			yTex += this.height * 2;
		}

		RenderSystem.enableDepthTest();
		blit(pPoseStack, this.x, this.y, this.xTexStart, yTex, this.width, this.height, textureWidth, textureHeight);
		if (this.isHoveredOrFocused()) {
			this.renderToolTip(pPoseStack, pMouseX, pMouseY);
		}
	}

	@Override
	public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
		if (this.onTooltip != NO_TOOLTIP) this.onTooltip.onTooltip(this, pPoseStack, pMouseX, pMouseY);
		else if (this.getMessage() != CommonComponents.EMPTY) {
			MutableComponent primary = (MutableComponent) this.getMessage();
			if (!this.active) primary = primary.withStyle(ChatFormatting.GRAY);
			List<FormattedCharSequence> tooltips = new ArrayList<>();
			tooltips.add(primary.getVisualOrderText());
			if (!this.active && this.inactiveMessage != CommonComponents.EMPTY) tooltips.add(this.inactiveMessage.getVisualOrderText());
			Minecraft.getInstance().screen.renderTooltip(pPoseStack, tooltips, pMouseX, pMouseY);
		}
	}

}
