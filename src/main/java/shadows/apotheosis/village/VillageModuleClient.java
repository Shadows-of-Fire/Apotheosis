package shadows.apotheosis.village;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.village.fletching.FletchingScreen;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowRenderer;
import shadows.apotheosis.village.fletching.arrows.ExplosiveArrowRenderer;
import shadows.apotheosis.village.fletching.arrows.MiningArrowRenderer;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowRenderer;

public class VillageModuleClient {

	public static void init() {
		MenuScreens.register(ApotheosisObjects.FLETCHING, FletchingScreen::new);
		EntityRenderers.register(ApotheosisObjects.OB_ARROW_ENTITY, ObsidianArrowRenderer::new);
		EntityRenderers.register(ApotheosisObjects.BH_ARROW_ENTITY, BroadheadArrowRenderer::new);
		EntityRenderers.register(ApotheosisObjects.EX_ARROW_ENTITY, ExplosiveArrowRenderer::new);
		EntityRenderers.register(ApotheosisObjects.MN_ARROW_ENTITY, MiningArrowRenderer::new);
	}

}