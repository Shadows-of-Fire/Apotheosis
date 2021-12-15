package shadows.apotheosis.deadly.gen;

import java.util.Random;
import java.util.function.Predicate;

import com.google.common.base.Predicates;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.reload.RandomSpawnerManager;

public class RogueSpawnerFeature extends Feature<NoneFeatureConfiguration> {

	public static final RogueSpawnerFeature INSTANCE = new RogueSpawnerFeature();
	public static final Predicate<BlockState> STONE_TEST = b -> Predicates.NATURAL_STONE.test(b, null);

	public RogueSpawnerFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator gen, Random rand, BlockPos pos, NoneFeatureConfiguration cfg) {
		if (!DeadlyConfig.canGenerateIn(world)) return false;
		BlockState state = world.getBlockState(pos);
		BlockState downState = world.getBlockState(pos.below());
		BlockState upState = world.getBlockState(pos.above());
		if (STONE_TEST.test(downState) && upState.isAir(world, pos.above()) && (state.isAir(world, pos) || STONE_TEST.test(state))) {
			RandomSpawnerManager.INSTANCE.getRandomItem(rand).place(world, pos, rand);
			DeadlyModule.debugLog(pos, "Rogue Spawner");
			return true;
		}
		return false;
	}

}
