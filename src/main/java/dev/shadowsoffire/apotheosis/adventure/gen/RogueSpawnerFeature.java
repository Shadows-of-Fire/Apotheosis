package dev.shadowsoffire.apotheosis.adventure.gen;

import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.spawner.RandomSpawnerManager;
import dev.shadowsoffire.apotheosis.adventure.spawner.SpawnerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class RogueSpawnerFeature extends Feature<NoneFeatureConfiguration> {

    public static final RogueSpawnerFeature INSTANCE = new RogueSpawnerFeature();
    public static final RuleTest STONE_TEST = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);

    public RogueSpawnerFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel world = ctx.level();
        if (!AdventureConfig.canGenerateIn(world)) return false;
        BlockPos pos = ctx.origin();
        RandomSource rand = ctx.random();
        BlockState state = world.getBlockState(pos);
        BlockState downState = world.getBlockState(pos.below());
        BlockState upState = world.getBlockState(pos.above());
        if (STONE_TEST.test(downState, rand) && upState.isAir() && (state.isAir() || STONE_TEST.test(state, rand))) {
            SpawnerItem item = RandomSpawnerManager.INSTANCE.getRandomItem(rand);
            item.place(world, pos, rand);
            AdventureModule.debugLog(pos, "Rogue Spawner - " + item.getId());
            return true;
        }
        return false;
    }

}
