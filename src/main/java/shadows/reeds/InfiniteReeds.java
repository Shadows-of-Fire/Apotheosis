package shadows.reeds;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import shadows.Apotheosis;

public class InfiniteReeds {

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Apotheosis.registerOverrideBlock(e.getRegistry(), new BlockReedExt(), Apotheosis.MODID);
	}

}
