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
		this.pickupStatus = AbstractArrowEntity.PickupStatus.DISALLOWED;
	}

	public MiningArrowEntity(World world) {
		this(ApotheosisObjects.MN_ARROW_ENTITY, world);
	}

	public MiningArrowEntity(LivingEntity shooter, World world, ItemStack breakerItem, Type type) {
		super(ApotheosisObjects.MN_ARROW_ENTITY, shooter, world);
		this.breakerItem = breakerItem;
		this.pickupStatus = AbstractArrowEntity.PickupStatus.DISALLOWED;
		this.type = type;
		this.playerId = shooter.getUniqueID();
	}

	@Override
	protected ItemStack getArrowStack() {
		return ItemStack.EMPTY; //This arrow can never be picked up.
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void tick() {
		if (!this.world.isRemote) {
			this.setFlag(6, this.isGlowing());
		}

		this.baseTick();

		boolean noClip = this.getNoClip();
		Vector3d motion = this.getMotion();
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(horizontalMag(motion));
			this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (180F / (float) Math.PI));
			this.rotationPitch = (float) (MathHelper.atan2(motion.y, f) * (180F / (float) Math.PI));
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
			int iterations = 0;
			while (!this.world.isRemote && this.isAlive()) {
				RayTraceResult traceResult = this.world.rayTraceBlocks(new RayTraceContext(pos, posNextTick, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
				if (traceResult.getType() == RayTraceResult.Type.MISS) break;
				else if (traceResult.getType() == RayTraceResult.Type.BLOCK) {
					this.onImpact(traceResult);
				}
				if (iterations++ > 10) break; //Safeguard in case mods do weird stuff
			}

			motion = this.getMotion();
			double velX = motion.x;
			double velY = motion.y;
			double velZ = motion.z;
			if (this.getIsCritical()) {
				for (int i = 0; i < 4; ++i) {
					this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + velX * i / 4.0D, this.getPosY() + velY * i / 4.0D, this.getPosZ() + velZ * i / 4.0D, -velX, -velY + 0.2D, -velZ);
				}
			}

			double nextTickX = this.getPosX() + velX;
			double nextTickY = this.getPosY() + velY;
			double nextTickZ = this.getPosZ() + velZ;
			float f1 = MathHelper.sqrt(horizontalMag(motion));
			if (noClip) {
				this.rotationYaw = (float) (MathHelper.atan2(-velX, -velZ) * (180F / (float) Math.PI));
			} else {
				this.rotationYaw = (float) (MathHelper.atan2(velX, velZ) * (180F / (float) Math.PI));
			}

			this.rotationPitch = (float) (MathHelper.atan2(velY, f1) * (180F / (float) Math.PI));
			this.rotationPitch = func_234614_e_(this.prevRotationPitch, this.rotationPitch);
			this.rotationYaw = func_234614_e_(this.prevRotationYaw, this.rotationYaw);
			float f2 = 0.99F;
			if (this.isInWater()) {
				for (int j = 0; j < 4; ++j) {
					this.world.addParticle(ParticleTypes.BUBBLE, nextTickX - velX * 0.25D, nextTickY - velY * 0.25D, nextTickZ - velZ * 0.25D, velX, velY, velZ);
				}

				f2 = this.getWaterDrag();
			}

			this.setMotion(motion.scale(f2));
			if (!this.hasNoGravity() && !noClip) {
				Vector3d vector3d4 = this.getMotion();
				this.setMotion(vector3d4.x, vector3d4.y - 0.05F, vector3d4.z);
			}

			this.setPosition(nextTickX, nextTickY, nextTickZ);
		}
	}

	@Override
	protected void func_230299_a_(BlockRayTraceResult res) {
		this.breakBlock(res.getPos());
	}

	@Override
	public boolean hasNoGravity() {
		return false;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("blocks_broken", this.blocksBroken);
		if (this.playerId != null) compound.putUniqueId("player_id", this.playerId);
		compound.put("breaker_item", this.breakerItem.serializeNBT());
		compound.putByte("arrow_type", (byte) this.type.ordinal());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.blocksBroken = compound.getInt("blocks_broken");
		if (compound.contains("player_id")) this.playerId = compound.getUniqueId("player_id");
		this.breakerItem = ItemStack.read(compound.getCompound("breaker_item"));
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
		if (!this.world.isRemote && !this.world.getBlockState(pos).isAir(this.world, pos)) {
			if (BlockUtil.breakExtraBlock((ServerWorld) this.world, pos, this.breakerItem, this.playerId)) {
				if (++this.blocksBroken >= 12) {
					this.remove();
				}
			} else {
				this.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 1.0F, 1.5F / (this.rand.nextFloat() * 0.2F + 0.9F));
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