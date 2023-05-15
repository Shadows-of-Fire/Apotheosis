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
import net.minecraft.util.random.SimpleWeightedRandomList;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ApothSpawnerTile extends SpawnerBlockEntity {

	public boolean ignoresPlayers = false;
	public boolean ignoresConditions = false;
	public boolean redstoneControl = false;
	public boolean ignoresLight = false;
	public boolean hasNoAI = false;
	public boolean silent = false;

	public ApothSpawnerTile(BlockPos pos, BlockState state) {
		super(pos, state);
		this.spawner = new SpawnerLogicExt();
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		tag.putBoolean("ignore_players", this.ignoresPlayers);
		tag.putBoolean("ignore_conditions", this.ignoresConditions);
		tag.putBoolean("redstone_control", this.redstoneControl);
		tag.putBoolean("ignore_light", this.ignoresLight);
		tag.putBoolean("no_ai", this.hasNoAI);
		tag.putBoolean("silent", this.silent);
		super.saveAdditional(tag);
	}

	@Override
	public void load(CompoundTag tag) {
		this.ignoresPlayers = tag.getBoolean("ignore_players");
		this.ignoresConditions = tag.getBoolean("ignore_conditions");
		this.redstoneControl = tag.getBoolean("redstone_control");
		this.ignoresLight = tag.getBoolean("ignore_light");
		this.hasNoAI = tag.getBoolean("no_ai");
		this.silent = tag.getBoolean("silent");
		super.load(tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		this.load(pkt.getTag());
	}

	public class SpawnerLogicExt extends BaseSpawner {

		@Override
		public void setEntityId(EntityType<?> pType) {
			this.nextSpawnData = new SpawnData();
			super.setEntityId(pType);
			this.spawnPotentials = SimpleWeightedRandomList.single(this.nextSpawnData);
			if (ApothSpawnerTile.this.level != null) this.delay(ApothSpawnerTile.this.level, ApothSpawnerTile.this.worldPosition);
		}

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
			return flag && (!ApothSpawnerTile.this.redstoneControl || ApothSpawnerTile.this.level.hasNeighborSignal(pos));
		}

		private void delay(Level pLevel, BlockPos pPos) {
			if (this.maxSpawnDelay <= this.minSpawnDelay) {
				this.spawnDelay = this.minSpawnDelay;
			} else {
				this.spawnDelay = this.minSpawnDelay + pLevel.random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
			}

			this.spawnPotentials.getRandom(pLevel.random).ifPresent(potential -> {
				this.setNextSpawnData(pLevel, pPos, potential.getData());
			});
			this.broadcastEvent(pLevel, pPos, 1);
		}

		@Override
		public void clientTick(Level pLevel, BlockPos pPos) {
			if (!this.isActivated(pLevel, pPos)) {
				this.oSpin = this.spin;
			} else {
				double d0 = pPos.getX() + pLevel.random.nextDouble();
				double d1 = pPos.getY() + pLevel.random.nextDouble();
				double d2 = pPos.getZ() + pLevel.random.nextDouble();
				pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
				pLevel.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
				if (this.spawnDelay > 0) {
					--this.spawnDelay;
				}

				this.oSpin = this.spin;
				this.spin = (this.spin + 1000.0F / (this.spawnDelay + 200.0F)) % 360.0D;
			}

		}

		@Override
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
						CompoundTag tag = this.nextSpawnData.getEntityToSpawn();
						Optional<EntityType<?>> optional = EntityType.by(tag);
						if (optional.isEmpty()) {
							this.delay(pServerLevel, pPos);
							return;
						}

						ListTag listtag = tag.getList("Pos", 6);
						int j = listtag.size();
						double d0 = j >= 1 ? listtag.getDouble(0) : pPos.getX() + (pServerLevel.random.nextDouble() - pServerLevel.random.nextDouble()) * this.spawnRange + 0.5D;
						double d1 = j >= 2 ? listtag.getDouble(1) : (double) (pPos.getY() + pServerLevel.random.nextInt(3) - 1);
						double d2 = j >= 3 ? listtag.getDouble(2) : pPos.getZ() + (pServerLevel.random.nextDouble() - pServerLevel.random.nextDouble()) * this.spawnRange + 0.5D;
						if (pServerLevel.noCollision(optional.get().getAABB(d0, d1, d2))) {
							BlockPos blockpos = new BlockPos(d0, d1, d2);

							//LOGIC CHANGE : Ability to ignore conditions set in the spawner and by the entity.
							LyingLevel liar = new LyingLevel(pServerLevel);
							boolean useLiar = false;
							if (!ApothSpawnerTile.this.ignoresConditions) {
								if (ApothSpawnerTile.this.ignoresLight) {
									boolean pass = false;
									for (int light = 0; light < 16; light++) {
										liar.setFakeLightLevel(light);
										if (this.checkSpawnRules(optional, liar, blockpos)) {
											pass = true;
											break;
										}
									}
									if (!pass) continue;
									else useLiar = true;
								} else if (!this.checkSpawnRules(optional, pServerLevel, blockpos)) continue;
							}

							Entity entity = EntityType.loadEntityRecursive(tag, pServerLevel, p_151310_ -> {
								p_151310_.moveTo(d0, d1, d2, p_151310_.getYRot(), p_151310_.getXRot());
								return p_151310_;
							});
							if (entity == null) {
								this.delay(pServerLevel, pPos);
								return;
							}

							if (ApothSpawnerTile.this.hasNoAI && entity instanceof Mob mob) {
								mob.setNoAi(true);
								entity.getPersistentData().putBoolean("apotheosis:movable", true);
							}
							if (ApothSpawnerTile.this.silent) entity.setSilent(true);

							int k = pServerLevel.getEntitiesOfClass(entity.getClass(), new AABB(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX() + 1, pPos.getY() + 1, pPos.getZ() + 1).inflate(this.spawnRange)).size();
							if (k >= this.maxNearbyEntities) {
								this.delay(pServerLevel, pPos);
								return;
							}

							entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), pServerLevel.random.nextFloat() * 360.0F, 0.0F);
							if (entity instanceof Mob) {
								Mob mob = (Mob) entity;
								net.minecraftforge.eventbus.api.Event.Result res = net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(mob, useLiar ? liar : pServerLevel, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, MobSpawnType.SPAWNER);
								if (res == net.minecraftforge.eventbus.api.Event.Result.DENY) continue;
								if (res == net.minecraftforge.eventbus.api.Event.Result.DEFAULT) {
									if (!ApothSpawnerTile.this.ignoresConditions && (this.nextSpawnData.getCustomSpawnRules().isEmpty() && !mob.checkSpawnRules(useLiar ? liar : pServerLevel, MobSpawnType.SPAWNER) || !mob.checkSpawnObstruction(useLiar ? liar : pServerLevel))) {
										continue;
									}
								}

								if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
									if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(mob, useLiar ? liar : pServerLevel, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, MobSpawnType.SPAWNER)) ((Mob) entity).finalizeSpawn(pServerLevel, pServerLevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, (SpawnGroupData) null, (CompoundTag) null);
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

		/**
		 * Checks if the requested entity passes spawn rule checks or not.
		 */
		private boolean checkSpawnRules(Optional<EntityType<?>> optional, ServerLevelAccessor pServerLevel, BlockPos blockpos) {
			if (this.nextSpawnData.getCustomSpawnRules().isPresent()) {
				if (!optional.get().getCategory().isFriendly() && pServerLevel.getDifficulty() == Difficulty.PEACEFUL) {
					return false;
				}

				SpawnData.CustomSpawnRules spawndata$customspawnrules = this.nextSpawnData.getCustomSpawnRules().get();
				if (ApothSpawnerTile.this.ignoresLight) return true; // All custom spawn rules are light-based, so if we ignore light, we can short-circuit here.
				if (!spawndata$customspawnrules.blockLightLimit().isValueInRange(pServerLevel.getBrightness(LightLayer.BLOCK, blockpos)) || !spawndata$customspawnrules.skyLightLimit().isValueInRange(pServerLevel.getBrightness(LightLayer.SKY, blockpos))) {
					return false;
				}
			} else if (!SpawnPlacements.checkSpawnRules(optional.get(), pServerLevel, MobSpawnType.SPAWNER, blockpos, pServerLevel.getRandom())) {
				return false;
			}
			return true;
		}

	}

}