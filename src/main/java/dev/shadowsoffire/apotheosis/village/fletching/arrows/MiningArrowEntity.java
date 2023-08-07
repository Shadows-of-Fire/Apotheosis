package dev.shadowsoffire.apotheosis.village.fletching.arrows;

import java.util.UUID;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class MiningArrowEntity extends AbstractArrow implements IEntityAdditionalSpawnData {

    protected int blocksBroken = 0;
    protected UUID playerId = null;
    protected ItemStack breakerItem = ItemStack.EMPTY;
    protected Type type = Type.IRON;

    public MiningArrowEntity(EntityType<? extends AbstractArrow> t, Level world) {
        super(t, world);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    public MiningArrowEntity(Level world) {
        this(Apoth.Entities.MINING_ARROW.get(), world);
    }

    public MiningArrowEntity(LivingEntity shooter, Level world, ItemStack breakerItem, Type type) {
        super(Apoth.Entities.MINING_ARROW.get(), shooter, world);
        this.breakerItem = breakerItem;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.type = type;
        this.playerId = shooter.getUUID();
    }

    public MiningArrowEntity(Level world, double x, double y, double z, ItemStack breakerItem, Type type) {
        super(Apoth.Entities.MINING_ARROW.get(), x, y, z, world);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.breakerItem = breakerItem;
        this.type = type;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // This arrow can never be picked up.
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick() {
        if (!this.level().isClientSide) {
            this.setSharedFlag(6, this.isCurrentlyGlowing());
        }

        this.baseTick();

        boolean noClip = this.isNoPhysics();
        Vec3 motion = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = motion.horizontalDistance();
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (180F / (float) Math.PI)));
            this.setXRot((float) (Mth.atan2(motion.y, d0) * (180F / (float) Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level().getBlockState(blockpos);
        if (!blockstate.isAir() && !noClip) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3 vec31 = this.position();

                for (AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(vec31)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.inGround) {
            this.discard();
        }
        else {
            this.inGroundTime = 0;
            Vec3 pos = this.position();
            Vec3 posNextTick = pos.add(motion);
            int iterations = 0;
            while (!this.level().isClientSide && this.isAlive()) {
                HitResult traceResult = this.level().clip(new ClipContext(pos, posNextTick, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (traceResult.getType() == HitResult.Type.MISS) break;
                else if (traceResult.getType() == HitResult.Type.BLOCK) {
                    this.onHit(traceResult);
                }
                if (iterations++ > 10) break; // Safeguard in case mods do weird stuff
            }

            motion = this.getDeltaMovement();
            double dX = motion.x;
            double dY = motion.y;
            double dZ = motion.z;
            if (this.isCritArrow()) {
                for (int i = 0; i < 4; ++i) {
                    this.level().addParticle(ParticleTypes.CRIT, this.getX() + dX * i / 4.0D, this.getY() + dY * i / 4.0D, this.getZ() + dZ * i / 4.0D, -dX, -dY + 0.2D, -dZ);
                }
            }

            double nextX = this.getX() + dX;
            double nextY = this.getY() + dY;
            double nextZ = this.getZ() + dZ;
            double hDist = motion.horizontalDistance();
            if (noClip) {
                this.setYRot((float) (Mth.atan2(-dX, -dZ) * (180F / (float) Math.PI)));
            }
            else {
                this.setYRot((float) (Mth.atan2(dX, dZ) * (180F / (float) Math.PI)));
            }

            this.setXRot((float) (Mth.atan2(dY, hDist) * (180F / (float) Math.PI)));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f = 0.99F;
            if (this.isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    this.level().addParticle(ParticleTypes.BUBBLE, nextX - dX * 0.25D, nextY - dY * 0.25D, nextZ - dZ * 0.25D, dX, dY, dZ);
                }

                f = this.getWaterInertia();
            }

            this.setDeltaMovement(motion.scale(f));
            if (!this.isNoGravity() && !noClip) {
                Vec3 vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - 0.05F, vec34.z);
            }

            this.setPos(nextX, nextY, nextZ);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult res) {
        this.breakBlock(res.getBlockPos());
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("blocks_broken", this.blocksBroken);
        if (this.playerId != null) compound.putUUID("player_id", this.playerId);
        compound.put("breaker_item", this.breakerItem.serializeNBT());
        compound.putByte("arrow_type", (byte) this.type.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.blocksBroken = compound.getInt("blocks_broken");
        if (compound.contains("player_id")) this.playerId = compound.getUUID("player_id");
        this.breakerItem = ItemStack.of(compound.getCompound("breaker_item"));
        this.type = Type.values()[compound.getByte("arrow_type")];
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeByte(this.type.ordinal());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        this.type = Type.values()[buf.readByte()];
    }

    @SuppressWarnings("deprecation")
    protected void breakBlock(BlockPos pos) {
        if (!this.level().isClientSide && !this.level().getBlockState(pos).isAir()) {
            if (BlockUtil.breakExtraBlock((ServerLevel) this.level(), pos, this.breakerItem, this.playerId)) {
                if (++this.blocksBroken >= 12) {
                    this.discard();
                }
            }
            else {
                this.playSound(SoundEvents.ANVIL_PLACE, 1.0F, 1.5F / (this.random.nextFloat() * 0.2F + 0.9F));
                this.discard();
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
