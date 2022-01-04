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

	public ApothSpawnerTile(BlockPos pos, BlockState state) {
		super(pos, state);
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
	public void load(CompoundTag tag) {
		this.ignoresPlayers = tag.getBoolean("ignore_players");
		this.ignoresConditions = tag.getBoolean("ignore_conditions");
		this.ignoresCap = tag.getBoolean("ignore_cap");
		this.redstoneEnabled = tag.getBoolean("redstone_control");
		super.load(tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		this.load(pkt.getTag());
	}

	public class SpawnerLogicExt extends BaseSpawner {

		@Override
		public void broadcastEvent(Level level, BlockPos pos, int id) {
			level.blockEvent(pos, Blocks.SPAWNER, id, 0);
		}

		@Nullable
		@Override //Fix MC-189565 https://bugs.mojang.com/browse/MC-189565
		public Entity getOrCreateDisplayEntity(Level level) {
			if (this.displayEntity == null) {
				CompoundTag tag = this.nextSpawnData.entityToSpawn();
				EntityType.by(tag).ifPresent(e -> {
					this.displayEntity = e.create(level);
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
		public void setNextSpawnData(Level level, BlockPos pos, SpawnData nextSpawnData) {
			super.setNextSpawnData(level, pos, nextSpawnData);

			if (level != null) {
				BlockState iblockstate = level.getBlockState(pos);
				level.sendBlockUpdated(pos, iblockstate, iblockstate, 4);
			}
		}

		private boolean isActivated(Level level, BlockPos blockpos) {
			boolean flag = ApothSpawnerTile.this.ignoresPlayers || level.hasNearbyAlivePlayer(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, this.requiredPlayerRange);
			return flag && (!ApothSpawnerTile.this.redstoneEnabled || level.hasNeighborSignal(blockpos));
		}

		private void resetTimer(Level level, BlockPos pos) {
			if (this.maxSpawnDelay <= this.minSpawnDelay) {
				this.spawnDelay = this.minSpawnDelay;
			} else {
				int i = this.maxSpawnDelay - this.minSpawnDelay;
				this.spawnDelay = this.minSpawnDelay + level.random.nextInt(i);
			}

			if (!this.spawnPotentials.isEmpty()) {
				this.setNextSpawnData(level, pos, this.spawnPotentials.getRandomValue(level.random).get());
			}

			this.broadcastEvent(level, pos, 1);
		}

		@Override
		public void clientTick(Level world, BlockPos blockpos) {
			if (!this.isActivated(world, blockpos)) {
				this.oSpin = this.spin;
			} else {
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
			}
		}

		@Override
		public void serverTick(ServerLevel world, BlockPos blockpos) {
			if (!this.isActivated(world, blockpos)) {
				this.oSpin = this.spin;
			} else {
				if (this.spawnDelay == -1) {
					this.resetTimer(world, blockpos);
				}

				if (this.spawnDelay > 0) {
					--this.spawnDelay;
					return;
				}

				boolean flag = false;

				for (int i = 0; i < this.spawnCount; ++i) {
					CompoundTag compoundnbt = this.nextSpawnData.entityToSpawn();
					Optional<EntityType<?>> optional = EntityType.by(compoundnbt);
					if (!optional.isPresent()) {
						this.resetTimer(world, blockpos);
						return;
					}

					ListTag listnbt = compoundnbt.getList("Pos", 6);
					int j = listnbt.size();
					double x = j >= 1 ? listnbt.getDouble(0) : blockpos.getX() + (world.random.nextDouble() - world.random.nextDouble()) * this.spawnRange + 0.5D;
					double y = j >= 2 ? listnbt.getDouble(1) : (double) (blockpos.getY() + world.random.nextInt(3) - 1);
					double z = j >= 3 ? listnbt.getDouble(2) : blockpos.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * this.spawnRange + 0.5D;
					if (ApothSpawnerTile.this.ignoresConditions || world.noCollision(optional.get().getAABB(x, y, z)) && SpawnPlacements.checkSpawnRules(optional.get(), (ServerLevelAccessor) world, MobSpawnType.SPAWNER, new BlockPos(x, y, z), world.getRandom())) {
						Entity entity = EntityType.loadEntityRecursive(compoundnbt, world, newEntity -> {
							newEntity.moveTo(x, y, z);
							return newEntity;
						});
						if (entity == null) {
							this.resetTimer(world, blockpos);
							return;
						}

						if (!ApothSpawnerTile.this.ignoresCap) {
							int nearby = world.getEntitiesOfClass(entity.getClass(), new AABB(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1).inflate(this.spawnRange)).size();
							if (nearby >= this.maxNearbyEntities) {
								this.resetTimer(world, blockpos);
								return;
							}
						}

						entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
						if (entity instanceof Mob) {
							Mob mobentity = (Mob) entity;
							if (!ApothSpawnerTile.this.ignoresConditions && !ForgeEventFactory.canEntitySpawnSpawner(mobentity, world, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this)) {
								continue;
							}

							if (this.nextSpawnData.entityToSpawn().size() == 1 && this.nextSpawnData.entityToSpawn().contains("id", 8) && !ForgeEventFactory.doSpecialSpawn((Mob) entity, world, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, MobSpawnType.SPAWNER)) {
								((Mob) entity).finalizeSpawn(world, world.getCurrentDifficultyAt(new BlockPos(entity.position())), MobSpawnType.SPAWNER, null, null);
							}
						}

						if (!world.tryAddFreshEntityWithPassengers(entity)) {
							this.resetTimer(world, blockpos);
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
					this.resetTimer(world, blockpos);
				}
			}
		}
	}

}