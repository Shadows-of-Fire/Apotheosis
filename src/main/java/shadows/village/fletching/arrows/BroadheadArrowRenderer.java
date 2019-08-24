package shadows.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import shadows.Apotheosis;

public class BroadheadArrowRenderer extends ArrowRenderer<BroadheadArrowEntity> {

	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/entity/broadhead_arrow.png");

	public BroadheadArrowRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(BroadheadArrowEntity entity) {
		return TEXTURES;
	}

}
