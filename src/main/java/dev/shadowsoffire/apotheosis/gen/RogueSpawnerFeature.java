package dev.shadowsoffire.apotheosis.gen;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawner;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawnerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class RogueSpawnerFeature extends Feature<SuccessChanceFeatureConfig> {

    public static final RuleTest STONE_TEST = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);

    public RogueSpawnerFeature() {
        super(SuccessChanceFeatureConfig.CODEC);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(FeaturePlaceContext<SuccessChanceFeatureConfig> ctx) {
        WorldGenLevel world = ctx.level();
        BlockPos pos = ctx.origin();
        RandomSource rand = ctx.random();
        if (!AdventureConfig.canGenerateIn(world) || rand.nextFloat() > ctx.config().successChance()) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        BlockState downState = world.getBlockState(pos.below());
        BlockState upState = world.getBlockState(pos.above());
        if (STONE_TEST.test(downState, rand) && upState.isAir() && (state.isAir() || STONE_TEST.test(state, rand))) {
            RogueSpawner item = RogueSpawnerRegistry.INSTANCE.getRandomItem(rand);
            if (item == null) {
                return false;
            }
            item.place(world, pos, rand);
            Apotheosis.debugLog(pos, "Rogue Spawner - " + RogueSpawnerRegistry.INSTANCE.getKey(item));
            return true;
        }

        return false;
    }

}
