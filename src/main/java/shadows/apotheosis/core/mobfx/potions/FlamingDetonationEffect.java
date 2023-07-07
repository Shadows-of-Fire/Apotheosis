package shadows.apotheosis.core.mobfx.potions;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.Apotheosis;

public class FlamingDetonationEffect extends MobEffect {

	public static final DamageSource DETONATION = new DamageSource(Apotheosis.MODID + ".detonation").setMagic().bypassArmor();

	public FlamingDetonationEffect() {
		super(MobEffectCategory.HARMFUL, 0xFFD800);
	}

	@Override
	public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amp) {
		super.removeAttributeModifiers(entity, map, amp);
		int ticks = entity.getRemainingFireTicks();
		if (ticks > 0) {
			entity.setRemainingFireTicks(0);
			entity.hurt(DETONATION, (1 + amp) * ticks / 14F);
			ServerLevel level = (ServerLevel) entity.level;
			AABB bb = entity.getBoundingBox();
			level.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 100, bb.getXsize(), bb.getYsize(), bb.getZsize(), 0.25);
			level.playSound(null, entity, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1, 1.2F);
		}
	}

}