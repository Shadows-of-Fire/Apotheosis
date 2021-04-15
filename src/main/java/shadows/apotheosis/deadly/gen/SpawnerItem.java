package shadows.apotheosis.deadly.gen;

import java.util.List;
import java.util.Random;

import com.google.gson.annotations.SerializedName;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import shadows.apotheosis.deadly.DeadlyLoot;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.util.SpawnerStats;
import shadows.placebo.util.ChestBuilder;
import shadows.placebo.util.SpawnerEditor;

public class SpawnerItem extends WeightedRandom.Item {

	public static final Block[] FILLER_BLOCKS = new Block[] { Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.CRYING_OBSIDIAN, Blocks.LODESTONE };

	protected final SpawnerStats stats;
	@SerializedName("spawn_potentials")
	protected final List<WeightedSpawnerEntity> spawnPotentials;
	@SerializedName("loot_table")
	protected final ResourceLocation lootTable;

	public SpawnerItem(SpawnerStats stats, ResourceLocation lootTable, List<WeightedSpawnerEntity> potentials, int weight) {
		super(weight);
		this.stats = stats;
		this.lootTable = lootTable;
		this.spawnPotentials = potentials;
	}

	@SuppressWarnings("deprecation")
	public void place(IServerWorld world, BlockPos pos, Random rand) {
		world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 2);
		SpawnerEditor editor = new SpawnerEditor(world, pos);
		this.stats.apply(editor).setSpawnData(this.spawnPotentials.get(rand.nextInt(this.spawnPotentials.size()))).setPotentials(this.spawnPotentials.toArray(new WeightedSpawnerEntity[0]));
		int chance = DeadlyConfig.spawnerValueChance;
		ChestBuilder.place(world, rand, pos.down(), chance > 0 && rand.nextInt(chance) == 0 ? DeadlyLoot.VALUABLE : this.lootTable);
		world.setBlockState(pos.up(), FILLER_BLOCKS[rand.nextInt(FILLER_BLOCKS.length)].getDefaultState(), 2);
		for (Direction f : Plane.HORIZONTAL) {
			if (world.getBlockState(pos.offset(f)).isAir(world, pos.offset(f))) {
				BooleanProperty side = (BooleanProperty) Blocks.VINE.getStateContainer().getProperty(f.getOpposite().getName2());
				world.setBlockState(pos.offset(f), Blocks.VINE.getDefaultState().with(side, true), 2);
			}
		}
	}

}