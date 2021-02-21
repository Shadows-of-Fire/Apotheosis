package shadows.apotheosis.village;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.village.fletching.FletchingScreen;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowRenderer;
import shadows.apotheosis.village.fletching.arrows.ExplosiveArrowRenderer;
import shadows.apotheosis.village.fletching.arrows.MiningArrowRenderer;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowRenderer;

public class VillageModuleClient {

	public static void init() {
		ScreenManager.registerFactory(ApotheosisObjects.FLETCHING, FletchingScreen::new);
		EntityRendererManager mgr = Minecraft.getInstance().getRenderManager();
		mgr.register(ApotheosisObjects.OB_ARROW_ENTITY, new ObsidianArrowRenderer(mgr));
		mgr.register(ApotheosisObjects.BH_ARROW_ENTITY, new BroadheadArrowRenderer(mgr));
		mgr.register(ApotheosisObjects.EX_ARROW_ENTITY, new ExplosiveArrowRenderer(mgr));
		mgr.register(ApotheosisObjects.MN_ARROW_ENTITY, new MiningArrowRenderer(mgr));
	}

}