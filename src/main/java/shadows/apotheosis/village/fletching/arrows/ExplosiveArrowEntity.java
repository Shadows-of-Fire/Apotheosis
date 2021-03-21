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
	protected ItemStack getArrowStack() {
		return new ItemStack(ApotheosisObjects.EXPLOSIVE_ARROW);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void arrowHit(LivingEntity living) {
		if (!this.world.isRemote) {
			Entity shooter = this.func_234616_v_();
			LivingEntity explosionSource = null;
			if (shooter instanceof LivingEntity) explosionSource = (LivingEntity) shooter;
			this.world.createExplosion(null, DamageSource.causeExplosionDamage(explosionSource), null, living.getPosX(), living.getPosY(), living.getPosZ(), 2, false, Mode.DESTROY);
			this.remove();
		}
	}

	@Override //onBlockHit
	protected void func_230299_a_(BlockRayTraceResult res) {
		super.func_230299_a_(res);
		Vector3d vec = res.getHitVec();
		if (!this.world.isRemote) {
			Entity shooter = this.func_234616_v_();
			LivingEntity explosionSource = null;
			if (shooter instanceof LivingEntity) explosionSource = (LivingEntity) shooter;
			this.world.createExplosion(null, DamageSource.causeExplosionDamage(explosionSource), null, vec.getX(), vec.getY(), vec.getZ(), 3, false, Mode.DESTROY);
			this.remove();
		}
	}
}