package shadows.reeds;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import shadows.Apotheosis;

@EventBusSubscriber(modid = Apotheosis.MODID)
public class InfiniteReeds {

	@SubscribeEvent
	public static void blocks(Register<Block> e) {
		Apotheosis.registerOverrideBlock(e.getRegistry(), new BlockReedExt(), Apotheosis.MODID);
	}

}
