package dev.shadowsoffire.apotheosis.village.fletching.arrows;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class ExplosiveArrowRenderer extends ArrowRenderer<ExplosiveArrowEntity> {

    public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/entity/explosive_arrow.png");

    public ExplosiveArrowRenderer(Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public ResourceLocation getTextureLocation(ExplosiveArrowEntity entity) {
        return TEXTURES;
    }

}
