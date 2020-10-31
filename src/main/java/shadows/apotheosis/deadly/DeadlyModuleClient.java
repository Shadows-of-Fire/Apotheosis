package shadows.apotheosis.deadly;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import shadows.apotheosis.ApotheosisObjects;

public class DeadlyModuleClient {

	public static void init() {
		RenderTypeLookup.setRenderLayer(ApotheosisObjects.BOSS_SPAWNER, RenderType.getCutout());
	}

}
