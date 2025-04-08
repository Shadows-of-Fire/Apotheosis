package dev.shadowsoffire.apotheosis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class BeamRenderer {
    public static void renderBeaconBeam(
        PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation beamLocation, ResourceLocation glowLocation, float partialTick, float textureScale,
        long gameTime, float yOffset, float height, int colorBot, int colorTop, float beamRadius, float glowRadius) {

        if (height < 0) {
            return;
        }

        float i = yOffset + height;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float f = (float) Math.floorMod(gameTime, 40) + partialTick;
        float f1 = height < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f3 = 0.0F;
        float f5 = 0.0F;
        float f6 = -beamRadius;
        float f9 = -beamRadius;
        float f12 = -1.0F + f2;
        float f13 = (float) height * textureScale * (0.5F / beamRadius) + f12;
        renderPart(
            poseStack,
            bufferSource.getBuffer(ApothRenderTypes.affixBeam(beamLocation, true)),
            colorBot,
            colorTop,
            yOffset,
            i,
            0.0F,
            beamRadius,
            beamRadius,
            0.0F,
            f6,
            0.0F,
            0.0F,
            f9,
            0.0F,
            1.0F,
            f13,
            f12);
        poseStack.popPose();
        f3 = -glowRadius;
        float f4 = -glowRadius;
        f5 = -glowRadius;
        f6 = -glowRadius;
        f12 = -1.0F + f2;
        f13 = (float) height * textureScale + f12;
        renderPart(
            poseStack,
            bufferSource.getBuffer(ApothRenderTypes.affixBeam(glowLocation, true)),
            FastColor.ARGB32.color(FastColor.ARGB32.alpha(colorBot) / 2, colorBot),
            FastColor.ARGB32.color(FastColor.ARGB32.alpha(colorTop) / 2, colorTop),
            yOffset,
            i,
            f3,
            f4,
            glowRadius,
            f5,
            f6,
            glowRadius,
            glowRadius,
            glowRadius,
            0.0F,
            1.0F,
            f13,
            f12);
        poseStack.popPose();
    }

    private static void renderPart(
        PoseStack poseStack,
        VertexConsumer consumer,
        int colorBot,
        int colorTop,
        float minY,
        float maxY,
        float x1,
        float z1,
        float x2,
        float z2,
        float x3,
        float z3,
        float x4,
        float z4,
        float minU,
        float maxU,
        float minV,
        float maxV) {
        PoseStack.Pose posestack$pose = poseStack.last();
        renderQuad(
            posestack$pose, consumer, colorBot, colorTop, minY, maxY, x1, z1, x2, z2, minU, maxU, minV, maxV);
        renderQuad(
            posestack$pose, consumer, colorBot, colorTop, minY, maxY, x4, z4, x3, z3, minU, maxU, minV, maxV);
        renderQuad(
            posestack$pose, consumer, colorBot, colorTop, minY, maxY, x2, z2, x4, z4, minU, maxU, minV, maxV);
        renderQuad(
            posestack$pose, consumer, colorBot, colorTop, minY, maxY, x3, z3, x1, z1, minU, maxU, minV, maxV);
    }

    private static void renderQuad(
        PoseStack.Pose pose,
        VertexConsumer consumer,
        int colorBot,
        int colorTop,
        float minY,
        float maxY,
        float minX,
        float minZ,
        float maxX,
        float maxZ,
        float minU,
        float maxU,
        float minV,
        float maxV) {
        addVertex(pose, consumer, colorTop, maxY, minX, minZ, maxU, minV);
        addVertex(pose, consumer, colorBot, minY, minX, minZ, maxU, maxV);
        addVertex(pose, consumer, colorBot, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, consumer, colorTop, maxY, maxX, maxZ, minU, minV);
    }

    private static void addVertex(
        PoseStack.Pose pose, VertexConsumer consumer, int color, float y, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880)
            .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
