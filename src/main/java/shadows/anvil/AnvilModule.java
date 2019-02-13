package shadows.anvil;

import net.minecraft.block.Block;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisPreInit;

/**
 * TODO: Roll into Enchantment module.
 * @author Shadows
 *
 */
public class AnvilModule {

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		GameRegistry.registerTileEntity(TileAnvil.class, new ResourceLocation(Apotheosis.MODID, "anvil"));
	}

	@SubscribeEvent
	public void init(ApotheosisInit e) {
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Block b;
		e.getRegistry().register(b = new BlockAnvilExt());
		ForgeRegistries.ITEMS.register(new ItemAnvilExt(b));
	}

	@SubscribeEvent
	public void applyUnbreaking(AnvilRepairEvent e) {
		if (e.getEntityPlayer().openContainer instanceof ContainerRepair) {
			ContainerRepair r = (ContainerRepair) e.getEntityPlayer().openContainer;
			TileEntity te = r.world.getTileEntity(r.pos);
			if (te instanceof TileAnvil) e.setBreakChance(e.getBreakChance() / (((TileAnvil) te).getUnbreaking() + 1));
		}
	}

}
