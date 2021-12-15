package shadows.apotheosis.spawn.spawner;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;
import shadows.apotheosis.spawn.SpawnerModule;

public class ApothSpawnerTile extends SpawnerBlockEntity {

	public boolean ignoresPlayers = false;
	public boolean ignoresConditions = false;
	public boolean ignoresCap = false;
	public boolean redstoneEnabled = false;

	public ApothSpawnerTile() {
		this.spawner = new SpawnerLogicExt();
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag.putBoolean("ignore_players", this.ignoresPlayers);
		tag.putBoolean("ignore_conditions", this.ignoresConditions);
		tag.putBoolean("ignore_cap", this.ignoresCap);
		tag.putBoolean("redstone_control", this.redstoneEnabled);
		return super.save(tag);
	}

	@Override
	public void load(BlockState state, CompoundTag tag) {
		this.ignoresPlayers = tag.getBoolean("ignore_players");
		this.ignoresConditions = tag.getBoolean("ignore_conditions");
		this.ignoresCap = tag.getBoolean("ignore_cap");
		this.redstoneEnabled = tag.getBoolean("redstone_control");
		super.load(state, tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		this.load(Blocks.SPAWNER.defaultBlockState(), pkt.getTag());
	}

	public class SpawnerLogicExt extends BaseSpawner {

		@Override
		public void broadcastEvent(int id) {
			ApothSpawnerTile.this.level.blockEvent(ApothSpawnerTile.this.worldPosition, Blocks.SPAWNER, id, 0);
		}

		@Override
		public Level getLevel() {
			return ApothSpawnerTile.this.level;
		}

		@Override
		public BlockPos getPos() {
			return ApothSpawnerTile.this.worldPosition;
		}

		@Nullable
		@Override //Fix MC-189565 https://bugs.mojang.com/browse/MC-189565
		public Entity getOrCreateDisplayEntity() {
			if (this.displayEntity == null) {
				CompoundTag tag = this.nextSpawnData.getTag();
				EntityType.by(tag).ifPresent(e -> {
					this.displayEntity = e.create(this.getLevel());
					try {
						this.displayEntity.load(tag);
					} catch (Exception ex) {
						SpawnerModule.LOG.error("Exception occurred reading entity nbt for client cache - likely MC-189565");
					}
				});
			}
			return this.displayEntity;
		}

		@Override
		public void setNextSpawnData(SpawnData nextSpawnData) {
			super.setNextSpawnData(nextSpawnData);

			if (this.getLevel() != null) {
				BlockState iblockstate = this.getLevel().getBlockState(this.getPos());
				this.getLevel().sendBlockUpdated(ApothSpawnerTile.this.worldPosition, iblockstate, iblockstate, 4);
			}
		}

		private boolean isActivated() {
			BlockPos blockpos = this.getPos();
			boolean flag = ApothSpawnerTile.this.ignoresPlayers || this.getLevel().hasNearbyAlivePlayer(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, this.requiredPlayerRange);
			return flag && (!ApothSpawnerTile.this.redstoneEnabled || ApothSpawnerTile.this.level.hasNeighborSignal(blockpos));
		}

		private void resetTimer() {
			if (this.maxSpawnDelay <= this.minSpawnDelay) {
				this.spawnDelay = this.minSpawnDelay;
			} else {
				int i = this.maxSpawnDelay - this.minSpawnDelay;
				this.spawnDelay = this.minSpawnDelay + this.getLevel().random.nextInt(i);
			}

			if (!this.spawnPotentials.isEmpty()) {
				this.setNextSpawnData(WeighedRandom.getRandomItem(this.getLevel().random, this.spawnPotentials));
			}

			this.broadcastEvent(1);
		}

		@Override
		public void tick() {
			if (!this.isActivated()) {
				this.oSpin = this.spin;
			} else {
				Level world = this.getLevel();
				BlockPos blockpos = this.getPos();
				if (world.isClientSide) {
					double d3 = blockpos.getX() + world.random.nextFloat();
					double d4 = blockpos.getY() + world.random.nextFloat();
					double d5 = blockpos.getZ() + world.random.nextFloat();
					world.addParticle(ParticleTypes.SMOKE, d3, d4, d5, 0.0D, 0.0D, 0.0D);
					world.addParticle(ParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D);
					if (this.spawnDelay > 0) {
						--this.spawnDelay;
					}

					this.oSpin = this.spin;
					this.spin = (this.spin + 1000.0F / (this.spawnDelay + 200.0F)) % 360.0D;
				} else {
					if (this.spawnDelay == -1) {
						this.resetTimer();
					}

					if (this.spawnDelay > 0) {
						--this.spawnDelay;
						return;
					}

					boolean flag = false;

					for (int i = 0; i < this.spawnCount; ++i) {
						CompoundTag compoundnbt = this.nextSpawnData.getTag();
						Optional<EntityType<?>> optional = EntityType.by(compoundnbt);
						if (!optional.isPresent()) {
							this.resetTimer();
							return;
						}

						ListTag listnbt = compoundnbt.getList("Pos", 6);
						int j = listnbt.size();
						double x = j >= 1 ? listnbt.getDouble(0) : blockpos.getX() + (world.random.nextDouble() - world.random.nextDouble()) * this.spawnRange + 0.5D;
						double y = j >= 2 ? listnbt.getDouble(1) : (double) (blockpos.getY() + world.random.nextInt(3) - 1);
						double z = j >= 3 ? listnbt.getDouble(2) : blockpos.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * this.spawnRange + 0.5D;
						if (ApothSpawnerTile.this.ignoresConditions || world.noCollision(optional.get().getAABB(x, y, z)) && SpawnPlacements.checkSpawnRules(optional.get(), (ServerLevelAccessor) world, MobSpawnType.SPAWNER, new BlockPos(x, y, z), world.getRandom())) {
							Entity entity = EntityType.loadEntityRecursive(compoundnbt, world, newEntity -> {
								newEntity.moveTo(x, y, z, newEntity.yRot, newEntity.xRot);
								return newEntity;
							});
							if (entity == null) {
								this.resetTimer();
								return;
							}

							if (!ApothSpawnerTile.this.ignoresCap) {
								int nearby = world.getEntitiesOfClass(entity.getClass(), new AABB(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1).inflate(this.spawnRange)).size();
								if (nearby >= this.maxNearbyEntities) {
									this.resetTimer();
									return;
								}
							}

							entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
							if (entity instanceof Mob) {
								Mob mobentity = (Mob) entity;
								if (!ApothSpawnerTile.this.ignoresConditions && !ForgeEventFactory.canEntitySpawnSpawner(mobentity, world, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this)) {
									continue;
								}

								if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8) && !ForgeEventFactory.doSpecialSpawn((Mob) entity, this.getLevel(), (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, MobSpawnType.SPAWNER)) {
									((Mob) entity).finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(new BlockPos(entity.position())), MobSpawnType.SPAWNER, null, null);
								}
							}

							if (!((ServerLevel) world).tryAddFreshEntityWithPassengers(entity)) {
								this.resetTimer();
								return;
							}

							world.levelEvent(2004, blockpos, 0);
							if (entity instanceof Mob) {
								((Mob) entity).spawnAnim();
							}

							flag = true;
						}
					}

					if (flag) {
						this.resetTimer();
					}
				}

			}
		}
	}

}