package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;

public class ObsidianArrowRenderer extends ArrowRenderer<ObsidianArrowEntity> {

	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/entity/obsidian_arrow.png");

	public ObsidianArrowRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(ObsidianArrowEntity entity) {
		return TEXTURES;
	}

}