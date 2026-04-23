package dev.shadowsoffire.apotheosis.client;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/**
 * Provides custom render types used by various Apotheosis rendering systems.
 * <p>
 * Historically we used {@code RENDERTYPE_EYES_SHADER} for better compatibility with shader packs
 * when rendering glowing/translucent overlays. On 26.1 we route through {@link RenderTypes#eyes(Identifier)}
 * which wires the same EYES pipeline.
 */
public final class ApothRenderTypes {

    public static final BiFunction<Identifier, Boolean, RenderType> BEAM = Util.memoize(
        (texture, translucency) -> RenderTypes.eyes(texture));

    public static final Function<Identifier, RenderType> SHADOW = Util.memoize(RenderTypes::entityShadow);

    public static RenderType affixBeam(Identifier location, boolean colorFlag) {
        return BEAM.apply(location, colorFlag);
    }

    public static RenderType affixShadow(Identifier location) {
        return SHADOW.apply(location);
    }

    private ApothRenderTypes() {}

}
