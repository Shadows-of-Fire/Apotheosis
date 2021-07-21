package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class MiningArrowRenderer extends ArrowRenderer<MiningArrowEntity> {

	public MiningArrowRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(MiningArrowEntity entity) {
		return entity.type.getTexture();
	}

}