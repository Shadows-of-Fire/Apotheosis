package shadows.apotheosis.ench.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

public class SeaAltarRenderer implements BlockEntityRenderer<SeaAltarTile> {
	private final BlockEntityRenderDispatcher renderer;

	public SeaAltarRenderer(BlockEntityRendererProvider.Context ctx) {
		this.renderer = ctx.getBlockEntityRenderDispatcher();
	}

	@Override
	public void render(SeaAltarTile te, float partialTicks, PoseStack matrix, MultiBufferSource buf, int combinedLightIn, int combinedOverlayIn) {
		if (this.renderer.camera != null && te.getBlockPos().distSqr(this.renderer.camera.getPosition().x, this.renderer.camera.getPosition().y, this.renderer.camera.getPosition().z, true) < 128d) {

			matrix.pushPose();
			boolean thirdPerson = Minecraft.getInstance().getEntityRenderDispatcher().options.getCameraType() == CameraType.THIRD_PERSON_FRONT;
			float viewerYaw = Minecraft.getInstance().cameraEntity.getViewYRot(partialTicks);
			float angleRotateItem = !thirdPerson ? -viewerYaw : -viewerYaw % 360 + 180;

			double[][] offsets = { { 3 / 16D, 3 / 16D }, { 3 / 16D, 13 / 16D }, { 13 / 16D, 3 / 16D }, { 13 / 16D, 13 / 16D } };
			float scale = 0.2F;
			double yOffset = 0.75;
			
			int pSeed = 0;
			
			for (int i = 0; i < 4; i++) {
				matrix.pushPose();
				matrix.translate(offsets[i][0], yOffset, offsets[i][1]);
				matrix.mulPose(new Quaternion(new Vector3f(0, 1, 0), angleRotateItem, true));
				matrix.scale(scale, scale, scale);
				ItemStack s = te.getInv().getStackInSlot(i);
				if (!s.isEmpty()) {
					Minecraft.getInstance().getItemRenderer().renderStatic(s, TransformType.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrix, buf, pSeed);
				}
				matrix.popPose();
			}

			if (!te.getInv().getStackInSlot(4).isEmpty()) {
				matrix.pushPose();
				matrix.translate(0.5, 0.4, 0.5);
				matrix.mulPose(new Quaternion(new Vector3f(0, 1, 0), angleRotateItem, true));
				matrix.scale(scale, scale, scale);
				ItemStack s = te.getInv().getStackInSlot(4);
				Minecraft.getInstance().getItemRenderer().renderStatic(s, TransformType.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrix, buf, pSeed);
				matrix.popPose();
			}

			matrix.popPose();
		}
	}
}