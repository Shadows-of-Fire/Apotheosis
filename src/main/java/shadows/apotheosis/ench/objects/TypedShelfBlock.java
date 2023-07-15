package shadows.apotheosis.ench.objects;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import shadows.apotheosis.ench.api.IEnchantingBlock;

public class TypedShelfBlock extends Block implements IEnchantingBlock {

	protected final Supplier<? extends ParticleOptions> particle;

	public TypedShelfBlock(Properties props, Supplier<? extends ParticleOptions> particle) {
		super(props);
		this.particle = particle;
	}

	@Override
	public ParticleOptions getTableParticle(BlockState state) {
		return this.particle.get();
	}

	public static class SculkShelfBlock extends TypedShelfBlock {

		public SculkShelfBlock(Properties props, Supplier<? extends ParticleOptions> particle) {
			super(props, particle);
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
			if (rand.nextInt(100) == 0) {
				level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + rand.nextFloat() * 0.4F, true);
			}
		}

	}

}
