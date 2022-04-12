package shadows.apotheosis.spawn.spawner;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import shadows.apotheosis.spawn.SpawnerModule;

public class ApothSpawnerTile extends MobSpawnerTileEntity {

	public boolean ignoresPlayers = false;
	public boolean ignoresConditions = false;
	public boolean redstoneControl = false;
	public boolean ignoresLight = false;
	public boolean hasNoAI = false;

	public ApothSpawnerTile() {
		this.spawner = new SpawnerLogicExt();
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		tag.putBoolean("ignore_players", this.ignoresPlayers);
		tag.putBoolean("ignore_conditions", this.ignoresConditions);
		tag.putBoolean("redstone_control", this.redstoneControl);
		tag.putBoolean("ignore_light", this.ignoresLight);
		tag.putBoolean("no_ai", this.hasNoAI);
		return super.save(tag);
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		this.ignoresPlayers = tag.getBoolean("ignore_players");
		this.ignoresConditions = tag.getBoolean("ignore_conditions");
		this.redstoneControl = tag.getBoolean("redstone_control");
		this.ignoresLight = tag.getBoolean("ignore_light");
		this.hasNoAI = tag.getBoolean("no_ai");
		super.load(state, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.load(Blocks.SPAWNER.defaultBlockState(), pkt.getTag());
	}

	public class SpawnerLogicExt extends AbstractSpawner {

		@Override
		public void setEntityId(EntityType<?> pType) {
			super.setEntityId(pType);
			this.spawnPotentials.clear();
			this.spawnPotentials.add(this.nextSpawnData);
			if (ApothSpawnerTile.this.level != null) this.reset();
		}

		@Override
		public void broadcastEvent(int id) {
			ApothSpawnerTile.this.level.blockEvent(ApothSpawnerTile.this.worldPosition, Blocks.SPAWNER, id, 0);
		}

		@Override
		public World getLevel() {
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
				CompoundNBT tag = this.nextSpawnData.getTag();
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
		public void setNextSpawnData(WeightedSpawnerEntity nextSpawnData) {
			super.setNextSpawnData(nextSpawnData);

			if (this.getLevel() != null) {
				BlockState state = this.getLevel().getBlockState(this.getPos());
				this.getLevel().sendBlockUpdated(ApothSpawnerTile.this.worldPosition, state, state, 4);
			}
		}

		private boolean isActivated() {
			BlockPos pos = this.getPos();
			World level = this.getLevel();
			boolean flag = ApothSpawnerTile.this.ignoresPlayers || level.hasNearbyAlivePlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this.requiredPlayerRange);
			return flag && (!ApothSpawnerTile.this.redstoneControl || ApothSpawnerTile.this.level.hasNeighborSignal(pos));
		}

		private void reset() {
			World pLevel = this.getLevel();
			if (this.maxSpawnDelay <= this.minSpawnDelay) {
				this.spawnDelay = this.minSpawnDelay;
			} else {
				this.spawnDelay = this.minSpawnDelay + pLevel.random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
			}

			if (!this.spawnPotentials.isEmpty()) {
				this.setNextSpawnData(WeightedRandom.getRandomItem(this.getLevel().random, this.spawnPotentials));
			}

			this.broadcastEvent(1);
		}

		@Override
		public void tick() {
			if (!this.isActivated()) {
				this.oSpin = this.spin;
			} else {
				World world = this.getLevel();
				BlockPos pPos = this.getPos();
				if (world.isClientSide) {
					double nX = pPos.getX() + world.random.nextFloat();
					double nY = pPos.getY() + world.random.nextFloat();
					double nZ = pPos.getZ() + world.random.nextFloat();
					world.addParticle(ParticleTypes.SMOKE, nX, nY, nZ, 0.0D, 0.0D, 0.0D);
					world.addParticle(ParticleTypes.FLAME, nX, nY, nZ, 0.0D, 0.0D, 0.0D);
					if (this.spawnDelay > 0) {
						--this.spawnDelay;
					}

					this.oSpin = this.spin;
					this.spin = (this.spin + 1000.0F / (this.spawnDelay + 200.0F)) % 360.0D;
				} else {
					if (this.spawnDelay == -1) {
						this.reset();
					}

					if (this.spawnDelay > 0) {
						--this.spawnDelay;
						return;
					}

					boolean flag = false;

					for (int i = 0; i < this.spawnCount; ++i) {
						CompoundNBT compoundnbt = this.nextSpawnData.getTag();
						Optional<EntityType<?>> optional = EntityType.by(compoundnbt);
						if (!optional.isPresent()) {
							this.reset();
							return;
						}

						ListNBT listnbt = compoundnbt.getList("Pos", 6);
						int j = listnbt.size();
						double x = j >= 1 ? listnbt.getDouble(0) : pPos.getX() + (world.random.nextDouble() - world.random.nextDouble()) * this.spawnRange + 0.5D;
						double y = j >= 2 ? listnbt.getDouble(1) : (double) (pPos.getY() + world.random.nextInt(3) - 1);
						double z = j >= 3 ? listnbt.getDouble(2) : pPos.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * this.spawnRange + 0.5D;
						if (world.noCollision(optional.get().getAABB(x, y, z))) {

							BlockPos blockpos = new BlockPos(x, y, z);
							LyingLevel liar = new LyingLevel(world);
							boolean useLiar = false;
							if (!ApothSpawnerTile.this.ignoresConditions) {
								if (ApothSpawnerTile.this.ignoresLight) {
									boolean pass = false;
									for (int light = 0; light < 16; light++) {
										liar.setFakeLightLevel(light);
										if (checkSpawnRules(optional, liar, blockpos)) {
											pass = true;
											break;
										}
									}
									if (!pass) continue;
									else useLiar = true;
								} else if (!checkSpawnRules(optional, (IServerWorld) world, blockpos)) continue;
							}

							compoundnbt.putBoolean("NoAI", ApothSpawnerTile.this.hasNoAI); // Technically, this breaks existing spawners that are NoAI... but I've never heard of one of those.

							Entity entity = EntityType.loadEntityRecursive(compoundnbt, world, newEntity -> {
								newEntity.moveTo(x, y, z, newEntity.yRot, newEntity.xRot);
								return newEntity;
							});
							if (entity == null) {
								this.reset();
								return;
							}

							if (ApothSpawnerTile.this.hasNoAI) entity.getPersistentData().putBoolean("apotheosis:movable", true);

							int nearby = world.getEntitiesOfClass(entity.getClass(), new AxisAlignedBB(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX() + 1, pPos.getY() + 1, pPos.getZ() + 1).inflate(this.spawnRange)).size();
							if (nearby >= this.maxNearbyEntities) {
								this.reset();
								return;
							}

							entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
							if (entity instanceof MobEntity) {
								MobEntity mob = (MobEntity) entity;
								net.minecraftforge.eventbus.api.Event.Result res = net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(mob, useLiar ? liar : world, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, SpawnReason.SPAWNER);
								if (res == net.minecraftforge.eventbus.api.Event.Result.DENY) continue;
								if (res == net.minecraftforge.eventbus.api.Event.Result.DEFAULT) {
									if (!ApothSpawnerTile.this.ignoresConditions && (!mob.checkSpawnRules(world, SpawnReason.SPAWNER) || !mob.checkSpawnObstruction(world))) {
										continue;
									}
								}

								if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8)) {
									if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(mob, world, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, SpawnReason.SPAWNER)) mob.finalizeSpawn((IServerWorld) world, world.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.SPAWNER, null, null);
								}
							}

							if (!((ServerWorld) world).tryAddFreshEntityWithPassengers(entity)) {
								this.reset();
								return;
							}

							world.levelEvent(2004, pPos, 0);
							if (entity instanceof MobEntity) {
								((MobEntity) entity).spawnAnim();
							}

							flag = true;
						}
					}

					if (flag) {
						this.reset();
					}
				}

			}
		}

		/**
		 * Checks if the requested entity passes spawn rule checks or not.
		 */
		private boolean checkSpawnRules(Optional<EntityType<?>> optional, IServerWorld pServerLevel, BlockPos blockpos) {
			if (!EntitySpawnPlacementRegistry.checkSpawnRules(optional.get(), pServerLevel, SpawnReason.SPAWNER, blockpos, pServerLevel.getRandom())) {
				return false;
			}
			return true;
		}

	}

}