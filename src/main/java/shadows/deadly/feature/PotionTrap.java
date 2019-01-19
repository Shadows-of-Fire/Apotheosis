package shadows.deadly.feature;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.util.DeadlyConstants.PotionTrapType;
import shadows.deadly.util.DeadlyUtil;
import shadows.deadly.util.TagBuilder;

public class PotionTrap extends WorldFeature {

	public static final List<SpawnerItem> POTION_ITEMS = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos) {
		if (world.rand.nextFloat() <= DeadlyConfig.potionTrapChance) return;
		int x = pos.getX() + MathHelper.getInt(world.rand, 4, 12);
		int z = pos.getZ() + +MathHelper.getInt(world.rand, 4, 12);
		int y = world.rand.nextInt(50) + 11;
		MutableBlockPos mPos = new MutableBlockPos(x, y, z);
		for (byte state = 0; y > 5; y--) {
			if (world.isBlockNormalCube(mPos.setPos(x, y, z), true)) {
				if (state == 0) {
					if (this.canBePlaced(world, mPos)) {
						this.place(world, mPos);
						return;
					}
					state = -1;
				}
			} else {
				state = 0;
			}
		}
	}

	/// Returns true if this feature can be placed at the location.
	@Override
	public boolean canBePlaced(World world, BlockPos pos) {
		for (EnumFacing f : EnumFacing.HORIZONTALS)
			if (world.isAirBlock(pos.offset(f))) {
				pos = pos.down();
				break;
			}
		for (EnumFacing f : EnumFacing.HORIZONTALS)
			if (!world.isBlockNormalCube(pos.offset(f), false)) { return false; }
		return world.isAirBlock(pos.up(2)) && world.isBlockNormalCube(pos.down(), false);
	}

	/// Places this feature at the location.
	@Override
	public void place(World world, BlockPos pos) {
		for (EnumFacing f : EnumFacing.HORIZONTALS)
			if (world.isAirBlock(pos.offset(f))) {
				world.setBlockToAir(pos);
				pos = pos.down();
				break;
			}
		WeightedRandom.getRandomItem(world.rand, POTION_ITEMS).place(world, pos);
		DeadlyUtil.coverTrap(world, pos);
	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.potionTrapChance > 0;
	}

	public static void init() {
		for (PotionTrapType p : PotionTrapType.values()) {
			POTION_ITEMS.add(new SpawnerItem(TagBuilder.createPotionSpawner(p.potion), 1));
		}
	}
}