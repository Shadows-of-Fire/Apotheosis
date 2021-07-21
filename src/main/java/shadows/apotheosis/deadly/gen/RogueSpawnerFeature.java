package shadows.apotheosis.deadly.gen;

import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.reload.RandomSpawnerManager;

public class RogueSpawnerFeature extends Feature<NoFeatureConfig> {

	public static final RogueSpawnerFeature INSTANCE = new RogueSpawnerFeature();
	public static final Predicate<BlockState> STONE_TEST = b -> FillerBlockType.NATURAL_STONE.test(b, null);

	public RogueSpawnerFeature() {
		super(NoFeatureConfig.CODEC);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean place(ISeedReader world, ChunkGenerator gen, Random rand, BlockPos pos, NoFeatureConfig cfg) {
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
