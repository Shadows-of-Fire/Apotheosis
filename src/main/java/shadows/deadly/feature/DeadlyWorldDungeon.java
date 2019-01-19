package shadows.deadly.feature;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.util.ChestBuilder;
import shadows.deadly.util.DeadlyConstants;

/**
 * Generator for Deadly World Dungeons.  Not Fully Functional.
 * TODO: Make this a proper dungeon generator, instead of a weird, less-useful version of the vanilla generator.
 * @author Shadows
 *
 */
public class DeadlyWorldDungeon extends WorldFeature {

	public static final List<SpawnerItem> DUNGEON_SPAWNERS = new ArrayList<>();
	public static final List<WeightedTypeEntry> DUNGEON_TYPES = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos) {
		int x, y, z;
		for (float count = DeadlyConfig.dungeonPlaceAttempts; count >= 1.0 || count > 0.0 && count > world.rand.nextFloat(); count--) {
			x = pos.getX() + 8;
			y = world.rand.nextInt(80) + 20;
			z = pos.getY() + 8;
			this.place(world, new BlockPos(x, y, z));
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos) {
		return true;
	}

	@Override
	public void place(World world, BlockPos pos) {
		int radX = 3 + (world.rand.nextInt(3));
		int radY = 3;
		int radZ = 3 + (world.rand.nextInt(3));
		int exposedBlocks = 0;
		int X, Y, Z;
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		MutableBlockPos mPos = new MutableBlockPos();
		for (X = x - radX - 1; X <= x + radX + 1; X++) {
			for (Y = y - 1; Y <= y + radY + 1; Y++) {
				for (Z = z - radZ - 1; Z <= z + radZ + 1; Z++) {
					if ((Y == y - 1 || Y == y + radY + 1) && !world.getBlockState(mPos.setPos(X, Y, Z)).getMaterial().isSolid()) return;
					if ((X == x - radX - 1 || X == x + radX + 1 || Z == z - radZ - 1 || Z == z + radZ + 1) && Y == y && isAcceptable(world, mPos.setPos(X, Y, Z)) && isAcceptable(world, mPos.setPos(X, Y + 1, Z))) {
						exposedBlocks++;
					}
				}
			}
		}
		if (exposedBlocks < 1 || exposedBlocks > 15) return;
		for (X = x - radX - 1; X <= x + radX + 1; X++) {
			for (Y = y + radY; Y >= y - 1; Y--) {
				for (Z = z - radZ - 1; Z <= z + radZ + 1; Z++) {
					if (X != x - radX - 1 && Y != y - 1 && Z != z - radZ - 1 && X != x + radX + 1 && Y != y + radY + 1 && Z != z + radZ + 1) {
						world.setBlockToAir(mPos.setPos(X, Y, Z));
					} else if (Y >= 0 && !world.getBlockState(mPos.setPos(X, Y - 1, Z)).getMaterial().isSolid()) {
						world.setBlockToAir(mPos.setPos(X, Y, Z));
					} else if (world.getBlockState(mPos.setPos(X, Y, Z)).getMaterial().isSolid()) {
						if (Y == y - 1 && world.rand.nextInt(4) != 0) {
							world.setBlockState(mPos.setPos(X, Y, Z), Blocks.MOSSY_COBBLESTONE.getDefaultState(), 2);
						} else if (world.rand.nextDouble() < DeadlyConfig.dungeonSilverfishChance) {
							world.setBlockState(mPos.setPos(X, Y, Z), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.COBBLESTONE), 2);
						} else {
							world.setBlockState(mPos.setPos(X, Y, Z), Blocks.COBBLESTONE.getDefaultState(), 2);
						}
					}
				}
			}
		}
		byte chestCount = 0;
		while (chestCount < 2) {
			byte chestAttempts = 0;
			while (true) {
				if (chestAttempts < 3) {
					placeChest: {
						X = x + world.rand.nextInt(radX * 2 + 1) - radX;
						Z = z + world.rand.nextInt(radZ * 2 + 1) - radZ;
						if (world.isAirBlock(mPos.setPos(X, y, Z))) {
							exposedBlocks = 0;
							if (world.getBlockState(mPos.setPos(X - 1, y, Z)).getMaterial().isSolid()) {
								exposedBlocks++;
							}
							if (world.getBlockState(mPos.setPos(X + 1, y, Z)).getMaterial().isSolid()) {
								exposedBlocks++;
							}
							if (world.getBlockState(mPos.setPos(X, y, Z - 1)).getMaterial().isSolid()) {
								exposedBlocks++;
							}
							if (world.getBlockState(mPos.setPos(X, y, Z + 1)).getMaterial().isSolid()) {
								exposedBlocks++;
							}
							if (exposedBlocks == 1) {
								ChestBuilder.place(world, world.rand, mPos.setPos(X, y, Z), world.rand.nextDouble() > .95 ? LootTableList.CHESTS_STRONGHOLD_CORRIDOR : LootTableList.CHESTS_SIMPLE_DUNGEON);
								break placeChest;
							}
						}
						chestAttempts++;
						continue;
					}
				}
				chestCount++;
				break;
			}
		}
		WeightedRandom.getRandomItem(world.rand, DUNGEON_TYPES).generate(world, mPos, x, y, z);
	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.dungeonPlaceAttempts > 0;
	}

	public static void init() {
		SpawnerItem.addItems(DUNGEON_SPAWNERS, DeadlyConstants.DUNGEON_SPAWNER_STATS, DeadlyConfig.dungeonWeightedMobs);

		DUNGEON_TYPES.add(new WeightedTypeEntry(DeadlyConfig.dungeonDefaultChance) {
			@Override
			void generate(World world, MutableBlockPos mPos, int x, int y, int z) {
				if (world.rand.nextFloat() < DeadlyConfig.dungeonArmorChance) {
					IBlockState ob = Blocks.OBSIDIAN.getDefaultState();
					world.setBlockState(mPos.setPos(x - 1, y, z), ob, 2);
					world.setBlockState(mPos.setPos(x + 1, y, z), ob, 2);
					world.setBlockState(mPos.setPos(x, y - 1, z), ob, 2);
					world.setBlockState(mPos.setPos(x, y + 1, z), ob, 2);
					world.setBlockState(mPos.setPos(x, y, z - 1), ob, 2);
					world.setBlockState(mPos.setPos(x, y, z + 1), ob, 2);
				}
				WeightedRandom.getRandomItem(world.rand, DUNGEON_SPAWNERS).place(world, new BlockPos(x, y, z));

			}
		});
		/*
				DUNGEON_TYPES.add(new WeightedTypeEntry(DeadlyConfig.dungeonTowerChance) {
					@Override
					void generate(World world, MutableBlockPos mPos, int x, int y, int z) {
						Tower.placeTower(world, new BlockPos(x, y + 1, z));
						if (world.getBlockState(mPos.setPos(x, y, z)).getBlock() == Blocks.COBBLESTONE && world.rand.nextDouble() < DeadlyConfig.dungeonSilverfishChance) {
							DeadlyUtil.setBlockWithMeta(world, mPos.setPos(x, y, z), Blocks.MONSTER_EGG, 1, 2);
						}
					}
				});
		*/

		DUNGEON_TYPES.add(new WeightedTypeEntry(DeadlyConfig.dungeonBrutalChance) {
			@Override
			void generate(World world, MutableBlockPos mPos, int x, int y, int z) {
				WorldGenerator.BRUTAL_SPAWNER.place(world, new BlockPos(x, y, z));
			}
		});

		DUNGEON_TYPES.add(new WeightedTypeEntry(DeadlyConfig.dungeonSwarmChance) {
			@Override
			void generate(World world, MutableBlockPos mPos, int x, int y, int z) {
				WorldGenerator.SWARM_SPAWNER.place(world, new BlockPos(x, y, z));
			}
		});

	}

	private static abstract class WeightedTypeEntry extends WeightedRandom.Item {
		WeightedTypeEntry(int weight) {
			super(weight);
		}

		abstract void generate(World world, MutableBlockPos mPos, int x, int y, int z);
	}

	private boolean isAcceptable(World world, MutableBlockPos pos) {
		return world.isAirBlock(pos); // || world.getBlockState(pos).getBlock() == Blocks.STONE;
	}
}