package shadows.deadly.feature;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.util.ChestBuilder;
import shadows.deadly.util.DeadlyConstants;

public class RogueSpawner extends WorldFeature {

	public static final List<SpawnerItem> ROGUE_SPAWNERS = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos) {
		if (world.rand.nextFloat() <= DeadlyConfig.spawnerChance) {
			int x = pos.getX() + MathHelper.getInt(world.rand, 4, 12);
			int z = pos.getZ() + MathHelper.getInt(world.rand, 4, 12);
			int y = world.rand.nextInt(50) + 11;
			MutableBlockPos mPos = new MutableBlockPos();
			for (byte state = 0; y > 4; y--) {
				if (world.isBlockNormalCube(mPos.setPos(x, y, z), true)) {
					if (state == 0) {
						if (this.canBePlaced(world, mPos.setPos(x, y + 1, z))) {
							this.place(world, mPos.setPos(x, y + 1, z));
							return;
						}
						state = -1;
					}
				} else {
					state = 0;
				}
			}
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos) {
		return world.isAirBlock(pos) && world.isAirBlock(pos.up());
	}

	@Override
	public void place(World world, BlockPos pos) {
		if (world.rand.nextDouble() <= DeadlyConfig.spawnerArmorChance) {
			boolean trick = false;
			int[][] positions;
			if (world.rand.nextDouble() <= DeadlyConfig.spawnerChestChance) {
				positions = new int[][] { { 0, 1, 0 }, { -1, 0, 0 }, { 1, 0, 0 }, { 0, 0, -1 }, { 0, 0, 1 }, { -1, -1, 0 }, { 1, -1, 0 }, { 0, -1, -1 }, { 0, -1, 1 }, { 0, -2, 0 } };
				ChestBuilder.place(world, world.rand, pos.down(), ChestBuilder.SPAWNER_ARMORED);
			} else {
				positions = new int[][] { { 0, 1, 0 }, { -1, 0, 0 }, { 1, 0, 0 }, { 0, 0, -1 }, { 0, 0, 1 }, { 0, -1, 0 } };
				trick = world.rand.nextDouble() <= DeadlyConfig.spawnerTrickChance;
			}
			for (int[] set : positions) {
				world.setBlockState(pos.add(set[0], set[1], set[2]), Blocks.OBSIDIAN.getDefaultState(), 2);
			}
			if (trick) {
				ChestBuilder.place(world, world.rand, pos, ChestBuilder.SPAWNER);
				return;
			}
		} else if (world.rand.nextDouble() <= DeadlyConfig.spawnerChestChance && world.isBlockNormalCube(pos.down(2), false)) {
			ChestBuilder.place(world, world.rand, pos.down(), ChestBuilder.SPAWNER);
		}
		WeightedRandom.getRandomItem(world.rand, ROGUE_SPAWNERS).place(world, pos);
	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.spawnerChance > 0;
	}

	public static void init() {
		SpawnerItem.addItems(ROGUE_SPAWNERS, DeadlyConstants.DUNGEON_SPAWNER_STATS, DeadlyConfig.rogueSpawnerWeightedMobs);
	}
}