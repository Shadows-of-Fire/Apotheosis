package shadows.ench.altar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class RenderPrismaticAltar extends TileEntitySpecialRenderer<TilePrismaticAltar> {

	@Override
	public void render(TilePrismaticAltar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		int light = te.getWorld().getCombinedLight(te.getPos(), 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);

		boolean thirdPerson = Minecraft.getMinecraft().getRenderManager().options.thirdPersonView == 2;
		float viewerYaw = this.rendererDispatcher.entityYaw;
		float angleRotateItem = !thirdPerson ? -viewerYaw : -viewerYaw % 360 + 180;

		double[][] offsets = { { 3 / 16D, 3 / 16D }, { 3 / 16D, 13 / 16D }, { 13 / 16D, 3 / 16D }, { 13 / 16D, 13 / 16D } };
		double scale = 0.2;
		double yOffset = 0.75;

		for (int i = 0; i < 4; i++) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + offsets[i][0], y + yOffset, z + offsets[i][1]);
			GlStateManager.rotate(angleRotateItem, 0, 1, 0);
			GlStateManager.scale(scale, scale, scale);
			ItemStack s = te.getInv().getStackInSlot(i);
			if (!s.isEmpty()) Minecraft.getMinecraft().getRenderItem().renderItem(s, TransformType.FIXED);
			GlStateManager.popMatrix();
		}

		if (!te.getInv().getStackInSlot(4).isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5, y + 0.4, z + 0.5);
			GlStateManager.rotate(angleRotateItem, 0, 1, 0);
			GlStateManager.scale(scale, scale, scale);
			ItemStack s = te.getInv().getStackInSlot(4);
			Minecraft.getMinecraft().getRenderItem().renderItem(s, TransformType.FIXED);
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

}
