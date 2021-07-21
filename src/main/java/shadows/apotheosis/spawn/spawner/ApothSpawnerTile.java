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
import net.minecraftforge.event.ForgeEventFactory;
import shadows.apotheosis.spawn.SpawnerModule;

public class ApothSpawnerTile extends MobSpawnerTileEntity {

	public boolean ignoresPlayers = false;
	public boolean ignoresConditions = false;
	public boolean ignoresCap = false;
	public boolean redstoneEnabled = false;

	public ApothSpawnerTile() {
		this.spawner = new SpawnerLogicExt();
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		tag.putBoolean("ignore_players", this.ignoresPlayers);
		tag.putBoolean("ignore_conditions", this.ignoresConditions);
		tag.putBoolean("ignore_cap", this.ignoresCap);
		tag.putBoolean("redstone_control", this.redstoneEnabled);
		return super.save(tag);
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		this.ignoresPlayers = tag.getBoolean("ignore_players");
		this.ignoresConditions = tag.getBoolean("ignore_conditions");
		this.ignoresCap = tag.getBoolean("ignore_cap");
		this.redstoneEnabled = tag.getBoolean("redstone_control");
		super.load(state, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.load(Blocks.SPAWNER.defaultBlockState(), pkt.getTag());
	}

	public class SpawnerLogicExt extends AbstractSpawner {

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
						CompoundNBT compoundnbt = this.nextSpawnData.getTag();
						Optional<EntityType<?>> optional = EntityType.by(compoundnbt);
						if (!optional.isPresent()) {
							this.resetTimer();
							return;
						}

						ListNBT listnbt = compoundnbt.getList("Pos", 6);
						int j = listnbt.size();
						double x = j >= 1 ? listnbt.getDouble(0) : blockpos.getX() + (world.random.nextDouble() - world.random.nextDouble()) * this.spawnRange + 0.5D;
						double y = j >= 2 ? listnbt.getDouble(1) : (double) (blockpos.getY() + world.random.nextInt(3) - 1);
						double z = j >= 3 ? listnbt.getDouble(2) : blockpos.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * this.spawnRange + 0.5D;
						if (ApothSpawnerTile.this.ignoresConditions || world.noCollision(optional.get().getAABB(x, y, z)) && EntitySpawnPlacementRegistry.checkSpawnRules(optional.get(), (IServerWorld) world, SpawnReason.SPAWNER, new BlockPos(x, y, z), world.getRandom())) {
							Entity entity = EntityType.loadEntityRecursive(compoundnbt, world, newEntity -> {
								newEntity.moveTo(x, y, z, newEntity.yRot, newEntity.xRot);
								return newEntity;
							});
							if (entity == null) {
								this.resetTimer();
								return;
							}

							if (!ApothSpawnerTile.this.ignoresCap) {
								int nearby = world.getEntitiesOfClass(entity.getClass(), new AxisAlignedBB(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1).inflate(this.spawnRange)).size();
								if (nearby >= this.maxNearbyEntities) {
									this.resetTimer();
									return;
								}
							}

							entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
							if (entity instanceof MobEntity) {
								MobEntity mobentity = (MobEntity) entity;
								if (!ApothSpawnerTile.this.ignoresConditions && !ForgeEventFactory.canEntitySpawnSpawner(mobentity, world, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this)) {
									continue;
								}

								if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8) && !ForgeEventFactory.doSpecialSpawn((MobEntity) entity, this.getLevel(), (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, SpawnReason.SPAWNER)) {
									((MobEntity) entity).finalizeSpawn((IServerWorld) world, world.getCurrentDifficultyAt(new BlockPos(entity.position())), SpawnReason.SPAWNER, null, null);
								}
							}

							if (!((ServerWorld) world).tryAddFreshEntityWithPassengers(entity)) {
								this.resetTimer();
								return;
							}

							world.levelEvent(2004, blockpos, 0);
							if (entity instanceof MobEntity) {
								((MobEntity) entity).spawnAnim();
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