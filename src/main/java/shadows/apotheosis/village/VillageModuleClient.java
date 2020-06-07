package shadows.apotheosis.village;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.fml.DeferredWorkQueue;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.village.fletching.FletchingScreen;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowRenderer;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowRenderer;

@SuppressWarnings("deprecation")
public class VillageModuleClient {

	public static void init() {
		DeferredWorkQueue.runLater(() -> {
			ScreenManager.registerFactory(ApotheosisObjects.FLETCHING, FletchingScreen::new);
			EntityRendererManager mgr = Minecraft.getInstance().getRenderManager();
			mgr.register(ApotheosisObjects.OB_ARROW_ENTITY, new ObsidianArrowRenderer(mgr));
			mgr.register(ApotheosisObjects.BH_ARROW_ENTITY, new BroadheadArrowRenderer(mgr));
		});
	}

}
