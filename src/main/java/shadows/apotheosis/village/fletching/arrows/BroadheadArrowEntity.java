package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import shadows.apotheosis.ApotheosisObjects;

public class BroadheadArrowEntity extends Arrow {

	public BroadheadArrowEntity(EntityType<? extends Arrow> t, Level world) {
		super(t, world);
	}

	public BroadheadArrowEntity(Level world) {
		super(ApotheosisObjects.BH_ARROW_ENTITY, world);
	}

	public BroadheadArrowEntity(LivingEntity shooter, Level world) {
		super(world, shooter);
	}

	public BroadheadArrowEntity(Level world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(ApotheosisObjects.BROADHEAD_ARROW);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public EntityType<?> getType() {
		return ApotheosisObjects.BH_ARROW_ENTITY;
	}

	@Override
	public int getColor() {
		return -1;
	}

	@Override
	protected void doPostHurtEffects(LivingEntity living) {
		MobEffectInstance bleed = living.getEffect(ApotheosisObjects.BLEEDING);
		if (bleed != null) {
			living.addEffect(new MobEffectInstance(ApotheosisObjects.BLEEDING, bleed.getDuration() + 60, bleed.getAmplifier() + 1));
		} else {
			living.addEffect(new MobEffectInstance(ApotheosisObjects.BLEEDING, 300));
		}
	}

	public BroadheadArrowEntity bleed() {
		this.addEffect(new MobEffectInstance(ApotheosisObjects.BLEEDING, 300));
		return this;
	}
}