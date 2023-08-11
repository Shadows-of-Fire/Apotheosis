package dev.shadowsoffire.apotheosis.adventure.gen;

import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BossDungeonFeature extends Feature<NoneFeatureConfiguration> {

    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState BRICK = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY_BRICK = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState CRACKED_BRICK = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    private static final BlockState[] BRICKS = { BRICK, MOSSY_BRICK, CRACKED_BRICK };

    public BossDungeonFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel world = ctx.level();
        if (!AdventureConfig.canGenerateIn(world)) return false;
        BlockPos pos = ctx.origin();
        RandomSource rand = ctx.random();
        int xRadius = 3 + rand.nextInt(3);
        int floor = -1;
        int roof = 4;
        int zRadius = 3 + rand.nextInt(3);
        int doors = 0;

        BlockState[][][] states = new BlockState[xRadius * 2 + 1][6][zRadius * 2 + 1];

        for (int x = -xRadius; x <= xRadius; ++x) {
            for (int y = floor; y <= roof; ++y) {
                for (int z = -zRadius; z <= zRadius; ++z) {
                    BlockPos blockpos = pos.offset(x, y, z);
                    BlockState state = world.getBlockState(blockpos);
                    boolean flag = state.isSolid();
                    // Exit if the floor is not fully solid.

                    if (y == floor && !flag || y == roof && !flag) {
                        return false;
                    } // Exit if the roof is not fully solid.

                    if ((x == -xRadius || x == xRadius || z == -zRadius || z == zRadius) && y == 1 && state.isAir() && states[x + xRadius][y - 1 + 1][z + zRadius].isAir()) {
                        ++doors; // Count number of 2x1 holes at y=0.
                    }
                    states[x + xRadius][y + 1][z + zRadius] = state;
                }
            }
        }

        if (doors >= 1 && doors <= 5) {
            for (int x = -xRadius; x <= xRadius; ++x) {
                for (int y = roof - 1; y >= floor; --y) {
                    for (int z = -zRadius; z <= zRadius; ++z) {
                        BlockPos blockpos = pos.offset(x, y, z);
                        BlockState state = states[x + xRadius][y + 1][z + zRadius];
                        if (x != -xRadius && y != floor && z != -zRadius && x != xRadius && y != roof && z != zRadius) {
                            if (!state.is(Blocks.CHEST)) world.setBlock(blockpos, CAVE_AIR, 2);
                        }
                        else if (y > floor && !states[x + xRadius][y - 1 + 1][z + zRadius].isSolid()) {
                            world.setBlock(blockpos, CAVE_AIR, 2);
                        }
                        else if (state.isSolid() && !state.is(Blocks.CHEST)) {
                            if (y == floor) {
                                world.setBlock(blockpos, BRICKS[rand.nextInt(3)], 2);
                            }
                            else {
                                world.setBlock(blockpos, rand.nextBoolean() ? BRICK : BRICKS[rand.nextInt(3)], 2);
                            }
                        }
                    }
                }
            }

            int xChestRadius = xRadius - 1;
            int zChestRadius = zRadius - 1;
            for (int chests = 0; chests < 2; ++chests) {
                for (int attempts = 0; attempts < 3; ++attempts) {
                    boolean wall = rand.nextBoolean(); // Which wall will select a constant position;
                    int x = wall ? rand.nextBoolean() ? -xChestRadius : xChestRadius : rand.nextInt(xChestRadius * 2 + 1) - xChestRadius;
                    int y = 0;
                    int z = !wall ? rand.nextBoolean() ? -zChestRadius : zChestRadius : rand.nextInt(zChestRadius * 2 + 1) - zChestRadius;
                    BlockPos blockpos2 = pos.offset(x, y, z);
                    if (world.getBlockState(blockpos2).isAir()) {
                        int nearbySolids = 0;

                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            if (world.getBlockState(blockpos2.relative(dir)).isSolid()) {
                                ++nearbySolids;
                            }
                        }

                        if (nearbySolids == 1) {
                            world.setBlock(blockpos2, StructurePiece.reorient(world, blockpos2, Blocks.CHEST.defaultBlockState()), 2);
                            RandomizableContainerBlockEntity.setLootTable(world, rand, blockpos2, BuiltInLootTables.SIMPLE_DUNGEON);
                            break;
                        }
                    }
                }
            }

            world.setBlock(pos, dev.shadowsoffire.apotheosis.adventure.Adventure.Blocks.BOSS_SPAWNER.get().defaultBlockState(), 2);
            AdventureModule.debugLog(pos, "Boss Dungeon");

            return true;
        }
        else {
            return false;
        }
    }

}
