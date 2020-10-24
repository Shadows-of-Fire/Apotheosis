package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;

public class MiningArrowRenderer extends ArrowRenderer<MiningArrowEntity> {

	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/entity/mining_arrow.png");

	public MiningArrowRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getEntityTexture(MiningArrowEntity entity) {
		return TEXTURES;
	}

}