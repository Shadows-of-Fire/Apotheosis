package shadows.apotheosis.deadly.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.gen.BossItem;
import shadows.apotheosis.deadly.reload.BossItemManager;
import shadows.placebo.block_entity.TickingBlockEntity;

public class BossSpawnerBlock extends BaseEntityBlock {

	public BossSpawnerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BossSpawnerTile(pos, state);
	}

	public static class BossSpawnerTile extends BlockEntity implements TickingBlockEntity {

		protected BossItem item;
		protected int ticks = 0;

		public BossSpawnerTile(BlockPos pos, BlockState state) {
			super(ApotheosisObjects.BOSS_SPAWN_TILE, pos, state);
		}

		@Override
		public void serverTick(Level level, BlockPos pos, BlockState state) {
			if (this.ticks++ % 40 == 0 && level.getEntities(EntityType.PLAYER, new AABB(pos).inflate(8, 8, 8), EntitySelector.NO_SPECTATORS).stream().anyMatch(p -> !p.isCreative())) {
				this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
				if (this.item != null) {
					Mob entity = this.item.createBoss((ServerLevel) level, pos, level.getRandom());
					entity.setPersistenceRequired();
					this.level.addFreshEntity(entity);
				}
			}
		}

		public void setBossItem(BossItem item) {
			this.item = item;
		}

		@Override
		public CompoundTag save(CompoundTag tag) {
			tag.putString("boss_item", this.item.getId().toString());
			return super.save(tag);
		}

		@Override
		public void load(CompoundTag tag) {
			this.item = BossItemManager.INSTANCE.getById(new ResourceLocation(tag.getString("boss_item")));
			super.load(tag);
		}

	}

}
