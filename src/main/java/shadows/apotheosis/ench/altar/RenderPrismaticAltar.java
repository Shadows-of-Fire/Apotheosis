package shadows.apotheosis.ench.altar;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

@SuppressWarnings("deprecation")
public class RenderPrismaticAltar extends TileEntityRenderer<TilePrismaticAltar> {

	public RenderPrismaticAltar(TileEntityRendererDispatcher terd) {
		super(terd);
	}

	@Override
	public void func_225616_a_(TilePrismaticAltar te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buf, int p_225616_5_, int p_225616_6_) {
		if (this.field_228858_b_.renderInfo != null && te.getDistanceSq(this.field_228858_b_.renderInfo.getProjectedView().x, this.field_228858_b_.renderInfo.getProjectedView().y, this.field_228858_b_.renderInfo.getProjectedView().z) < 128d) {

			matrix.func_227860_a_();
			boolean thirdPerson = Minecraft.getInstance().getRenderManager().options.thirdPersonView == 2;
			float viewerYaw = Minecraft.getInstance().renderViewEntity.getYaw(partialTicks);
			float angleRotateItem = !thirdPerson ? -viewerYaw : -viewerYaw % 360 + 180;

			double[][] offsets = { { 3 / 16D, 3 / 16D }, { 3 / 16D, 13 / 16D }, { 13 / 16D, 3 / 16D }, { 13 / 16D, 13 / 16D } };
			float scale = 0.2F;
			double yOffset = 0.75;

			for (int i = 0; i < 4; i++) {
				matrix.func_227860_a_();
				matrix.func_227861_a_(offsets[i][0], yOffset, offsets[i][1]);
				matrix.func_227863_a_(new Quaternion(new Vector3f(0, 1, 0), angleRotateItem, true));
				matrix.func_227862_a_(scale, scale, scale);
				ItemStack s = te.getInv().getStackInSlot(i);
				if (!s.isEmpty()) Minecraft.getInstance().getItemRenderer().func_229110_a_(s, TransformType.FIXED, p_225616_5_, OverlayTexture.field_229196_a_, matrix, buf);
				matrix.func_227865_b_();
			}

			if (!te.getInv().getStackInSlot(4).isEmpty()) {
				matrix.func_227860_a_();
				matrix.func_227861_a_(0.5, 0.4, 0.5);
				matrix.func_227863_a_(new Quaternion(new Vector3f(0, 1, 0), angleRotateItem, true));
				matrix.func_227862_a_(scale, scale, scale);
				ItemStack s = te.getInv().getStackInSlot(4);
				Minecraft.getInstance().getItemRenderer().func_229110_a_(s, TransformType.FIXED, p_225616_5_, OverlayTexture.field_229196_a_, matrix, buf);
				matrix.func_227865_b_();
			}

			matrix.func_227865_b_();
		}
	}
}
