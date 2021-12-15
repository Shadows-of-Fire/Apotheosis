package shadows.apotheosis.deadly.gen;

import java.util.List;
import java.util.Random;

import com.google.gson.annotations.SerializedName;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import shadows.apotheosis.deadly.DeadlyLoot;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.util.SpawnerStats;
import shadows.placebo.util.ChestBuilder;
import shadows.placebo.util.SpawnerEditor;

public class SpawnerItem extends WeighedRandom.WeighedRandomItem {

	public static final Block[] FILLER_BLOCKS = new Block[] { Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.CRYING_OBSIDIAN, Blocks.LODESTONE };

	protected final SpawnerStats stats;
	@SerializedName("spawn_potentials")
	protected final List<SpawnData> spawnPotentials;
	@SerializedName("loot_table")
	protected final ResourceLocation lootTable;

	public SpawnerItem(SpawnerStats stats, ResourceLocation lootTable, List<SpawnData> potentials, int weight) {
		super(weight);
		this.stats = stats;
		this.lootTable = lootTable;
		this.spawnPotentials = potentials;
	}

	@SuppressWarnings("deprecation")
	public void place(ServerLevelAccessor world, BlockPos pos, Random rand) {
		world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
		SpawnerEditor editor = new SpawnerEditor(world, pos);
		this.stats.apply(editor).setSpawnData(this.spawnPotentials.get(rand.nextInt(this.spawnPotentials.size()))).setPotentials(this.spawnPotentials.toArray(new SpawnData[0]));
		int chance = DeadlyConfig.spawnerValueChance;
		ChestBuilder.place(world, rand, pos.below(), chance > 0 && rand.nextInt(chance) == 0 ? DeadlyLoot.VALUABLE : this.lootTable);
		world.setBlock(pos.above(), FILLER_BLOCKS[rand.nextInt(FILLER_BLOCKS.length)].defaultBlockState(), 2);
		for (Direction f : Plane.HORIZONTAL) {
			if (world.getBlockState(pos.relative(f)).isAir(world, pos.relative(f))) {
				BooleanProperty side = (BooleanProperty) Blocks.VINE.getStateDefinition().getProperty(f.getOpposite().getName());
				world.setBlock(pos.relative(f), Blocks.VINE.defaultBlockState().setValue(side, true), 2);
			}
		}
	}

}