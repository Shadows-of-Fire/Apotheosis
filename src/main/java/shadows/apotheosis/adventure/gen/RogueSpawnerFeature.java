package shadows.apotheosis.adventure.gen;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.spawner.RandomSpawnerManager;
import shadows.apotheosis.adventure.spawner.SpawnerItem;

public class RogueSpawnerFeature extends Feature<NoneFeatureConfiguration> {

    public static final RogueSpawnerFeature INSTANCE = new RogueSpawnerFeature();
    public static final Predicate<BlockState> STONE_TEST = b -> OreFeatures.NATURAL_STONE.test(b, null);

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
        if (rand.nextInt(100) == 0) {
            BlockState state = world.getBlockState(pos);
            BlockState downState = world.getBlockState(pos.below());
            BlockState upState = world.getBlockState(pos.above());
            if (STONE_TEST.test(downState) && upState.isAir() && (state.isAir() || STONE_TEST.test(state))) {
                SpawnerItem item = RandomSpawnerManager.INSTANCE.getRandomItem(rand);
                if (item == null) return false;
                item.place(world, pos, rand);
                AdventureModule.debugLog(pos, "Rogue Spawner - " + item.getId());
                return true;
            }
        }
        return false;
    }

}
