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
	}

	@Override
	public void render(MatrixStack stack, int x, int y, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, x, y, partialTicks);
		this.drawMouseoverTooltip(stack, x, y);
	}

	@Override
	protected void drawForeground(MatrixStack stack, int mouseX, int mouseY) {
		this.textRenderer.draw(stack, this.title, 47, 6.0F, 4210752);
		this.textRenderer.draw(stack, this.playerInventory.getDisplayName(), 8.0F, this.ySize - 96 + 2, 4210752);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void drawBackground(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(TEXTURES);
		int i = this.guiLeft;
		int j = (this.height - this.ySize) / 2;
		this.drawTexture(stack, i, j, 0, 0, this.xSize, this.ySize);
	}
}