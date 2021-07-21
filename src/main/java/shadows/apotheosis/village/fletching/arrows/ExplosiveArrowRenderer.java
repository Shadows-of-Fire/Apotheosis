package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;

public class ExplosiveArrowRenderer extends ArrowRenderer<ExplosiveArrowEntity> {

	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/entity/explosive_arrow.png");

	public ExplosiveArrowRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(ExplosiveArrowEntity entity) {
		return TEXTURES;
	}

}