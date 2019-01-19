package shadows.deadly.feature;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.util.DeadlyUtil;
import shadows.deadly.util.TagBuilder;

/**
 * Creates a fire spawner that just shoots out falling fire blocks.  Very unsafe.
 * @author Shadows
 *
 */
public class FireTrap extends WorldFeature {

	public static final SpawnerItem FIRE_SPAWNER = new SpawnerItem(TagBuilder.createFireSpawner(), 0);

	@Override
	public void generate(World world, BlockPos pos) {
		if (world.rand.nextFloat() <= DeadlyConfig.fireTrapChance) return;
		int x = pos.getX() + world.rand.nextInt(16);
		int z = pos.getZ() + world.rand.nextInt(16);
		int y = world.rand.nextInt(50) + 11;
		MutableBlockPos mPos = new MutableBlockPos();
		for (byte state = 0; y > 5; y--) {
			if (world.isBlockNormalCube(mPos.setPos(x, y, z), true)) {
				if (state == 0) {
					if (this.canBePlaced(world, mPos.setPos(x, y, z))) {
						this.place(world, mPos.setPos(x, y, z));
						return;
					}
					state = -1;
				}
			} else {
				state = 0;
			}
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos) {
		for (int x1 = -1; x1 < 2; x1++) {
			for (int z1 = -1; z1 < 2; z1++) {
				if (world.isBlockNormalCube(pos.add(x1, 1, z1), true)) return false;
			}
		}
		return world.isAirBlock(pos.up(2)) && world.isBlockNormalCube(pos.down(), false) && world.isBlockNormalCube(pos.add(-1, 0, 0), false) && world.isBlockNormalCube(pos.add(1, 0, 0), false) && world.isBlockNormalCube(pos.add(0, 0, -1), false) && world.isBlockNormalCube(pos.add(0, 0, 1), false);
	}

	@Override
	public void place(World world, BlockPos pos) {
		FIRE_SPAWNER.place(world, pos);
		DeadlyUtil.coverTrap(world, pos);
	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.fireTrapChance > 0;
	}
}