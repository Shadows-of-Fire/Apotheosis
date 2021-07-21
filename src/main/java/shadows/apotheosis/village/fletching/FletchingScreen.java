package shadows.apotheosis.village.fletching;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import shadows.apotheosis.Apotheosis;

public class FletchingScreen extends ContainerScreen<FletchingContainer> {
	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/fletching_table.png");

	public FletchingScreen(FletchingContainer container, PlayerInventory player, ITextComponent title) {
		super(container, player, title);
		this.titleLabelX = 47;
		this.titleLabelY = 6;
		this.inventoryLabelX = 8;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	public void render(MatrixStack stack, int x, int y, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, x, y, partialTicks);
		this.renderTooltip(stack, x, y);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(TEXTURES);
		int i = this.leftPos;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(stack, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}
}