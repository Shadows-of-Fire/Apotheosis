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
		this.spawnerLogic = new SpawnerLogicExt();
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("ignore_players", this.ignoresPlayers);
		tag.putBoolean("ignore_conditions", this.ignoresConditions);
		tag.putBoolean("ignore_cap", this.ignoresCap);
		tag.putBoolean("redstone_control", this.redstoneEnabled);
		return super.write(tag);
	}

	@Override
	public void read(BlockState state, CompoundNBT tag) {
		this.ignoresPlayers = tag.getBoolean("ignore_players");
		this.ignoresConditions = tag.getBoolean("ignore_conditions");
		this.ignoresCap = tag.getBoolean("ignore_cap");
		this.redstoneEnabled = tag.getBoolean("redstone_control");
		super.read(state, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.read(Blocks.SPAWNER.getDefaultState(), pkt.getNbtCompound());
	}

	public class SpawnerLogicExt extends AbstractSpawner {

		@Override
		public void broadcastEvent(int id) {
			ApothSpawnerTile.this.world.addBlockEvent(ApothSpawnerTile.this.pos, Blocks.SPAWNER, id, 0);
		}

		@Override
		public World getWorld() {
			return ApothSpawnerTile.this.world;
		}

		@Override
		public BlockPos getSpawnerPosition() {
			return ApothSpawnerTile.this.pos;
		}

		@Nullable
		@Override //Fix MC-189565 https://bugs.mojang.com/browse/MC-189565
		public Entity getCachedEntity() {
			if (this.cachedEntity == null) {
				CompoundNBT tag = this.spawnData.getNbt();
				EntityType.readEntityType(tag).ifPresent(e -> {
					this.cachedEntity = e.create(this.getWorld());
					try {
						this.cachedEntity.read(tag);
					} catch (Exception ex) {
						SpawnerModule.LOG.error("Exception occurred reading entity nbt for client cache - likely MC-189565");
					}
				});
			}
			return this.cachedEntity;
		}

		@Override
		public void setNextSpawnData(WeightedSpawnerEntity nextSpawnData) {
			super.setNextSpawnData(nextSpawnData);

			if (this.getWorld() != null) {
				BlockState iblockstate = this.getWorld().getBlockState(this.getSpawnerPosition());
				this.getWorld().notifyBlockUpdate(ApothSpawnerTile.this.pos, iblockstate, iblockstate, 4);
			}
		}

		private boolean isActivated() {
			BlockPos blockpos = this.getSpawnerPosition();
			boolean flag = ApothSpawnerTile.this.ignoresPlayers || this.getWorld().isPlayerWithin(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, this.activatingRangeFromPlayer);
			return flag && (!ApothSpawnerTile.this.redstoneEnabled || ApothSpawnerTile.this.world.isBlockPowered(blockpos));
		}

		private void resetTimer() {
			if (this.maxSpawnDelay <= this.minSpawnDelay) {
				this.spawnDelay = this.minSpawnDelay;
			} else {
				int i = this.maxSpawnDelay - this.minSpawnDelay;
				this.spawnDelay = this.minSpawnDelay + this.getWorld().rand.nextInt(i);
			}

			if (!this.potentialSpawns.isEmpty()) {
				this.setNextSpawnData(WeightedRandom.getRandomItem(this.getWorld().rand, this.potentialSpawns));
			}

			this.broadcastEvent(1);
		}

		@Override
		public void tick() {
			if (!this.isActivated()) {
				this.prevMobRotation = this.mobRotation;
			} else {
				World world = this.getWorld();
				BlockPos blockpos = this.getSpawnerPosition();
				if (world.isRemote) {
					double d3 = blockpos.getX() + world.rand.nextFloat();
					double d4 = blockpos.getY() + world.rand.nextFloat();
					double d5 = blockpos.getZ() + world.rand.nextFloat();
					world.addParticle(ParticleTypes.SMOKE, d3, d4, d5, 0.0D, 0.0D, 0.0D);
					world.addParticle(ParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D);
					if (this.spawnDelay > 0) {
						--this.spawnDelay;
					}

					this.prevMobRotation = this.mobRotation;
					this.mobRotation = (this.mobRotation + 1000.0F / (this.spawnDelay + 200.0F)) % 360.0D;
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
						CompoundNBT compoundnbt = this.spawnData.getNbt();
						Optional<EntityType<?>> optional = EntityType.readEntityType(compoundnbt);
						if (!optional.isPresent()) {
							this.resetTimer();
							return;
						}

						ListNBT listnbt = compoundnbt.getList("Pos", 6);
						int j = listnbt.size();
						double x = j >= 1 ? listnbt.getDouble(0) : blockpos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * this.spawnRange + 0.5D;
						double y = j >= 2 ? listnbt.getDouble(1) : (double) (blockpos.getY() + world.rand.nextInt(3) - 1);
						double z = j >= 3 ? listnbt.getDouble(2) : blockpos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * this.spawnRange + 0.5D;
						if (ApothSpawnerTile.this.ignoresConditions || world.hasNoCollisions(optional.get().getBoundingBoxWithSizeApplied(x, y, z)) && EntitySpawnPlacementRegistry.canSpawnEntity(optional.get(), (IServerWorld) world, SpawnReason.SPAWNER, new BlockPos(x, y, z), world.getRandom())) {
							Entity entity = EntityType.loadEntityAndExecute(compoundnbt, world, newEntity -> {
								newEntity.setLocationAndAngles(x, y, z, newEntity.rotationYaw, newEntity.rotationPitch);
								return newEntity;
							});
							if (entity == null) {
								this.resetTimer();
								return;
							}

							if (!ApothSpawnerTile.this.ignoresCap) {
								int nearby = world.getEntitiesWithinAABB(entity.getClass(), new AxisAlignedBB(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1).grow(this.spawnRange)).size();
								if (nearby >= this.maxNearbyEntities) {
									this.resetTimer();
									return;
								}
							}

							entity.setLocationAndAngles(entity.getPosX(), entity.getPosY(), entity.getPosZ(), world.rand.nextFloat() * 360.0F, 0.0F);
							if (entity instanceof MobEntity) {
								MobEntity mobentity = (MobEntity) entity;
								if (!ApothSpawnerTile.this.ignoresConditions && !ForgeEventFactory.canEntitySpawnSpawner(mobentity, world, (float) entity.getPosX(), (float) entity.getPosY(), (float) entity.getPosZ(), this)) {
									continue;
								}

								if (this.spawnData.getNbt().size() == 1 && this.spawnData.getNbt().contains("id", 8) && !ForgeEventFactory.doSpecialSpawn((MobEntity) entity, this.getWorld(), (float) entity.getPosX(), (float) entity.getPosY(), (float) entity.getPosZ(), this, SpawnReason.SPAWNER)) {
									((MobEntity) entity).onInitialSpawn((IServerWorld) world, world.getDifficultyForLocation(new BlockPos(entity.getPositionVec())), SpawnReason.SPAWNER, null, null);
								}
							}

							if (!((ServerWorld) world).func_242106_g(entity)) {
								this.resetTimer();
								return;
							}

							world.playEvent(2004, blockpos, 0);
							if (entity instanceof MobEntity) {
								((MobEntity) entity).spawnExplosionParticle();
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