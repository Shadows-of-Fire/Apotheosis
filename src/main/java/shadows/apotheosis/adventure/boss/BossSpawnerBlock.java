package shadows.apotheosis.adventure.boss;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.placebo.block_entity.TickingBlockEntity;
import shadows.placebo.block_entity.TickingEntityBlock;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;

public class BossSpawnerBlock extends Block implements TickingEntityBlock {

	public BossSpawnerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new BossSpawnerTile(pPos, pState);
	}

	public static class BossSpawnerTile extends BlockEntity implements TickingBlockEntity {

		protected BossItem item;
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
					BossItem bossItem = this.item == null ? BossItemManager.INSTANCE.getRandomItem(this.level.getRandom(), player.getLuck(), IDimensional.matches(this.level), IStaged.matches(player)) : this.item;
					Mob entity = bossItem.createBoss((ServerLevel) this.level, pos, this.level.getRandom(), player.getLuck());
					entity.setTarget(player);
					entity.setPersistenceRequired();
					((ServerLevel) this.level).addFreshEntityWithPassengers(entity);
				});
			}
		}

		public void setBossItem(BossItem item) {
			this.item = item;
		}

		@Override
		public void saveAdditional(CompoundTag tag) {
			if (this.item != null) tag.putString("boss_item", this.item.getId().toString());
			super.saveAdditional(tag);
		}

		@Override
		public void load(CompoundTag tag) {
			this.item = BossItemManager.INSTANCE.getValue(new ResourceLocation(tag.getString("boss_item")));
			super.load(tag);
		}

	}

}