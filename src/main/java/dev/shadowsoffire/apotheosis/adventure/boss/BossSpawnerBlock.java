package dev.shadowsoffire.apotheosis.adventure.boss;

import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.block_entity.TickingEntityBlock;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BossSpawnerBlock extends Block implements TickingEntityBlock {

    private static final VoxelShape OCC_SHAPE = Shapes.box(0, 0, 0, 0, 15.99, 0);

    public BossSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BossSpawnerTile(pPos, pState);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return OCC_SHAPE;
    }

    public static class BossSpawnerTile extends BlockEntity implements TickingBlockEntity {

        protected DynamicHolder<ApothBoss> item = BossRegistry.INSTANCE.emptyHolder();
        protected int ticks = 0;

        public BossSpawnerTile(BlockPos pos, BlockState state) {
            super(Apoth.Tiles.BOSS_SPAWNER.get(), pos, state);
        }

        @Override
        public void serverTick(Level pLevel, BlockPos pPos, BlockState pState) {
            if (this.ticks++ % 40 == 0) {
                Optional<Player> opt = this.level.getEntities(EntityType.PLAYER, new AABB(this.worldPosition).inflate(8, 8, 8), EntitySelector.NO_CREATIVE_OR_SPECTATOR).stream().findFirst();
                opt.ifPresent(player -> {
                    this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
                    BlockPos pos = this.worldPosition;
                    ApothBoss bossItem = !this.item.isBound() ? BossRegistry.INSTANCE.getRandomItem(this.level.getRandom(), player.getLuck(), IDimensional.matches(this.level), IStaged.matches(player)) : this.item.get();
                    if (bossItem == null) {
                        AdventureModule.LOGGER.error("A boss spawner attempted to spawn a boss at {} in {}, but no bosses were available!", this.getBlockPos(), this.level.dimension().location());
                        return;
                    }
                    Mob entity = bossItem.createBoss((ServerLevel) this.level, pos, this.level.getRandom(), player.getLuck());
                    entity.setTarget(player);
                    entity.setPersistenceRequired();
                    ((ServerLevel) this.level).addFreshEntityWithPassengers(entity);
                });
            }
        }

        public void setBossItem(DynamicHolder<ApothBoss> item) {
            this.item = item;
        }

        @Override
        public void saveAdditional(CompoundTag tag) {
            if (this.item != null) tag.putString("boss_item", this.item.getId().toString());
            super.saveAdditional(tag);
        }

        @Override
        public void load(CompoundTag tag) {
            this.item = BossRegistry.INSTANCE.holder(new ResourceLocation(tag.getString("boss_item")));
            super.load(tag);
        }

    }

}
