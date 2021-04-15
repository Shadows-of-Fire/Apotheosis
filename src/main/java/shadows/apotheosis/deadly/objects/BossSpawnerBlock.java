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
			if (this.world.isRemote) return;
			if (this.ticks++ % 40 == 0 && this.world.getEntitiesWithinAABB(EntityType.PLAYER, new AxisAlignedBB(this.pos).grow(8, 8, 8), EntityPredicates.NOT_SPECTATING).stream().anyMatch(p -> !p.isCreative())) {
				this.world.setBlockState(this.pos, Blocks.AIR.getDefaultState());
				BlockPos pos = this.pos;
				if (this.item != null) {
					MobEntity entity = this.item.createBoss((ServerWorld) this.world, pos, this.world.getRandom());
					entity.enablePersistence();
					this.world.addEntity(entity);
				}
			}
		}

		public void setBossItem(BossItem item) {
			this.item = item;
		}

		@Override
		public CompoundNBT write(CompoundNBT tag) {
			tag.putString("boss_item", this.item.getId().toString());
			return super.write(tag);
		}

		@Override
		public void read(BlockState state, CompoundNBT tag) {
			this.item = BossItemManager.INSTANCE.getById(new ResourceLocation(tag.getString("boss_item")));
			super.read(state, tag);
		}

	}

}
