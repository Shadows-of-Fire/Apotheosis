package dev.shadowsoffire.apotheosis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class BeamRenderer {
    public static void renderBeaconBeam(
        PoseStack poseStack, SubmitNodeCollector collector, Identifier beamLocation, Identifier glowLocation, float partialTick, float textureScale,
        long gameTime, float yOffset, float height, int colorBot, int colorTop, float beamRadius, float glowRadius) {

        if (height < 0) {
            return;
        }

        final float maxY = yOffset + height;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float f = (float) Math.floorMod(gameTime, 40) + partialTick;
        float f1 = height < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        final float f6 = -beamRadius;
        final float f9 = -beamRadius;
        final float f12 = -1.0F + f2;
        final float f13 = height * textureScale * (0.5F / beamRadius) + f12;
        collector.submitCustomGeometry(poseStack, ApothRenderTypes.affixBeam(beamLocation, true), (pose, buffer) -> renderPart(
            pose, buffer, colorBot, colorTop, yOffset, maxY,
            0.0F, beamRadius, beamRadius, 0.0F, f6, 0.0F, 0.0F, f9,
            0.0F, 1.0F, f13, f12));
        poseStack.popPose();
        final float g3 = -glowRadius;
        final float g4 = -glowRadius;
        final float g5 = -glowRadius;
        final float g6 = -glowRadius;
        final float g12 = -1.0F + f2;
        final float g13 = height * textureScale + g12;
        final int glowBot = (ARGB.alpha(colorBot) / 2) << 24 | (colorBot & 0x00FFFFFF);
        final int glowTop = (ARGB.alpha(colorTop) / 2) << 24 | (colorTop & 0x00FFFFFF);
        collector.submitCustomGeometry(poseStack, ApothRenderTypes.affixBeam(glowLocation, true), (pose, buffer) -> renderPart(
            pose, buffer, glowBot, glowTop, yOffset, maxY,
            g3, g4, glowRadius, g5, g6, glowRadius, glowRadius, glowRadius,
            0.0F, 1.0F, g13, g12));
        poseStack.popPose();
    }

    private static void renderPart(
        PoseStack.Pose pose,
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
        renderQuad(pose, consumer, colorBot, colorTop, minY, maxY, x1, z1, x2, z2, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, colorBot, colorTop, minY, maxY, x4, z4, x3, z3, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, colorBot, colorTop, minY, maxY, x2, z2, x4, z4, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, colorBot, colorTop, minY, maxY, x3, z3, x1, z1, minU, maxU, minV, maxV);
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
