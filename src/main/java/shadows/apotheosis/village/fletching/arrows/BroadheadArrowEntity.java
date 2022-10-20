package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import shadows.apotheosis.Apoth;

public class BroadheadArrowEntity extends Arrow {

	public BroadheadArrowEntity(EntityType<? extends Arrow> t, Level world) {
		super(t, world);
	}

	public BroadheadArrowEntity(Level world) {
		super(Apoth.Entities.BROADHEAD_ARROW.get(), world);
	}

	public BroadheadArrowEntity(LivingEntity shooter, Level world) {
		super(world, shooter);
	}

	public BroadheadArrowEntity(Level world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(Apoth.Items.BROADHEAD_ARROW.get());
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public EntityType<?> getType() {
		return Apoth.Entities.BROADHEAD_ARROW.get();
	}

	@Override
	public int getColor() {
		return -1;
	}

	@Override
	protected void doPostHurtEffects(LivingEntity living) {
		MobEffectInstance bleed = living.getEffect(Apoth.Effects.BLEEDING.get());
		if (bleed != null) {
			living.addEffect(new MobEffectInstance(Apoth.Effects.BLEEDING.get(), bleed.getDuration() + 60, bleed.getAmplifier() + 1));
		} else {
			living.addEffect(new MobEffectInstance(Apoth.Effects.BLEEDING.get(), 300));
		}
	}

	public BroadheadArrowEntity bleed() {
		this.addEffect(new MobEffectInstance(Apoth.Effects.BLEEDING.get(), 300));
		return this;
	}
}