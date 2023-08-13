package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import shadows.apotheosis.Apotheosis;

public class ObsidianArrowRenderer extends ArrowRenderer<ObsidianArrowEntity> {

    public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/entity/obsidian_arrow.png");

    public ObsidianArrowRenderer(Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public ResourceLocation getTextureLocation(ObsidianArrowEntity entity) {
        return TEXTURES;
    }

}
