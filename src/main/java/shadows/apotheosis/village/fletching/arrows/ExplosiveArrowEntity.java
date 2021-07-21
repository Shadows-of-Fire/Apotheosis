package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import shadows.apotheosis.ApotheosisObjects;

public class ExplosiveArrowEntity extends AbstractArrowEntity {

	public ExplosiveArrowEntity(EntityType<? extends AbstractArrowEntity> t, World world) {
		super(t, world);
	}

	public ExplosiveArrowEntity(World world) {
		super(ApotheosisObjects.EX_ARROW_ENTITY, world);
	}

	public ExplosiveArrowEntity(LivingEntity shooter, World world) {
		super(ApotheosisObjects.EX_ARROW_ENTITY, shooter, world);
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(ApotheosisObjects.EXPLOSIVE_ARROW);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void doPostHurtEffects(LivingEntity living) {
		if (!this.level.isClientSide) {
			Entity shooter = this.getOwner();
			LivingEntity explosionSource = null;
			if (shooter instanceof LivingEntity) explosionSource = (LivingEntity) shooter;
			this.level.explode(null, DamageSource.explosion(explosionSource), null, living.getX(), living.getY(), living.getZ(), 2, false, Mode.DESTROY);
			this.remove();
		}
	}

	@Override //onBlockHit
	protected void onHitBlock(BlockRayTraceResult res) {
		super.onHitBlock(res);
		Vector3d vec = res.getLocation();
		if (!this.level.isClientSide) {
			Entity shooter = this.getOwner();
			LivingEntity explosionSource = null;
			if (shooter instanceof LivingEntity) explosionSource = (LivingEntity) shooter;
			this.level.explode(null, DamageSource.explosion(explosionSource), null, vec.x(), vec.y(), vec.z(), 3, false, Mode.DESTROY);
			this.remove();
		}
	}
}