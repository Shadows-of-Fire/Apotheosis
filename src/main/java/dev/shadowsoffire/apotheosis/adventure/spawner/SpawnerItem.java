package dev.shadowsoffire.apotheosis.adventure.spawner;

import com.google.gson.annotations.SerializedName;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.util.SpawnerStats;
import dev.shadowsoffire.placebo.json.PSerializer;
import dev.shadowsoffire.placebo.reload.TypeKeyed.TypeKeyedBase;
import dev.shadowsoffire.placebo.reload.WeightedJsonReloadListener.ILuckyWeighted;
import dev.shadowsoffire.placebo.util.ChestBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SpawnerItem extends TypeKeyedBase<SpawnerItem> implements ILuckyWeighted {

    public static final PSerializer<SpawnerItem> SERIALIZER = PSerializer.basic("Rogue Spawner", obj -> RandomSpawnerManager.GSON.fromJson(obj, SpawnerItem.class));

    public static final Block[] FILLER_BLOCKS = { Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.CRYING_OBSIDIAN, Blocks.LODESTONE };

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
    public int getWeight() {
        return this.weight;
    }

    @Override
    public float getQuality() {
        return 0;
    }

    @SuppressWarnings("deprecation")
    public void place(WorldGenLevel world, BlockPos pos, RandomSource rand) {
        world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        SpawnerBlockEntity entity = (SpawnerBlockEntity) world.getBlockEntity(pos);
        this.stats.apply(entity);
        entity.spawner.spawnPotentials = this.spawnPotentials;
        entity.spawner.setNextSpawnData(null, pos, this.spawnPotentials.getRandomValue(rand).get());
        ChestBuilder.place(world, pos.below(), rand.nextFloat() <= AdventureConfig.spawnerValueChance ? Apoth.LootTables.CHEST_VALUABLE : this.lootTable);
        world.setBlock(pos.above(), FILLER_BLOCKS[rand.nextInt(FILLER_BLOCKS.length)].defaultBlockState(), 2);
        for (Direction f : Plane.HORIZONTAL) {
            if (world.getBlockState(pos.relative(f)).isAir()) {
                BooleanProperty side = (BooleanProperty) Blocks.VINE.getStateDefinition().getProperty(f.getOpposite().getName());
                world.setBlock(pos.relative(f), Blocks.VINE.defaultBlockState().setValue(side, true), 2);
            }
        }
    }

    @Override
    public PSerializer<? extends SpawnerItem> getSerializer() {
        return SERIALIZER;
    }

}
