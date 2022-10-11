package shadows.apotheosis.adventure.affix.reforging;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import shadows.apotheosis.Apoth;
import shadows.placebo.block_entity.TickingBlockEntity;

public class ReforgingTableTile extends BlockEntity implements TickingBlockEntity {

	public int time = 0;
	public boolean step1 = true;

	public ReforgingTableTile(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Apoth.Tiles.REFORGING_TABLE, pWorldPosition, pBlockState);
	}

	@Override
	public void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {
		time++;

		if (step1 && time == 59) {
			step1 = false;
			time = 0;
		} else if (time == 4 && !step1) {
			Random rand = pLevel.random;
			for (int i = 0; i < 6; i++) {
				pLevel.addParticle(ParticleTypes.CRIT, pPos.getX() + 0.5 + 0.2 * rand.nextDouble(), pPos.getY() + 13 / 16D, pPos.getZ() + 0.5 + 0.2 * rand.nextDouble(), 0, 0, 0);
			}
			pLevel.playLocalSound(pPos.getX(), pPos.getY(), pPos.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.005F, 1.7F + rand.nextFloat() * 0.2F, true);
			step1 = true;
			time = 0;
		}
	}

}
