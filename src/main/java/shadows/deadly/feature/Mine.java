package shadows.deadly.feature;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.util.DeadlyUtil;
import shadows.deadly.util.TagBuilder;

/**
 * Creates a TNT spawner that detonates as soon as someone gets too close.
 * @author Shadows
 *
 */
public class Mine extends WorldFeature {

	public static final SpawnerItem TNT_SPAWNER = new SpawnerItem(TagBuilder.createTNTSpawner(), 0);

	@Override
	public void generate(World world, BlockPos pos) {
		if (DeadlyConfig.mineChance <= world.rand.nextDouble()) return;
		int x = pos.getX() + MathHelper.getInt(world.rand, 4, 12);
		int z = pos.getZ() + MathHelper.getInt(world.rand, 4, 12);
		int y = world.rand.nextInt(50) + 11;
		MutableBlockPos mPos = new MutableBlockPos();
		for (byte state = 0; y > 5; y--) {
			if (world.isBlockNormalCube(mPos.setPos(x, y, z), true)) {
				if (state == 0) {
					for (EnumFacing facing : EnumFacing.HORIZONTALS)
						if (world.isAirBlock(mPos.offset(facing))) {
							world.setBlockToAir(mPos);
							mPos = mPos.setPos(mPos.getX(), mPos.getY() - 1, mPos.getZ());
							break;
						}
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
		for (EnumFacing facing : EnumFacing.HORIZONTALS)
			if (!world.isBlockNormalCube(pos.offset(facing), false)) return false;
		return world.isAirBlock(pos.up(2)) && world.isBlockNormalCube(pos.down(), false);
	}

	@Override
	public void place(World world, BlockPos pos) {
		TNT_SPAWNER.place(world, pos);
		DeadlyUtil.coverTrap(world, pos);
	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.mineChance > 0;
	}
}