package shadows.apotheosis.deadly.gen;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.reload.RandomSpawnerManager;

import static net.minecraft.data.worldgen.features.OreFeatures.NATURAL_STONE;

public class RogueSpawnerFeature extends Feature<NoneFeatureConfiguration> {
    public RogueSpawnerFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        var world = ctx.level();
        var pos = ctx.origin();
        var rand = ctx.random();

        if (!DeadlyConfig.DIM_WHITELIST.contains(world.getLevel().dimension().location())) {
//            DeadlyModule.LOGGER.info("Cannot spawn rogue spawner, bad dimension");
            return false;
        }
        BlockState state = world.getBlockState(pos);
        BlockState downState = world.getBlockState(pos.below());
        BlockState upState = world.getBlockState(pos.above());
        if (NATURAL_STONE.test(downState, rand) &&
                upState.isAir() &&
                (state.isAir() || NATURAL_STONE.test(state, rand))
        ) {
            var randomSpawner = RandomSpawnerManager.INSTANCE.getRandomItem(rand);
            if(randomSpawner.isEmpty()) {
//                DeadlyModule.LOGGER.info("Failed to get random rogue spawner!");
                return false;
            }

            randomSpawner.get().place(world, pos, rand);
            DeadlyModule.debugLog(pos, "Rogue Spawner");
            return true;
        }
//        DeadlyModule.LOGGER.info("Cannot spawn rogue spawner, failed placement condidtions at pos: {}", pos);
        return false;
    }
}