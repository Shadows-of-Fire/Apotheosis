package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;

public class BroadheadArrowRenderer extends ArrowRenderer<BroadheadArrowEntity> {

	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/entity/broadhead_arrow.png");

	public BroadheadArrowRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(BroadheadArrowEntity entity) {
		return TEXTURES;
	}

}