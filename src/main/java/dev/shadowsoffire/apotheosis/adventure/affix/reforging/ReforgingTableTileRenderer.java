package dev.shadowsoffire.apotheosis.adventure.affix.reforging;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ReforgingTableTileRenderer implements BlockEntityRenderer<ReforgingTableTile> {

    public static final ResourceLocation HAMMER = new ResourceLocation(Apotheosis.MODID, "item/hammer");

    @Override
    @SuppressWarnings("deprecation")
    public void render(ReforgingTableTile tile, float partials, PoseStack matrix, MultiBufferSource pBufferSource, int light, int overlay) {
        ItemRenderer irenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel base = irenderer.getItemModelShaper().getModelManager().getModel(HAMMER);
        matrix.pushPose();

        double px = 1 / 16D;

        matrix.scale(1.25F, 1.25F, 1.25F);
        matrix.translate(8.5 * px / 1.25, 16 * px / 1.25 - 0.015, 7 * px / 1.25);
        matrix.mulPose(Axis.YP.rotationDegrees(45));
        matrix.mulPose(Axis.XP.rotationDegrees(90));

        if (tile.step1) {
            float factor = tile.time % 60 + partials;
            float sin = Mth.sin(factor * Mth.PI / 120);
            float sinSq = sin * sin;

            matrix.translate(0.125 * sinSq, -0, -0.15 * sinSq);
            matrix.mulPose(Axis.YN.rotationDegrees(45 * sinSq));
        }
        else {
            float factor = tile.time % 5 + partials;
            float sin = Mth.sin(Mth.HALF_PI + factor * Mth.PI / 10);
            float sinSq = sin * sin;

            matrix.translate(0.125 * sinSq, -0, -0.15 * sinSq);
            matrix.mulPose(Axis.YN.rotationDegrees(45 * sinSq));
        }
        MultiBufferSource.BufferSource src = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        irenderer.renderModelLists(base, ItemStack.EMPTY, light, overlay, matrix, ItemRenderer.getFoilBufferDirect(src, ItemBlockRenderTypes.getRenderType(tile.getBlockState(), true), true, false));
        src.endBatch();

        matrix.popPose();
    }

}
