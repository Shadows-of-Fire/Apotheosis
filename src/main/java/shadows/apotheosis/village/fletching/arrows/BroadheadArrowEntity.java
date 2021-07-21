package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import shadows.apotheosis.ApotheosisObjects;

public class BroadheadArrowEntity extends ArrowEntity {

	public BroadheadArrowEntity(EntityType<? extends ArrowEntity> t, World world) {
		super(t, world);
	}

	public BroadheadArrowEntity(World world) {
		super(ApotheosisObjects.BH_ARROW_ENTITY, world);
	}

	public BroadheadArrowEntity(LivingEntity shooter, World world) {
		super(world, shooter);
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(ApotheosisObjects.BROADHEAD_ARROW);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
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
		EffectInstance bleed = living.getEffect(ApotheosisObjects.BLEEDING);
		if (bleed != null) {
			living.addEffect(new EffectInstance(ApotheosisObjects.BLEEDING, bleed.getDuration() + 60, bleed.getAmplifier() + 1));
		} else {
			living.addEffect(new EffectInstance(ApotheosisObjects.BLEEDING, 300));
		}
	}
}