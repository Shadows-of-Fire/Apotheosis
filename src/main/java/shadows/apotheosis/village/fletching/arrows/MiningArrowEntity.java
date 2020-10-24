package shadows.apotheosis.village.fletching.arrows;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.util.BlockUtil;

public class MiningArrowEntity extends AbstractArrowEntity {

	protected int blocksBroken = 0;
	protected UUID playerId = null;
	protected ItemStack breakerItem = ItemStack.EMPTY;

	public MiningArrowEntity(EntityType<? extends AbstractArrowEntity> t, World world) {
		super(t, world);
		this.noClip = true;
	}

	public MiningArrowEntity(World world) {
		super(ApotheosisObjects.MN_ARROW_ENTITY, world);
		this.noClip = true;
	}

	public MiningArrowEntity(LivingEntity shooter, World world, ItemStack breakerItem) {
		super(ApotheosisObjects.MN_ARROW_ENTITY, shooter, world);
		this.breakerItem = new ItemStack(Items.IRON_PICKAXE);
	}

	@Override
	protected ItemStack getArrowStack() {
		return new ItemStack(ApotheosisObjects.MINING_ARROW);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public void tick() {
		if (!this.world.isRemote) {
			this.setFlag(6, this.isGlowing());
		}

		this.baseTick();

		boolean noClip = this.getNoClip();
		Vector3d motion = this.getMotion();
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(horizontalMag(motion));
			this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI));
			this.rotationPitch = (float) (MathHelper.atan2(motion.y, (double) f) * (double) (180F / (float) Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
		}

		BlockPos blockpos = this.getPosition();
		BlockState blockstate = this.world.getBlockState(blockpos);
		if (!blockstate.isAir(this.world, blockpos) && !noClip) {
			VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
			if (!voxelshape.isEmpty()) {
				Vector3d vector3d1 = this.getPositionVec();

				for (AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
					if (axisalignedbb.offset(blockpos).contains(vector3d1)) {
						this.inGround = true;
						break;
					}
				}
			}
		}

		if (this.inGround) {
			this.remove();
		} else {
			this.timeInGround = 0;
			Vector3d pos = this.getPositionVec();
			Vector3d posNextTick = pos.add(motion);
			while (!world.isRemote && this.isAlive()) {
				RayTraceResult traceResult = this.world.rayTraceBlocks(new RayTraceContext(pos, posNextTick, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
				if (traceResult.getType() == Type.MISS) break;
				else if (traceResult.getType() == Type.BLOCK) {
					this.onImpact(traceResult);
				}
			}

			motion = this.getMotion();
			double velX = motion.x;
			double velY = motion.y;
			double velZ = motion.z;
			if (this.getIsCritical()) {
				for (int i = 0; i < 4; ++i) {
					this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + velX * (double) i / 4.0D, this.getPosY() + velY * (double) i / 4.0D, this.getPosZ() + velZ * (double) i / 4.0D, -velX, -velY + 0.2D, -velZ);
				}
			}

			double nextTickX = this.getPosX() + velX;
			double nextTickY = this.getPosY() + velY;
			double nextTickZ = this.getPosZ() + velZ;
			float f1 = MathHelper.sqrt(horizontalMag(motion));
			if (noClip) {
				this.rotationYaw = (float) (MathHelper.atan2(-velX, -velZ) * (double) (180F / (float) Math.PI));
			} else {
				this.rotationYaw = (float) (MathHelper.atan2(velX, velZ) * (double) (180F / (float) Math.PI));
			}

			this.rotationPitch = (float) (MathHelper.atan2(velY, (double) f1) * (double) (180F / (float) Math.PI));
			this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
			this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
			float f2 = 0.99F;
			if (this.isInWater()) {
				for (int j = 0; j < 4; ++j) {
					this.world.addParticle(ParticleTypes.BUBBLE, nextTickX - velX * 0.25D, nextTickY - velY * 0.25D, nextTickZ - velZ * 0.25D, velX, velY, velZ);
				}

				f2 = this.getWaterDrag();
			}

			this.setMotion(motion.scale((double) f2));
			if (!this.hasNoGravity() && !noClip) {
				Vector3d vector3d4 = this.getMotion();
				this.setMotion(vector3d4.x, vector3d4.y - (double) 0.05F, vector3d4.z);
			}

			this.setPosition(nextTickX, nextTickY, nextTickZ);
		}
	}

	@Override
	protected void func_230299_a_(BlockRayTraceResult res) {
		breakBlock(res.getPos());
	}

	@Override
	public boolean hasNoGravity() {
		return false;
	}

	@Override
	public void setShooter(@Nullable Entity entity) {
		super.setShooter(entity);
		if (entity instanceof PlayerEntity) playerId = entity.getUniqueID();
		else playerId = null;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("blocks_broken", blocksBroken);
		if (playerId != null) compound.putUniqueId("player_id", playerId);
		compound.put("breaker_item", breakerItem.serializeNBT());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		blocksBroken = compound.getInt("blocks_broken");
		if (compound.contains("player_id")) playerId = compound.getUniqueId("player_id");
		breakerItem = ItemStack.read(compound.getCompound("breaker_item"));
	}

	protected void breakBlock(BlockPos pos) {
		if (!world.isRemote && !world.getBlockState(pos).isAir(world, pos)) {
			if (BlockUtil.breakExtraBlock((ServerWorld) world, pos, breakerItem, playerId)) {
				if (++blocksBroken >= 12) {
					this.remove();
				}
			} else {
				this.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 1.0F, 1.5F / (this.rand.nextFloat() * 0.2F + 0.9F));
				this.remove();
			}
		}
	}
}