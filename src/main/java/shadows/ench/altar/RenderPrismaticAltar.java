package shadows.ench.altar;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;

@SuppressWarnings("deprecation")
public class RenderPrismaticAltar extends TileEntityRenderer<TilePrismaticAltar> {

	@Override
	public void render(TilePrismaticAltar te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		int light = te.getWorld().getCombinedLight(te.getPos(), 0);
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, light % 65536, light / 65536);

		boolean thirdPerson = Minecraft.getInstance().getRenderManager().options.thirdPersonView == 2;
		float viewerYaw = Minecraft.getInstance().renderViewEntity.getYaw(partialTicks);
		float angleRotateItem = !thirdPerson ? -viewerYaw : -viewerYaw % 360 + 180;

		double[][] offsets = { { 3 / 16D, 3 / 16D }, { 3 / 16D, 13 / 16D }, { 13 / 16D, 3 / 16D }, { 13 / 16D, 13 / 16D } };
		double scale = 0.2;
		double yOffset = 0.75;

		for (int i = 0; i < 4; i++) {
			GlStateManager.pushMatrix();
			GlStateManager.translated(x + offsets[i][0], y + yOffset, z + offsets[i][1]);
			GlStateManager.rotated(angleRotateItem, 0, 1, 0);
			GlStateManager.scaled(scale, scale, scale);
			ItemStack s = te.getInv().getStackInSlot(i);
			if (!s.isEmpty()) Minecraft.getInstance().getItemRenderer().renderItem(s, TransformType.FIXED);
			GlStateManager.popMatrix();
		}

		if (!te.getInv().getStackInSlot(4).isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translated(x + 0.5, y + 0.4, z + 0.5);
			GlStateManager.rotated(angleRotateItem, 0, 1, 0);
			GlStateManager.scaled(scale, scale, scale);
			ItemStack s = te.getInv().getStackInSlot(4);
			Minecraft.getInstance().getItemRenderer().renderItem(s, TransformType.FIXED);
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

}
