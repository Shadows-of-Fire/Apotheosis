package shadows.apotheosis.ench.altar;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

@SuppressWarnings("deprecation")
public class RenderPrismaticAltar extends TileEntityRenderer<TilePrismaticAltar> {

	public RenderPrismaticAltar(TileEntityRendererDispatcher terd) {
		super(terd);
	}

	@Override
	public void render(TilePrismaticAltar te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buf, int p_225616_5_, int p_225616_6_) {
		if (this.dispatcher.renderInfo != null && te.getPos().distanceSq(this.dispatcher.renderInfo.getProjectedView().x, this.dispatcher.renderInfo.getProjectedView().y, this.dispatcher.renderInfo.getProjectedView().z, true) < 128d) {

			matrix.push();
			boolean thirdPerson = Minecraft.getInstance().getRenderManager().options.thirdPersonView == 2;
			float viewerYaw = Minecraft.getInstance().renderViewEntity.getYaw(partialTicks);
			float angleRotateItem = !thirdPerson ? -viewerYaw : -viewerYaw % 360 + 180;

			double[][] offsets = { { 3 / 16D, 3 / 16D }, { 3 / 16D, 13 / 16D }, { 13 / 16D, 3 / 16D }, { 13 / 16D, 13 / 16D } };
			float scale = 0.2F;
			double yOffset = 0.75;

			for (int i = 0; i < 4; i++) {
				matrix.push();
				matrix.translate(offsets[i][0], yOffset, offsets[i][1]);
				matrix.multiply(new Quaternion(new Vector3f(0, 1, 0), angleRotateItem, true));
				matrix.scale(scale, scale, scale);
				ItemStack s = te.getInv().getStackInSlot(i);
				if (!s.isEmpty()) Minecraft.getInstance().getItemRenderer().renderItem(s, TransformType.FIXED, p_225616_5_, OverlayTexture.DEFAULT_UV, matrix, buf);
				matrix.pop();
			}

			if (!te.getInv().getStackInSlot(4).isEmpty()) {
				matrix.push();
				matrix.translate(0.5, 0.4, 0.5);
				matrix.multiply(new Quaternion(new Vector3f(0, 1, 0), angleRotateItem, true));
				matrix.scale(scale, scale, scale);
				ItemStack s = te.getInv().getStackInSlot(4);
				Minecraft.getInstance().getItemRenderer().renderItem(s, TransformType.FIXED, p_225616_5_, OverlayTexture.DEFAULT_UV, matrix, buf);
				matrix.pop();
			}

			matrix.pop();
		}
	}
}
