package dev.shadowsoffire.apotheosis.client;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * You might ask, why am I using {@link RenderType#RENDERTYPE_EYES_SHADER}?
 * It seems to have the best compatibility with Shaders for rendering this kind of geometry.
 * <p>
 * Originally, I was using {@link RenderType#RENDERTYPE_BEACON_BEAM_SHADER} and {@link RenderType#RENDERTYPE_ENTITY_SHADOW_SHADER}
 * respectively (the originals), but that caused weird translucency artifacts when using Shaders. I suspect this is
 * because shader packs are messing with these shaders to achieve some kind of desired effect.
 * <p>
 * Fortunately, using EYES gives us the same desired visual effect in vanilla, and does not appear to break when used with most shaders.
 */
public class ApothRenderTypes extends RenderType {

    public static final BiFunction<ResourceLocation, Boolean, RenderType> BEAM = Util.memoize(
        (texture, translucency) -> {
            RenderType.CompositeState builder = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_EYES_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(translucency ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                .setWriteMaskState(translucency ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .createCompositeState(false);
            return create("apotheosis:beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 1536, false, true, builder);
        });

    public static final Function<ResourceLocation, RenderType> SHADOW = Util.memoize(
        texture -> {
            RenderType.CompositeState builder = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_EYES_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(false);
            return create("apotheosis:shadow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, false, builder);
        });

    public static RenderType affixBeam(ResourceLocation location, boolean colorFlag) {
        return BEAM.apply(location, colorFlag);
    }

    public static RenderType affixShadow(ResourceLocation location) {
        return SHADOW.apply(location);
    }

    private ApothRenderTypes(String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

}
