package shadows.apotheosis.deadly.gen;

import java.util.List;
import java.util.Random;
import com.google.gson.annotations.SerializedName;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import shadows.apotheosis.deadly.DeadlyLoot;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.util.SpawnerStats;
import shadows.apotheosis.util.Weighted;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;
import shadows.placebo.util.ChestBuilder;

public class SpawnerItem extends Weighted implements PlaceboJsonReloadListener.TypeKeyed<SpawnerItem> {

    public static final Block[] FILLER_BLOCKS = new Block[] { Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.CRYING_OBSIDIAN, Blocks.LODESTONE };

    protected final SpawnerStats stats;
    @SerializedName("spawn_potentials")
    protected final WeightedRandomList<Wrapper<SpawnData>> spawnPotentials;
    @SerializedName("loot_table")
    protected final ResourceLocation lootTable;
    protected ResourceLocation id;
    private SerializerBuilder<SpawnerItem>.Serializer serializer;

    public SpawnerItem(SpawnerStats stats, ResourceLocation lootTable, List<Wrapper<SpawnData>> potentials, int weight) {
        super(weight);
        this.stats = stats;
        this.lootTable = lootTable;
        this.spawnPotentials = WeightedRandomList.create(potentials);
    }

    public void place(ServerLevelAccessor world, BlockPos pos, Random rand) {
        world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
        var blockEntity = world.getBlockEntity(pos);
        if(blockEntity instanceof SpawnerBlockEntity spawnerEntity)
        {
            var spawner = spawnerEntity.getSpawner();

            this.stats.apply(spawner);
            var randomSpawnData = spawnPotentials.getRandom(rand);
            spawner.nextSpawnData = randomSpawnData.isPresent() ? randomSpawnData.get().getData() : new SpawnData();

            SimpleWeightedRandomList.Builder<SpawnData> builder = SimpleWeightedRandomList.builder();
            spawnPotentials.unwrap().forEach(w -> builder.add(w.getData(), w.getWeight().asInt()));
            spawner.spawnPotentials = builder.build();

            int chance = DeadlyConfig.spawnerValueChance;
            ChestBuilder.place(world, rand, pos.below(), chance > 0 && rand.nextInt(chance) == 0 ? DeadlyLoot.VALUABLE : this.lootTable);
            world.setBlock(pos.above(), FILLER_BLOCKS[rand.nextInt(FILLER_BLOCKS.length)].defaultBlockState(), 2);
            for (Direction f : Direction.Plane.HORIZONTAL) {
                if (world.getBlockState(pos.relative(f)).isAir()) {
                    BooleanProperty side = (BooleanProperty) Blocks.VINE.getStateDefinition().getProperty(f.getOpposite().getName());
                    world.setBlock(pos.relative(f), Blocks.VINE.defaultBlockState().setValue(side, true), 2);
                }
            }
        }
    }

    @Override
    public void setId(ResourceLocation id) {
        if (this.id != null) throw new UnsupportedOperationException();
        this.id = id;
    }

    @Override
    public void setSerializer(SerializerBuilder<SpawnerItem>.Serializer serializer) {
        if (this.serializer != null) throw new UnsupportedOperationException();
        this.serializer = serializer;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public SerializerBuilder<SpawnerItem>.Serializer getSerializer() {
        return this.serializer;
    }
}