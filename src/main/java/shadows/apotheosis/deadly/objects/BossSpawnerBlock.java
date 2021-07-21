package shadows.apotheosis.deadly.objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.gen.BossItem;
import shadows.apotheosis.deadly.reload.BossItemManager;

public class BossSpawnerBlock extends Block {

	public BossSpawnerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BossSpawnerTile();
	}

	public static class BossSpawnerTile extends TileEntity implements ITickableTileEntity {

		protected BossItem item;
		protected int ticks = 0;

		public BossSpawnerTile() {
			super(ApotheosisObjects.BOSS_SPAWN_TILE);
		}

		@Override
		public void tick() {
			if (this.level.isClientSide) return;
			if (this.ticks++ % 40 == 0 && this.level.getEntities(EntityType.PLAYER, new AxisAlignedBB(this.worldPosition).inflate(8, 8, 8), EntityPredicates.NO_SPECTATORS).stream().anyMatch(p -> !p.isCreative())) {
				this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
				BlockPos pos = this.worldPosition;
				if (this.item != null) {
					MobEntity entity = this.item.createBoss((ServerWorld) this.level, pos, this.level.getRandom());
					entity.setPersistenceRequired();
					this.level.addFreshEntity(entity);
				}
			}
		}

		public void setBossItem(BossItem item) {
			this.item = item;
		}

		@Override
		public CompoundNBT save(CompoundNBT tag) {
			tag.putString("boss_item", this.item.getId().toString());
			return super.save(tag);
		}

		@Override
		public void load(BlockState state, CompoundNBT tag) {
			this.item = BossItemManager.INSTANCE.getById(new ResourceLocation(tag.getString("boss_item")));
			super.load(state, tag);
		}

	}

}
