package dev.shadowsoffire.apotheosis.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class MiningArrowRenderer extends ArrowRenderer<MiningArrowEntity> {

    public MiningArrowRenderer(Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public ResourceLocation getTextureLocation(MiningArrowEntity entity) {
        return entity.type.getTexture();
    }

}
