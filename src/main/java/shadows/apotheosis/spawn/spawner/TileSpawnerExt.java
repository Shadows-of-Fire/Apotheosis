package shadows.apotheosis.spawn.spawner;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
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
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.event.ForgeEventFactory;

public class TileSpawnerExt extends MobSpawnerTileEntity {

	public boolean ignoresPlayers = false;
	public boolean ignoresConditions = false;
	public boolean ignoresCap = false;
	public boolean redstoneEnabled = false;

	public TileSpawnerExt() {
		spawnerLogic = new SpawnerLogicExt();
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("ignore_players", ignoresPlayers);
		tag.putBoolean("ignore_conditions", ignoresConditions);
		tag.putBoolean("ignore_cap", ignoresCap);
		tag.putBoolean("redstone_control", redstoneEnabled);
		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		ignoresPlayers = tag.getBoolean("ignore_players");
		ignoresConditions = tag.getBoolean("ignore_conditions");
		ignoresCap = tag.getBoolean("ignore_cap");
		redstoneEnabled = tag.getBoolean("redstone_control");
		super.read(tag);
	}

	public void setIgnoresPlayers(boolean val) {
		ignoresPlayers = val;
	}

	public void setIgnoresConditions(boolean val) {
		ignoresConditions = val;
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		read(pkt.getNbtCompound());
	}

	public class SpawnerLogicExt extends AbstractSpawner {

		@Override
		public void broadcastEvent(int id) {
			TileSpawnerExt.this.world.addBlockEvent(TileSpawnerExt.this.pos, Blocks.SPAWNER, id, 0);
		}

		@Override
		public World getWorld() {
			return TileSpawnerExt.this.world;
		}

		@Override
		public BlockPos getSpawnerPosition() {
			return TileSpawnerExt.this.pos;
		}

		@Override
		public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_) {
			super.setNextSpawnData(p_184993_1_);

			if (getWorld() != null) {
				BlockState iblockstate = getWorld().getBlockState(getSpawnerPosition());
				getWorld().notifyBlockUpdate(TileSpawnerExt.this.pos, iblockstate, iblockstate, 4);
			}
		}

		private boolean isActivated() {
			BlockPos blockpos = this.getSpawnerPosition();
			boolean flag = ignoresPlayers || this.getWorld().isPlayerWithin(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, this.activatingRangeFromPlayer);
			return flag && (!redstoneEnabled || world.isBlockPowered(blockpos));
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
						if (ignoresConditions || world.doesNotCollide(optional.get().func_220328_a(x, y, z)) && EntitySpawnPlacementRegistry.func_223515_a(optional.get(), world.getWorld(), SpawnReason.SPAWNER, new BlockPos(x, y, z), world.getRandom())) {
							Entity entity = EntityType.func_220335_a(compoundnbt, world, (p_221408_6_) -> {
								p_221408_6_.setLocationAndAngles(x, y, z, p_221408_6_.rotationYaw, p_221408_6_.rotationPitch);
								return p_221408_6_;
							});
							if (entity == null) {
								this.resetTimer();
								return;
							}

							if (!ignoresCap) {
								int k = world.getEntitiesWithinAABB(entity.getClass(), new AxisAlignedBB(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1).grow(this.spawnRange)).size();
								if (k >= this.maxNearbyEntities) {
									this.resetTimer();
									return;
								}
							}

							entity.setLocationAndAngles(entity.getX(), entity.getY(), entity.getZ(), world.rand.nextFloat() * 360.0F, 0.0F);
							if (entity instanceof MobEntity) {
								MobEntity mobentity = (MobEntity) entity;
								if (!ignoresConditions && !ForgeEventFactory.canEntitySpawnSpawner(mobentity, world, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this)) {
									continue;
								}

								if (this.spawnData.getNbt().size() == 1 && this.spawnData.getNbt().contains("id", 8) && !ForgeEventFactory.doSpecialSpawn((MobEntity) entity, getWorld(), (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), this, SpawnReason.SPAWNER)) {
									((MobEntity) entity).onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(entity)), SpawnReason.SPAWNER, (ILivingEntityData) null, (CompoundNBT) null);
								}
							}

							this.spawnEntity(entity);
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

		protected void spawnEntity(Entity entity) {
			if (this.getWorld().addEntity(entity)) {
				for (Entity e : entity.getPassengers()) {
					this.spawnEntity(e);
				}

			}
		}
	}

}
