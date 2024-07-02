package dev.shadowsoffire.apotheosis.adventure.spawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.util.SpawnerStats;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
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
import net.minecraftforge.registries.ForgeRegistries;

public class RogueSpawner implements CodecProvider<RogueSpawner>, ILuckyWeighted {

    public static final Codec<RogueSpawner> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.INT.fieldOf("weight").forGetter(RogueSpawner::getWeight),
            SpawnerStats.CODEC.fieldOf("stats").forGetter(RogueSpawner::getStats),
            ResourceLocation.CODEC.fieldOf("loot_table").forGetter(RogueSpawner::getLootTableId),
            SimpleWeightedRandomList.wrappedCodec(SpawnData.CODEC).fieldOf("spawn_potentials").forGetter(s -> s.spawnPotentials))
        .apply(inst, RogueSpawner::new));

    protected final int weight;
    protected final SpawnerStats stats;
    protected final ResourceLocation lootTable;
    protected final SimpleWeightedRandomList<SpawnData> spawnPotentials;

    public RogueSpawner(int weight, SpawnerStats stats, ResourceLocation lootTable, SimpleWeightedRandomList<SpawnData> potentials) {
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

    public SpawnerStats getStats() {
        return this.stats;
    }

    public ResourceLocation getLootTableId() {
        return this.lootTable;
    }

    @SuppressWarnings("deprecation")
    public void place(WorldGenLevel world, BlockPos pos, RandomSource rand) {
        world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        SpawnerBlockEntity entity = (SpawnerBlockEntity) world.getBlockEntity(pos);
        this.stats.apply(entity);
        entity.spawner.spawnPotentials = this.spawnPotentials;
        entity.spawner.setNextSpawnData(null, pos, this.spawnPotentials.getRandomValue(rand).get());
        ChestBuilder.place(world, pos.below(), rand.nextFloat() <= AdventureConfig.spawnerValueChance ? Apoth.LootTables.CHEST_VALUABLE : this.lootTable);
        Block cover = ForgeRegistries.BLOCKS.tags().getTag(Apoth.Tags.ROGUE_SPAWNER_COVERS).getRandomElement(rand).orElse(Blocks.STONE);
        world.setBlock(pos.above(), cover.defaultBlockState(), 2);
        for (Direction f : Plane.HORIZONTAL) {
            if (world.getBlockState(pos.relative(f)).isAir()) {
                BooleanProperty side = (BooleanProperty) Blocks.VINE.getStateDefinition().getProperty(f.getOpposite().getName());
                world.setBlock(pos.relative(f), Blocks.VINE.defaultBlockState().setValue(side, true), 2);
            }
        }
    }

    @Override
    public Codec<? extends RogueSpawner> getCodec() {
        return CODEC;
    }

}
