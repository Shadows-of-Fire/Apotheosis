package shadows.apotheosis.deadly.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.loot.LootTables;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.objects.BossSpawnerBlock.BossSpawnerTile;
import shadows.apotheosis.deadly.reload.BossItemManager;

/**
 * Boss Dungeon Feature (Variant 2) - Credit to BigAl607 on discord for the structure.
 */
public class BossDungeonFeature2 extends Feature<NoFeatureConfig> {

	public static final ResourceLocation TEMPLATE_ID = new ResourceLocation(Apotheosis.MODID, "boss_1");
	public static final BossDungeonFeature2 INSTANCE = new BossDungeonFeature2();

	protected static int xRadius = 4;
	protected static int floor = -1;
	protected static int roof = 3;
	protected static int roofTop = 6;
	protected static int zRadius = 4;

	public BossDungeonFeature2() {
		super(NoFeatureConfig.field_236558_a_);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean generate(ISeedReader world, ChunkGenerator gen, Random rand, BlockPos pos, NoFeatureConfig cfg) {
		if (!DeadlyConfig.canGenerateIn(world)) return false;
		BlockState[][][] states = new BlockState[9][8][9];

		int doors = 0;

		for (int x = -xRadius; x <= xRadius; ++x) {
			for (int y = floor; y <= roofTop; ++y) {
				for (int z = -zRadius; z <= zRadius; ++z) {
					BlockPos blockpos = pos.add(x, y, z);
					BlockState state = world.getBlockState(blockpos);
					Material material = state.getMaterial();
					boolean flag = material.isSolid();
					if (y == floor && !flag) { return false; } //Exit if the floor is not fully solid.

					if (y == roof && !flag) { return false; } //Exit if the roof is not fully solid.
					if (y == roof + 1 && Math.abs(x) < xRadius && Math.abs(z) < zRadius && !flag) { return false; } //Exit if the roof is not fully solid.

					if (isDoorSpace(x, z) && y == 1 && state.isAir() && states[x + xRadius][y - 1 + 1][z + zRadius].isAir()) {
						++doors; //Count number of 2x1 holes in the walls that span y = (0,1)
					}

					states[x + xRadius][y + 1][z + zRadius] = state;
				}
			}
		}

		if (doors >= 3) { //3 Doors, a floor, and a roof.  Good enough for me!

			Template template = ServerLifecycleHooks.getCurrentServer().getTemplateManager().getTemplate(TEMPLATE_ID);
			template.func_237152_b_(world, pos.add(-4, -1, -4), new PlacementSettings(), rand);

			boolean rand1 = rand.nextBoolean();
			boolean rand2 = rand.nextBoolean();

			BlockPos chest1 = pos.add(rand1 ? xRadius - 1 : -xRadius + 1, 0, rand2 ? zRadius - 1 : -zRadius + 1);
			BlockPos chest2 = pos.add(!rand1 ? xRadius - 1 : -xRadius + 1, 0, !rand2 ? zRadius - 1 : -zRadius + 1);

			world.setBlockState(chest1, StructurePiece.correctFacing(world, chest1, Blocks.CHEST.getDefaultState()), 2);
			LockableLootTileEntity.setLootTable(world, rand, chest1, LootTables.CHESTS_SIMPLE_DUNGEON);
			world.setBlockState(chest2, StructurePiece.correctFacing(world, chest2, Blocks.CHEST.getDefaultState()), 2);
			LockableLootTileEntity.setLootTable(world, rand, chest2, LootTables.CHESTS_SIMPLE_DUNGEON);

			world.setBlockState(pos, ApotheosisObjects.BOSS_SPAWNER.getDefaultState(), 2);
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof BossSpawnerTile) {
				((BossSpawnerTile) tileentity).setBossItem(BossItemManager.INSTANCE.getRandomItem(rand));
			} else {
				DeadlyModule.LOGGER.error("Failed to fetch boss spawner entity at ({}, {}, {})", pos.getX(), pos.getY(), pos.getZ());
			}
			DeadlyModule.debugLog(pos, "Boss Dungeon (Variant 2)");
			return true;
		}
		return false;

	}

	static boolean isDoorSpace(int x, int z) {
		return Math.abs(z) == zRadius && x >= -1 && x <= 1 || Math.abs(x) == xRadius && z >= -1 && z <= 1;
	}

}
