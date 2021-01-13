package shadows.apotheosis.deadly.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.loot.LootTables;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.objects.BossSpawnerBlock.BossSpawnerTile;
import shadows.apotheosis.deadly.reload.BossItemManager;

public class BossFeature extends Feature<NoFeatureConfig> {

	private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();
	private static final BlockState BRICK = Blocks.STONE_BRICKS.getDefaultState();
	private static final BlockState MOSSY_BRICK = Blocks.MOSSY_STONE_BRICKS.getDefaultState();
	private static final BlockState CRACKED_BRICK = Blocks.CRACKED_STONE_BRICKS.getDefaultState();
	private static final BlockState[] BRICKS = { BRICK, MOSSY_BRICK, CRACKED_BRICK };
	//private static final BlockState WALL = Blocks.STONE_BRICK_WALL.getDefaultState();
	//private static final BlockState MOSSY_WALL = Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState();
	//private static final BlockState[] WALLS = { WALL, MOSSY_WALL };

	public static final BossFeature INSTANCE = new BossFeature();

	public BossFeature() {
		super(NoFeatureConfig.field_236558_a_);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean generate(ISeedReader world, ChunkGenerator gen, Random rand, BlockPos pos, NoFeatureConfig cfg) {
		int xRadius = 3 + rand.nextInt(3);
		int floor = -1;
		int roof = 4;
		int zRadius = 3 + rand.nextInt(3);
		int doors = 0;

		BlockState[][][] states = new BlockState[xRadius * 2 + 1][6][zRadius * 2 + 1];

		for (int x = -xRadius; x <= xRadius; ++x) {
			for (int y = floor; y <= roof; ++y) {
				for (int z = -zRadius; z <= zRadius; ++z) {
					BlockPos blockpos = pos.add(x, y, z);
					BlockState state = world.getBlockState(blockpos);
					Material material = state.getMaterial();
					boolean flag = material.isSolid();
					if (y == -1 && !flag) { return false; } //Exit if the floor is not fully solid.

					if (y == 4 && !flag) { return false; } //Exit if the roof is not fully solid.

					if ((x == -xRadius || x == xRadius || z == -zRadius || z == zRadius) && y == 1 && state.isAir() && states[x + xRadius][y - 1 + 1][z + zRadius].isAir()) {
						++doors; //Count number of 2x1 holes at y=0.
					}
					states[x + xRadius][y + 1][z + zRadius] = state;
				}
			}
		}

		if (doors >= 1 && doors <= 5) {
			for (int x = -xRadius; x <= xRadius; ++x) {
				for (int y = roof - 1; y >= floor; --y) {
					for (int z = -zRadius; z <= zRadius; ++z) {
						BlockPos blockpos = pos.add(x, y, z);
						BlockState state = states[x + xRadius][y + 1][z + zRadius];
						if (x != -xRadius && y != floor && z != -zRadius && x != xRadius && y != roof && z != zRadius) {
							if (!state.isIn(Blocks.CHEST)) world.setBlockState(blockpos, CAVE_AIR, 2);
						} else if (y > floor && !states[x + xRadius][y - 1 + 1][z + zRadius].getMaterial().isSolid()) {
							world.setBlockState(blockpos, CAVE_AIR, 2);
						} else if (state.getMaterial().isSolid() && !state.isIn(Blocks.CHEST)) {
							if (y == floor) {
								world.setBlockState(blockpos, BRICKS[rand.nextInt(3)], 2);
							} else {
								world.setBlockState(blockpos, rand.nextBoolean() ? BRICK : BRICKS[rand.nextInt(3)], 2);
							}
						}
					}
				}
			}

			int xChestRadius = xRadius - 1;
			int zChestRadius = zRadius - 1;
			for (int chests = 0; chests < 2; ++chests) {
				for (int attempts = 0; attempts < 3; ++attempts) {
					boolean wall = rand.nextBoolean(); //Which wall will select a constant position;
					int x = wall ? rand.nextBoolean() ? -xChestRadius : xChestRadius : rand.nextInt(xChestRadius * 2 + 1) - xChestRadius;
					int y = 0;
					int z = !wall ? rand.nextBoolean() ? -zChestRadius : zChestRadius : rand.nextInt(zChestRadius * 2 + 1) - zChestRadius;
					BlockPos blockpos2 = pos.add(x, y, z);
					if (world.getBlockState(blockpos2).isAir()) {
						int nearbySolids = 0;

						for (Direction dir : Direction.Plane.HORIZONTAL) {
							if (world.getBlockState(blockpos2.offset(dir)).getMaterial().isSolid()) {
								++nearbySolids;
							}
						}

						if (nearbySolids == 1) {
							world.setBlockState(blockpos2, StructurePiece.correctFacing(world, blockpos2, Blocks.CHEST.getDefaultState()), 2);
							LockableLootTileEntity.setLootTable(world, rand, blockpos2, LootTables.CHESTS_SIMPLE_DUNGEON);
							break;
						}
					}
				}
			}

			world.setBlockState(pos, ApotheosisObjects.BOSS_SPAWNER.getDefaultState(), 2);
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof BossSpawnerTile) {
				((BossSpawnerTile) tileentity).setBossItem(BossItemManager.INSTANCE.getRandomItem(rand));
			} else {
				DeadlyModule.LOGGER.error("Failed to fetch boss spawner entity at ({}, {}, {})", pos.getX(), pos.getY(), pos.getZ());
			}

			DeadlyFeature.debugLog(pos, "Boss Dungeon");

			return true;
		} else {
			return false;
		}
	}

}
