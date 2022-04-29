package shadows.apotheosis.deadly.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import shadows.apotheosis.Apoth;

import javax.annotation.Nullable;

public class BossSpawnerBlock extends BaseEntityBlock {

    public BossSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BossSpawnerBlockEntity(pPos, pState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, Apoth.Tiles.BOSS_SPAWN_TILE, BossSpawnerBlockEntity::tick);
    }

}