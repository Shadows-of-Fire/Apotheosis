package dev.shadowsoffire.apotheosis.adventure.client;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class GhostVertexBuilder implements VertexConsumer {
    private final VertexConsumer wrapped;
    private final int alpha;

    public GhostVertexBuilder(VertexConsumer wrapped, int alpha) {
        this.wrapped = wrapped;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return this.wrapped.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this.wrapped.color(red, green, blue, alpha * this.alpha / 0xFF);
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        return this.wrapped.uv(u, v);
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return this.wrapped.overlayCoords(u, v);
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return this.wrapped.uv2(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this.wrapped.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        this.wrapped.endVertex();
    }

    @Override
    public void defaultColor(int pRed, int pGreen, int pBlue, int pAlpha) {

    }

    @Override
    public void unsetDefaultColor() {

    }

    public static Function<MultiBufferSource, MultiBufferSource> makeGhostBuffer(int alpha) {
        return mbs -> new GhostBufferSource(mbs, alpha);
    }

    public static class GhostBufferSource implements MultiBufferSource {

        private final MultiBufferSource wrapped;
        private final int alpha;

        public GhostBufferSource(MultiBufferSource wrapped, int alpha) {
            this.wrapped = wrapped;
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return new GhostVertexBuilder(this.wrapped.getBuffer(type), this.alpha);
        }

    }

}
