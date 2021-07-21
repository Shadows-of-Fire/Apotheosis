package shadows.apotheosis.village.fletching.arrows;

import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.util.BlockUtil;

public class MiningArrowEntity extends AbstractArrowEntity implements IEntityAdditionalSpawnData {

	protected int blocksBroken = 0;
	protected UUID playerId = null;
	protected ItemStack breakerItem = ItemStack.EMPTY;
	protected Type type = Type.IRON;

	public MiningArrowEntity(EntityType<? extends AbstractArrowEntity> t, World world) {
		super(t, world);
		this.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
	}

	public MiningArrowEntity(World world) {
		this(ApotheosisObjects.MN_ARROW_ENTITY, world);
	}

	public MiningArrowEntity(LivingEntity shooter, World world, ItemStack breakerItem, Type type) {
		super(ApotheosisObjects.MN_ARROW_ENTITY, shooter, world);
		this.breakerItem = breakerItem;
		this.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
		this.type = type;
		this.playerId = shooter.getUUID();
	}

	@Override
	protected ItemStack getPickupItem() {
		return ItemStack.EMPTY; //This arrow can never be picked up.
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void tick() {
		if (!this.level.isClientSide) {
			this.setSharedFlag(6, this.isGlowing());
		}

		this.baseTick();

		boolean noClip = this.isNoPhysics();
		Vector3d motion = this.getDeltaMovement();
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			float f = MathHelper.sqrt(getHorizontalDistanceSqr(motion));
			this.yRot = (float) (MathHelper.atan2(motion.x, motion.z) * (180F / (float) Math.PI));
			this.xRot = (float) (MathHelper.atan2(motion.y, f) * (180F / (float) Math.PI));
			this.yRotO = this.yRot;
			this.xRotO = this.xRot;
		}

		BlockPos blockpos = this.blockPosition();
		BlockState blockstate = this.level.getBlockState(blockpos);
		if (!blockstate.isAir(this.level, blockpos) && !noClip) {
			VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
			if (!voxelshape.isEmpty()) {
				Vector3d vector3d1 = this.position();

				for (AxisAlignedBB axisalignedbb : voxelshape.toAabbs()) {
					if (axisalignedbb.move(blockpos).contains(vector3d1)) {
						this.inGround = true;
						break;
					}
				}
			}
		}

		if (this.inGround) {
			this.remove();
		} else {
			this.inGroundTime = 0;
			Vector3d pos = this.position();
			Vector3d posNextTick = pos.add(motion);
			int iterations = 0;
			while (!this.level.isClientSide && this.isAlive()) {
				RayTraceResult traceResult = this.level.clip(new RayTraceContext(pos, posNextTick, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
				if (traceResult.getType() == RayTraceResult.Type.MISS) break;
				else if (traceResult.getType() == RayTraceResult.Type.BLOCK) {
					this.onHit(traceResult);
				}
				if (iterations++ > 10) break; //Safeguard in case mods do weird stuff
			}

			motion = this.getDeltaMovement();
			double velX = motion.x;
			double velY = motion.y;
			double velZ = motion.z;
			if (this.isCritArrow()) {
				for (int i = 0; i < 4; ++i) {
					this.level.addParticle(ParticleTypes.CRIT, this.getX() + velX * i / 4.0D, this.getY() + velY * i / 4.0D, this.getZ() + velZ * i / 4.0D, -velX, -velY + 0.2D, -velZ);
				}
			}

			double nextTickX = this.getX() + velX;
			double nextTickY = this.getY() + velY;
			double nextTickZ = this.getZ() + velZ;
			float f1 = MathHelper.sqrt(getHorizontalDistanceSqr(motion));
			if (noClip) {
				this.yRot = (float) (MathHelper.atan2(-velX, -velZ) * (180F / (float) Math.PI));
			} else {
				this.yRot = (float) (MathHelper.atan2(velX, velZ) * (180F / (float) Math.PI));
			}

			this.xRot = (float) (MathHelper.atan2(velY, f1) * (180F / (float) Math.PI));
			this.xRot = lerpRotation(this.xRotO, this.xRot);
			this.yRot = lerpRotation(this.yRotO, this.yRot);
			float f2 = 0.99F;
			if (this.isInWater()) {
				for (int j = 0; j < 4; ++j) {
					this.level.addParticle(ParticleTypes.BUBBLE, nextTickX - velX * 0.25D, nextTickY - velY * 0.25D, nextTickZ - velZ * 0.25D, velX, velY, velZ);
				}

				f2 = this.getWaterInertia();
			}

			this.setDeltaMovement(motion.scale(f2));
			if (!this.isNoGravity() && !noClip) {
				Vector3d vector3d4 = this.getDeltaMovement();
				this.setDeltaMovement(vector3d4.x, vector3d4.y - 0.05F, vector3d4.z);
			}

			this.setPos(nextTickX, nextTickY, nextTickZ);
		}
	}

	@Override
	protected void onHitBlock(BlockRayTraceResult res) {
		this.breakBlock(res.getBlockPos());
	}

	@Override
	public boolean isNoGravity() {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("blocks_broken", this.blocksBroken);
		if (this.playerId != null) compound.putUUID("player_id", this.playerId);
		compound.put("breaker_item", this.breakerItem.serializeNBT());
		compound.putByte("arrow_type", (byte) this.type.ordinal());
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		this.blocksBroken = compound.getInt("blocks_broken");
		if (compound.contains("player_id")) this.playerId = compound.getUUID("player_id");
		this.breakerItem = ItemStack.of(compound.getCompound("breaker_item"));
		this.type = Type.values()[compound.getByte("arrow_type")];
	}

	@Override
	public void writeSpawnData(PacketBuffer buf) {
		buf.writeByte(this.type.ordinal());
	}

	@Override
	public void readSpawnData(PacketBuffer buf) {
		this.type = Type.values()[buf.readByte()];
	}

	@SuppressWarnings("deprecation")
	protected void breakBlock(BlockPos pos) {
		if (!this.level.isClientSide && !this.level.getBlockState(pos).isAir(this.level, pos)) {
			if (BlockUtil.breakExtraBlock((ServerWorld) this.level, pos, this.breakerItem, this.playerId)) {
				if (++this.blocksBroken >= 12) {
					this.remove();
				}
			} else {
				this.playSound(SoundEvents.ANVIL_PLACE, 1.0F, 1.5F / (this.random.nextFloat() * 0.2F + 0.9F));
				this.remove();
			}
		}
	}

	public static enum Type {
		IRON(new ResourceLocation(Apotheosis.MODID, "textures/entity/iron_mining_arrow.png")),
		DIAMOND(new ResourceLocation(Apotheosis.MODID, "textures/entity/diamond_mining_arrow.png"));

		private final ResourceLocation texture;

		Type(ResourceLocation texture) {
			this.texture = texture;
		}

		public ResourceLocation getTexture() {
			return this.texture;
		}
	}
}