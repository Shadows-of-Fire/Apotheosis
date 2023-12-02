package dev.shadowsoffire.apotheosis.ench.objects;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.ench.api.IEnchantingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class TreasureShelfBlock extends TypedShelfBlock implements IEnchantingBlock {

    public TreasureShelfBlock(Properties props) {
        super(props, Apoth.Particles.ENCHANT_SCULK);
    }

    @Override
    public boolean allowsTreasure(BlockState state, LevelReader world, BlockPos pos) {
        return true;
    }

    @Override
    public float getQuantaBonus(BlockState state, LevelReader world, BlockPos pos) {
        return -10F;
    }

    @Override
    public float getArcanaBonus(BlockState state, LevelReader world, BlockPos pos) {
        return 10F;
    }

}
