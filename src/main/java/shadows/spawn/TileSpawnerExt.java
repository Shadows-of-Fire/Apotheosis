package shadows.spawn;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

public class TileSpawnerExt extends TileEntityMobSpawner {

	public boolean ignoresPlayers = false;
	public boolean ignoresConditions = false;
	public boolean ignoresCap = false;
	public boolean redstoneEnabled = false;

	public TileSpawnerExt() {
		this.spawnerLogic = new SpawnerLogicExt();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setBoolean("ignore_players", ignoresPlayers);
		tag.setBoolean("ignore_conditions", ignoresConditions);
		tag.setBoolean("ignore_cap", ignoresPlayers);
		tag.setBoolean("redstone_control", redstoneEnabled);
		return super.writeToNBT(tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		ignoresPlayers = tag.getBoolean("ignore_players");
		ignoresConditions = tag.getBoolean("ignore_conditions");
		ignoresCap = tag.getBoolean("ignore_cap");
		redstoneEnabled = tag.getBoolean("redstone_control");
		super.readFromNBT(tag);
	}

	public void setIgnoresPlayers(boolean val) {
		ignoresPlayers = val;
	}

	public void setIgnoresConditions(boolean val) {
		ignoresConditions = val;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
	}

	public class SpawnerLogicExt extends MobSpawnerBaseLogic {

		@Override
		public void broadcastEvent(int id) {
			TileSpawnerExt.this.world.addBlockEvent(TileSpawnerExt.this.pos, Blocks.MOB_SPAWNER, id, 0);
		}

		@Override
		public World getSpawnerWorld() {
			return TileSpawnerExt.this.world;
		}

		@Override
		public BlockPos getSpawnerPosition() {
			return TileSpawnerExt.this.pos;
		}

		@Override
		public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_) {
			super.setNextSpawnData(p_184993_1_);

			if (this.getSpawnerWorld() != null) {
				IBlockState iblockstate = this.getSpawnerWorld().getBlockState(this.getSpawnerPosition());
				this.getSpawnerWorld().notifyBlockUpdate(TileSpawnerExt.this.pos, iblockstate, iblockstate, 4);
			}
		}

		private boolean isActivated() {
			BlockPos blockpos = this.getSpawnerPosition();
			boolean flag = ignoresPlayers || this.getSpawnerWorld().isAnyPlayerWithinRangeAt(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, this.activatingRangeFromPlayer);
			return flag && (!redstoneEnabled || world.isBlockPowered(blockpos));
		}

		private void resetTimer() {
			if (this.maxSpawnDelay <= this.minSpawnDelay) {
				this.spawnDelay = this.minSpawnDelay;
			} else {
				int i = this.maxSpawnDelay - this.minSpawnDelay;
				this.spawnDelay = this.minSpawnDelay + this.getSpawnerWorld().rand.nextInt(i);
			}

			if (!this.potentialSpawns.isEmpty()) {
				this.setNextSpawnData(WeightedRandom.getRandomItem(this.getSpawnerWorld().rand, this.potentialSpawns));
			}

			this.broadcastEvent(1);
		}

		@Override
		public void updateSpawner() {
			if (!this.isActivated()) {
				this.prevMobRotation = this.mobRotation;
			} else {
				BlockPos blockpos = this.getSpawnerPosition();

				if (this.getSpawnerWorld().isRemote) {
					double d3 = blockpos.getX() + this.getSpawnerWorld().rand.nextFloat();
					double d4 = blockpos.getY() + this.getSpawnerWorld().rand.nextFloat();
					double d5 = blockpos.getZ() + this.getSpawnerWorld().rand.nextFloat();
					this.getSpawnerWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d4, d5, 0.0D, 0.0D, 0.0D);
					this.getSpawnerWorld().spawnParticle(EnumParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D);

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
						NBTTagCompound tag = this.spawnData.getNbt();
						NBTTagList posList = tag.getTagList("Pos", 6);
						World world = this.getSpawnerWorld();
						int j = posList.tagCount();
						double x = j >= 1 ? posList.getDoubleAt(0) : blockpos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * this.spawnRange + 0.5D;
						double y = j >= 2 ? posList.getDoubleAt(1) : (double) (blockpos.getY() + world.rand.nextInt(3) - 1);
						double z = j >= 3 ? posList.getDoubleAt(2) : blockpos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * this.spawnRange + 0.5D;

						if (tag.hasKey("Offset")) {
							NBTTagList offsets = tag.getTagList("Offset", 6);
							j = posList.tagCount();
							x = j >= 1 ? this.getSpawnerPosition().getX() + offsets.getDoubleAt(0) : x;
							y = j >= 2 ? this.getSpawnerPosition().getY() + offsets.getDoubleAt(1) : y;
							z = j >= 3 ? this.getSpawnerPosition().getZ() + offsets.getDoubleAt(2) : z;
						}

						Entity entity = AnvilChunkLoader.readWorldEntityPos(tag, world, x, y, z, false);

						if (entity == null) { return; }

						if (!ignoresCap) {
							int k = world.getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1)).grow(this.spawnRange)).size();

							if (k >= this.maxNearbyEntities) {
								this.resetTimer();
								return;
							}
						}

						EntityLiving entityliving = entity instanceof EntityLiving ? (EntityLiving) entity : null;
						entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, world.rand.nextFloat() * 360.0F, 0.0F);

						if (entityliving == null || ignoresConditions || net.minecraftforge.event.ForgeEventFactory.canEntitySpawnSpawner(entityliving, getSpawnerWorld(), (float) entity.posX, (float) entity.posY, (float) entity.posZ, this)) {
							if (this.spawnData.getNbt().getSize() == 1 && this.spawnData.getNbt().hasKey("id", 8) && entity instanceof EntityLiving) {
								if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entityliving, this.getSpawnerWorld(), (float) entity.posX, (float) entity.posY, (float) entity.posZ, this)) ((EntityLiving) entity).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);
							}

							AnvilChunkLoader.spawnEntity(entity, world);
							world.playEvent(2004, blockpos, 0);

							if (entityliving != null) {
								entityliving.spawnExplosionParticle();
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
