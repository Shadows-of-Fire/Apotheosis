package shadows.ench.anvil.compat;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.Apotheosis;

public class ATCompat {

	public static void registerBlocks(Register<Block> e) {
		e.getRegistry().registerAll(new BlockTfarAnvil().setRegistryName("minecraft", "anvil"), new BlockTfarAnvil().setRegistryName("minecraft", "chipped_anvil"), new BlockTfarAnvil().setRegistryName("minecraft", "damaged_anvil"));
	}

	@SuppressWarnings("unchecked")
	public static void tileType() {
		TileEntityType<TileEntity> type = (TileEntityType<TileEntity>) ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(Apotheosis.MODID, "anvil"));
		type.factory = TileTfarAnvil::new;
	}
}
