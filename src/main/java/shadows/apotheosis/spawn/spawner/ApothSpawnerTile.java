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
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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
	public void saveAdditional(CompoundTag tag) {
		tag.putBoolean("ignore_players", this.ignoresPlayers);
		tag.putBoolean("ignore_conditions", this.ignoresConditions);
		tag.putBoolean("ignore_cap", this.ignoresCap);
		tag.putBoolean("redstone_control", this.redstoneEnabled);
		super.saveAdditional(tag);
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

		@Override
		public void setNextSpawnData(Level level, BlockPos pos, SpawnData nextSpawnData) {
			super.setNextSpawnData(level, pos, nextSpawnData);

			if (level != null) {
				BlockState state = level.getBlockState(pos);
				level.sendBlockUpdated(pos, state, state, 4);
			}
		}

		@Nullable
		@Override
		public BlockEntity getSpawnerBlockEntity() {
			return ApothSpawnerTile.this;
		}

		private boolean isActivated(Level level, BlockPos pos) {
			boolean flag = ApothSpawnerTile.this.ignoresPlayers || level.hasNearbyAlivePlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this.requiredPlayerRange);
			return flag && (!ApothSpawnerTile.this.redstoneEnabled || ApothSpawnerTile.this.level.hasNeighborSignal(pos));
		}

		private void delay(Level pLevel, BlockPos pPos) {
			if (this.maxSpawnDelay <= this.minSpawnDelay) {
				this.spawnDelay = this.minSpawnDelay;
			} else {
				this.spawnDelay = this.minSpawnDelay + pLevel.random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
			}

			this.spawnPotentials.getRandom(pLevel.random).ifPresent((potential) -> {
				this.setNextSpawnData(pLevel, pPos, potential.getData());
			});
			this.broadcastEvent(pLevel, pPos, 1);
		}

		@Override
		public void clientTick(Level pLevel, BlockPos pPos) {
			if (!this.isActivated(pLevel, pPos)) {
				this.oSpin = this.spin;
			} else {
				double d0 = (double) pPos.getX() + pLevel.random.nextDouble();
				double d1 = (double) pPos.getY() + pLevel.random.nextDouble();
				double d2 = (double) pPos.getZ() + pLevel.random.nextDouble();
				pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
				pLevel.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
				if (this.spawnDelay > 0) {
					--this.spawnDelay;
				}

				this.oSpin = this.spin;
				this.spin = (this.spin + (double) (1000.0F / ((float) this.spawnDelay + 200.0F))) % 360.0D;
			}

		}

		public void serverTick(ServerLevel pServerLevel, BlockPos pPos) {
			if (this.isActivated(pServerLevel, pPos)) {
				if (this.spawnDelay == -1) {
					this.delay(pServerLevel, pPos);
				}

				if (this.spawnDelay > 0) {
					--this.spawnDelay;
				} else {
					boolean flag = false;

					for (int i = 0; i < this.spawnCount; ++i) {
						CompoundTag compoundtag = this.nextSpawnData.getEntityToSpawn();
						Optional<EntityType<?>> optional = EntityType.by(compoundtag);
						if (optional.isEmpty()) {
							this.delay(pServerLevel, pPos);
							return;
						}

						ListTag listtag = compoundtag.getList("Pos", 6);
						int j = listtag.size();
						double d0 = j >= 1 ? listtag.getDouble(0) : (double) pPos.getX() + (pServerLevel.random.nextDouble() - pServerLevel.random.nextDouble()) * (double) this.spawnRange + 0.5D;
						double d1 = j >= 2 ? listtag.getDouble(1) : (double) (pPos.getY() + pServerLevel.random.nextInt(3) - 1);
						double d2 = j >= 3 ? listtag.getDouble(2) : (double) pPos.getZ() + (pServerLevel.random.nextDouble() - pServerLevel.random.nextDouble()) * (double) this.spawnRange + 0.5D;
						if (pServerLevel.noCollision(optional.get().getAABB(d0, d1, d2))) {
							BlockPos blockpos = new BlockPos(d0, d1, d2);

							//LOGIC CHANGE : Ability to ignore conditions set in the spawner and by the entity.
							if (!ApothSpawnerTile.this.ignoresConditions) {
								if (this.nextSpawnData.getCustomSpawnRules().isPresent()) {
									if (!optional.get().getCategory().isFriendly() && pServerLevel.getDifficulty() == Difficulty.PEACEFUL) {
										continue;
									}

									SpawnData.CustomSpawnRules spawndata$customspawnrules = this.nextSpawnData.getCustomSpawnRules().get();
									if (!spawndata$customspawnrules.blockLightLimit().isValueInRange(pServerLevel.getBrightness(LightLayer.BLOCK, blockpos)) || !spawndata$customspawnrules.skyLightLimit().isValueInRange(pServerLevel.getBrightness(LightLayer.SKY, blockpos))) {
										continue;
									}
								} else if (!SpawnPlacements.checkSpawnRules(optional.get(), pServerLevel, MobSpawnType.SPAWNER, blockpos, pServerLevel.getRandom())) {
									continue;
								}
							}

							Entity entity = EntityType.loadEntityRecursive(compoundtag, pServerLevel, (p_151310_) -> {
								p_151310_.moveTo(d0, d1, d2, p_151310_.getYRot(), p_151310_.getXRot());
								return p_151310_;
							});
							if (entity == null) {
								this.delay(pServerLevel, pPos);
								return;
							}

							//LOGIC CHANGE : Ability to ignore the spawned entity cap - infinite spawning potential!
							if (!ApothSpawnerTile.this.ignoresCap) {
								int k = pServerLevel.getEntitiesOfClass(entity.getClass(), (new AABB((double) pPos.getX(), (double) pPos.getY(), (double) pPos.getZ(), (double) (pPos.getX() + 1), (double) (pPos.getY() + 1), (double) (pPos.getZ() + 1))).inflate((double) this.spawnRange)).size();
								if (k >= this.maxNearbyEntities) {
									this.delay(pServerLevel, pPos);
									return;
								}
							}

							entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), pServerLevel.random.nextFloat() * 360.0F, 0.0F);
							if (entity instanceof Mob) {
								Mob mob = (Mob) entity;
								if (!net.minecraftforge.event.ForgeEventFactory.canEntitySpawnSpawner(mob, pServerLevel, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this)) {
									continue;
								}

								if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
									if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(mob, pServerLevel, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, MobSpawnType.SPAWNER)) ((Mob) entity).finalizeSpawn(pServerLevel, pServerLevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, (SpawnGroupData) null, (CompoundTag) null);
								}
							}

							if (!pServerLevel.tryAddFreshEntityWithPassengers(entity)) {
								this.delay(pServerLevel, pPos);
								return;
							}

							pServerLevel.levelEvent(2004, pPos, 0);
							if (entity instanceof Mob) {
								((Mob) entity).spawnAnim();
							}

							flag = true;
						}
					}

					if (flag) {
						this.delay(pServerLevel, pPos);
					}

				}
			}
		}
	}

}