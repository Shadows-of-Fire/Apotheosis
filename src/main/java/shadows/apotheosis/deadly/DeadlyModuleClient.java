package shadows.apotheosis.deadly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.Item;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.objects.RarityShardItem;

public class DeadlyModuleClient {

	public static void init() {
		RenderTypeLookup.setRenderLayer(ApotheosisObjects.BOSS_SPAWNER, RenderType.getCutout());
		Minecraft.getInstance().getItemColors().register((stack, tint) -> {
			return ((RarityShardItem) stack.getItem()).getRarity().getColor().getColor();
		}, DeadlyModule.RARITY_SHARDS.values().toArray(new Item[6]));
	}

}
