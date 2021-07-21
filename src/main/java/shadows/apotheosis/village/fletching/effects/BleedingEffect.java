package shadows.apotheosis.village.fletching.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import shadows.apotheosis.Apotheosis;

public class BleedingEffect extends Effect {

	public static final DamageSource BLEEDING = new DamageSource(Apotheosis.MODID + ".bleeding").bypassArmor();

	public BleedingEffect() {
		super(EffectType.HARMFUL, 0x8B0000);
	}

	@Override
	public void applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
		entityLivingBaseIn.hurt(BLEEDING, 1.0F + amplifier);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return duration % 40 == 0;
	}

}