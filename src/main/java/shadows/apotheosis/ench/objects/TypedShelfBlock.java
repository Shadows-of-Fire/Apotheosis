package shadows.apotheosis.ench.objects;

import java.util.function.Supplier;

import net.minecraft.core.particles.ParticleOptions;
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

}
