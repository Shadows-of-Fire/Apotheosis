package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import shadows.apotheosis.Apotheosis;

public class BroadheadArrowRenderer extends ArrowRenderer<BroadheadArrowEntity> {

	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/entity/broadhead_arrow.png");

	public BroadheadArrowRenderer(Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(BroadheadArrowEntity entity) {
		return TEXTURES;
	}

}