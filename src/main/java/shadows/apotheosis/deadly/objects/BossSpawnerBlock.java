package shadows.apotheosis.deadly.objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.gen.BossFeatureItem;

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

		protected BossFeatureItem item;
		protected int ticks = 0;

		public BossSpawnerTile() {
			super(ApotheosisObjects.BOSS_SPAWN_TILE);
		}

		@Override
		public void tick() {
			if (world.isRemote) return;
			if (ticks++ % 40 == 0 && world.getEntitiesWithinAABB(EntityType.PLAYER, new AxisAlignedBB(this.pos).grow(8, 8, 8), EntityPredicates.NOT_SPECTATING).stream().anyMatch(p -> !p.isCreative())) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
				BlockPos pos = this.pos;
				if (item != null) item.spawnBoss((ServerWorld) world, pos, world.getRandom());
			}
		}

		public void setBossItem(BossFeatureItem item) {
			this.item = item;
		}

		@Override
		public CompoundNBT write(CompoundNBT tag) {
			tag.put("boss_item", item.write());
			return super.write(tag);
		}

		@Override
		public void read(BlockState state, CompoundNBT tag) {
			item = BossFeatureItem.read(tag.getCompound("boss_item"));
			super.read(state, tag);
		}

	}

}
