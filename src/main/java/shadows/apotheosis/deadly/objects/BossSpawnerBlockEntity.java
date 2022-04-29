package shadows.apotheosis.deadly.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.deadly.loot.BossItem;
import shadows.apotheosis.deadly.reload.BossItemManager;

public class BossSpawnerBlockEntity extends BlockEntity {

    protected BossItem item;
    protected int ticks = 0;

    public BossSpawnerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(Apoth.Tiles.BOSS_SPAWN_TILE, pWorldPosition, pBlockState);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BossSpawnerBlockEntity blockEntity) {
        if (pLevel.isClientSide) return;
        if (blockEntity.ticks++ % 40 == 0 && pLevel.getEntities(EntityType.PLAYER, new AABB(blockEntity.worldPosition).inflate(8, 8, 8), EntitySelector.NO_SPECTATORS).stream().anyMatch(p -> !p.isCreative())) {
            pLevel.setBlockAndUpdate(blockEntity.worldPosition, Blocks.AIR.defaultBlockState());
            if (blockEntity.item != null) {
                Mob entity = blockEntity.item.createBoss((ServerLevel) pLevel, blockEntity.worldPosition, pLevel.getRandom());
                entity.setPersistenceRequired();
                pLevel.addFreshEntity(entity);
            }
        }
    }

    public BlockPos getPos() {
        return this.worldPosition;
    }

    public void setBossItem(BossItem item) {
        this.item = item;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putString("boss_item", this.item.getId().toString());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        this.item = BossItemManager.INSTANCE.getById(new ResourceLocation(tag.getString("boss_item")));
        super.load(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
    }

}
