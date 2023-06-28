package shadows.apotheosis.core.attributeslib.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import shadows.apotheosis.Apotheosis;

public class AttributeModifierComponent implements ClientTooltipComponent {

	public static final ResourceLocation TEXTURE = Apotheosis.loc("textures/gui/attribute_component.png");

	@Nullable
	private final ModifierSource<?> source;
	private final List<FormattedCharSequence> text;

	public AttributeModifierComponent(@Nullable ModifierSource<?> source, FormattedText text, Font font, int maxWidth) {
		this.source = source;
		this.text = font.split(text, maxWidth);
	}

	@Override
	public int getHeight() {
		return text.size() * 10;
	}

	@Override
	public int getWidth(Font font) {
		return this.text.stream().map(font::width).map(w -> w + 12).max(Integer::compareTo).get();
	}

	@Override
	public void renderImage(Font font, int x, int y, PoseStack stack, ItemRenderer itemRenderer, int pBlitOffset) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		GuiComponent.blit(stack, x, y, pBlitOffset, source == null ? 9 : 0, 0, 9, 9, 18, 9);
		if (source == null) return;
		source.render(font, x, y, stack, itemRenderer, pBlitOffset);
	}

	@Override
	public void renderText(Font font, int pX, int pY, Matrix4f pMatrix4f, BufferSource pBufferSource) {
		var line = text.get(0);
		font.drawInBatch(line, pX + 12, pY, -1, true, pMatrix4f, pBufferSource, false, 0, 15728880);
		for (int i = 1; i < text.size(); i++) {
			line = text.get(i);
			font.drawInBatch(line, pX, pY + i * (font.lineHeight + 1), -1, true, pMatrix4f, pBufferSource, false, 0, 15728880);
		}
	}

}
