package shadows.apotheosis.adventure.spawner;

import java.util.Random;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.util.SpawnerStats;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;
import shadows.placebo.util.ChestBuilder;

public class SpawnerItem extends TypeKeyedBase<SpawnerItem> implements WeightedEntry {

	public static final Block[] FILLER_BLOCKS = new Block[] { Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.CRYING_OBSIDIAN, Blocks.LODESTONE };

	@Expose(deserialize = false)
	private Weight _weight;
	protected final int weight;
	protected final SpawnerStats stats;
	@SerializedName("spawn_potentials")
	protected final SimpleWeightedRandomList<SpawnData> spawnPotentials;
	@SerializedName("loot_table")
	protected final ResourceLocation lootTable;

	public SpawnerItem(SpawnerStats stats, ResourceLocation lootTable, SimpleWeightedRandomList<SpawnData> potentials, int weight) {
		this.weight = weight;
		this.stats = stats;
		this.lootTable = lootTable;
		this.spawnPotentials = potentials;
	}

	@Override
	public Weight getWeight() {
		if (this._weight == null) this._weight = Weight.of(this.weight);
		return this._weight;
	}

	@SuppressWarnings("deprecation")
	public void place(WorldGenLevel world, BlockPos pos, Random rand) {
		world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
		SpawnerBlockEntity entity = (SpawnerBlockEntity) world.getBlockEntity(pos);
		this.stats.apply(entity);
		entity.spawner.spawnPotentials = this.spawnPotentials;
		entity.spawner.setNextSpawnData(null, pos, this.spawnPotentials.getRandomValue(rand).get());
		ChestBuilder.place(world, rand, pos.below(), rand.nextFloat() <= AdventureConfig.spawnerValueChance ? Apoth.LootTables.CHEST_VALUABLE : this.lootTable);
		world.setBlock(pos.above(), FILLER_BLOCKS[rand.nextInt(FILLER_BLOCKS.length)].defaultBlockState(), 2);
		for (Direction f : Plane.HORIZONTAL) {
			if (world.getBlockState(pos.relative(f)).isAir()) {
				BooleanProperty side = (BooleanProperty) Blocks.VINE.getStateDefinition().getProperty(f.getOpposite().getName());
				world.setBlock(pos.relative(f), Blocks.VINE.defaultBlockState().setValue(side, true), 2);
			}
		}
	}

}