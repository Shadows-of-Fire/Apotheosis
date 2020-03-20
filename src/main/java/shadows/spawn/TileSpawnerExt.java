package shadows.spawn;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.IMob;
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
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

public class TileSpawnerExt extends TileEntityMobSpawner {

	public boolean ignoresPlayers = false;
	public boolean ignoresConditions = false;
	public boolean ignoresCap = false;
	public boolean redstoneEnabled = false;

	public TileSpawnerExt() {
		spawnerLogic = new SpawnerLogicExt();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setBoolean("ignore_players", ignoresPlayers);
		tag.setBoolean("ignore_conditions", ignoresConditions);
		tag.setBoolean("ignore_cap", ignoresCap);
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
		readFromNBT(pkt.getNbtCompound());
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

			if (getSpawnerWorld() != null) {
				IBlockState iblockstate = getSpawnerWorld().getBlockState(getSpawnerPosition());
				getSpawnerWorld().notifyBlockUpdate(TileSpawnerExt.this.pos, iblockstate, iblockstate, 4);
			}
		}

		private boolean isActivated() {
			BlockPos blockpos = getSpawnerPosition();
			boolean flag = ignoresPlayers || getSpawnerWorld().isAnyPlayerWithinRangeAt(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, activatingRangeFromPlayer);
			return flag && (!redstoneEnabled || world.isBlockPowered(blockpos));
		}

		private void resetTimer() {
			if (maxSpawnDelay <= minSpawnDelay) {
				spawnDelay = minSpawnDelay;
			} else {
				int i = maxSpawnDelay - minSpawnDelay;
				spawnDelay = minSpawnDelay + getSpawnerWorld().rand.nextInt(i);
			}

			if (!potentialSpawns.isEmpty()) {
				setNextSpawnData(WeightedRandom.getRandomItem(getSpawnerWorld().rand, potentialSpawns));
			}

			broadcastEvent(1);
		}

		@Override
		public void updateSpawner() {
			if (!isActivated()) {
				prevMobRotation = mobRotation;
			} else {
				BlockPos blockpos = getSpawnerPosition();

				if (getSpawnerWorld().isRemote) {
					double d3 = blockpos.getX() + getSpawnerWorld().rand.nextFloat();
					double d4 = blockpos.getY() + getSpawnerWorld().rand.nextFloat();
					double d5 = blockpos.getZ() + getSpawnerWorld().rand.nextFloat();
					getSpawnerWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d4, d5, 0.0D, 0.0D, 0.0D);
					getSpawnerWorld().spawnParticle(EnumParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D);

					if (spawnDelay > 0) {
						--spawnDelay;
					}

					prevMobRotation = mobRotation;
					mobRotation = (mobRotation + 1000.0F / (spawnDelay + 200.0F)) % 360.0D;
				} else {
					if (spawnDelay == -1) {
						resetTimer();
					}

					if (spawnDelay > 0) {
						--spawnDelay;
						return;
					}

					boolean flag = false;

					for (int i = 0; i < spawnCount; ++i) {
						NBTTagCompound tag = spawnData.getNbt();
						NBTTagList posList = tag.getTagList("Pos", 6);
						World world = getSpawnerWorld();
						int j = posList.tagCount();
						double x = j >= 1 ? posList.getDoubleAt(0) : blockpos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * spawnRange + 0.5D;
						double y = j >= 2 ? posList.getDoubleAt(1) : (double) (blockpos.getY() + world.rand.nextInt(3) - 1);
						double z = j >= 3 ? posList.getDoubleAt(2) : blockpos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * spawnRange + 0.5D;

						if (tag.hasKey("Offset")) {
							NBTTagList offsets = tag.getTagList("Offset", 6);
							j = posList.tagCount();
							x = j >= 1 ? getSpawnerPosition().getX() + offsets.getDoubleAt(0) : x;
							y = j >= 2 ? getSpawnerPosition().getY() + offsets.getDoubleAt(1) : y;
							z = j >= 3 ? getSpawnerPosition().getZ() + offsets.getDoubleAt(2) : z;
						}

						Entity entity = AnvilChunkLoader.readWorldEntityPos(tag, world, x, y, z, false);

						if (entity == null) { return; }

						if (!ignoresCap) {
							int k = world.getEntitiesWithinAABB(entity.getClass(), new AxisAlignedBB(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + 1, blockpos.getY() + 1, blockpos.getZ() + 1).grow(spawnRange)).size();

							if (k >= maxNearbyEntities) {
								resetTimer();
								return;
							}
						}

						EntityLiving entityliving = entity instanceof EntityLiving ? (EntityLiving) entity : null;
						entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, world.rand.nextFloat() * 360.0F, 0.0F);

						if (entityliving == null || (ignoresConditions && (!(entityliving instanceof IMob) || world.getDifficulty() != EnumDifficulty.PEACEFUL)) || net.minecraftforge.event.ForgeEventFactory.canEntitySpawnSpawner(entityliving, getSpawnerWorld(), (float) entity.posX, (float) entity.posY, (float) entity.posZ, this)) {
							if (spawnData.getNbt().getSize() == 1 && spawnData.getNbt().hasKey("id", 8) && entity instanceof EntityLiving) {
								if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entityliving, getSpawnerWorld(), (float) entity.posX, (float) entity.posY, (float) entity.posZ, this)) ((EntityLiving) entity).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);
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
						resetTimer();
					}
				}
			}
		}
	}

}
