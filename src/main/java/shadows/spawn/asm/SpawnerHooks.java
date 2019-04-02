package shadows.spawn.asm;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.datafix.IDataFixer;
import shadows.Apotheosis;
import shadows.spawn.TileSpawnerExt;

/**
 * ASM methods for the spawner module.
 * @author Shadows
 *
 */
public class SpawnerHooks {

	/**
	 * Returns the active class for the mob spawner tile.
	 * Called from {@link TileEntityMobSpawner$2#process(IDataFixer, NBTTagCompound, int)}
	 * Injected by {@link SpawnerTransformer}
	 */
	public static Class<? extends TileEntityMobSpawner> getSpawnerClass() {
		return Apotheosis.enableSpawner ? TileSpawnerExt.class : TileEntityMobSpawner.class;
	}

}
