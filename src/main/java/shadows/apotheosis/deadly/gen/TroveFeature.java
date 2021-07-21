package shadows.apotheosis.deadly.gen;

import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;

public class TroveFeature extends Feature<NoFeatureConfig> {

	private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
	private static final Block[] ORES = new Block[] { Blocks.IRON_ORE, Blocks.COAL_ORE, Blocks.GOLD_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE };
	public static final Predicate<BlockState> STONE_TEST = s -> FillerBlockType.NATURAL_STONE.test(s, null);

	public static final TroveFeature INSTANCE = new TroveFeature();

	public TroveFeature() {
		super(NoFeatureConfig.CODEC);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean place(ISeedReader world, ChunkGenerator gen, Random rand, BlockPos pos, NoFeatureConfig cfg) {
		if (!DeadlyConfig.canGenerateIn(world)) return false;
		int xRadius = 2 + rand.nextInt(2);
		int floor = -1;
		int tntLevel = -2;
		int undercoat = -3;
		int roof = 3;
		int zRadius = 2 + rand.nextInt(2);
		int doors = 0;

		BlockState[][][] states = new BlockState[xRadius * 2 + 1][7][zRadius * 2 + 1];

		for (int x = -xRadius; x <= xRadius; ++x) {
			for (int y = undercoat; y <= roof; ++y) {
				for (int z = -zRadius; z <= zRadius; ++z) {
					BlockPos blockpos = pos.offset(x, y, z);
					BlockState state = world.getBlockState(blockpos);
					Material material = state.getMaterial();
					boolean flag = material.isSolid() && material.isSolidBlocking();
					if (y <= floor && !flag) { return false; } //Exit if the floor is not fully solid.

					if (y == roof && !flag) { return false; } //Exit if the roof is not fully solid.

					if ((Math.abs(x) == xRadius && Math.abs(z) != zRadius || Math.abs(z) == zRadius && Math.abs(x) != xRadius) && y == 1 && state.isAir() && states[x + xRadius][y - 1 + 3][z + zRadius].isAir()) {
						++doors; //Count number of 2x1 holes at y=0 in non-corners
					}
					states[x + xRadius][y + 3][z + zRadius] = state;
				}
			}
		}

		if (doors >= 1 && doors <= 2) {
			for (int x = -xRadius; x <= xRadius; ++x) {
				for (int y = tntLevel; y < roof; ++y) {
					for (int z = -zRadius; z <= zRadius; ++z) {
						BlockPos blockpos = pos.offset(x, y, z);
						BlockState state = states[x + xRadius][y + 3][z + zRadius];
						if (y == tntLevel && Math.abs(x) <= 1 && Math.abs(z) <= 1) { //Spawn a 3x3x1 of TNT in the center
							if (!state.is(Blocks.CHEST)) world.setBlock(blockpos, Blocks.TNT.defaultBlockState(), 2);
						} else if (Math.abs(x) == xRadius || Math.abs(z) == zRadius) { //In the walls
							if (y == 0 && state.isAir(world, blockpos) && states[x + xRadius][y + 1 + 3][z + zRadius].isAir(world, blockpos.above())) { //Replace the doors with cobwebs
								world.setBlock(blockpos, Blocks.COBWEB.defaultBlockState(), 2);
								states[x + xRadius][y + 3][z + zRadius] = Blocks.COBWEB.defaultBlockState();
								world.setBlock(blockpos.above(), Blocks.COBWEB.defaultBlockState(), 2);
								states[x + xRadius][y + 1 + 3][z + zRadius] = Blocks.COBWEB.defaultBlockState();
								int xModif = x == xRadius ? -1 : x == -xRadius ? 1 : 0;
								int zModif = z == zRadius ? -1 : z == -zRadius ? 1 : 0;
								BlockPos inward = blockpos.offset(xModif, 0, zModif);
								world.setBlock(inward, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 2);
								states[x + xModif + xRadius][y + 3][z + zModif + zRadius] = Blocks.STONE_PRESSURE_PLATE.defaultBlockState();
								if (x > 2 || z > 2) world.setBlock(inward.below(2), Blocks.TNT.defaultBlockState(), 2);
							} else if (STONE_TEST.test(state) || state.isAir(world, blockpos)) {
								if (rand.nextFloat() < 0.75F) world.setBlock(blockpos, ORES[rand.nextInt(ORES.length)].defaultBlockState(), 2);
								else world.setBlock(blockpos, Blocks.STONE.defaultBlockState(), 2);
							}
						} else if (y >= 0 && y < roof && state.getBlock() != Blocks.STONE_PRESSURE_PLATE) {
							world.setBlock(blockpos, CAVE_AIR, 2);
						}
					}
				}
			}

			DeadlyModule.debugLog(pos, "Ore Trove");

			return true;
		} else {
			return false;
		}
	}

}
