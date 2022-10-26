package shadows.apotheosis.mixin;

import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import shadows.apotheosis.Apoth;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Redirect(method="getDamageAfterMagicAbsorb", at=@At(value="INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
	public boolean fakeHasEffect(LivingEntity instance, MobEffect pPotion){
		return true;
	}

	@Redirect(method = "getDamageAfterMagicAbsorb", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getAmplifier()I"))
	public int fakeGetAmplifier(MobEffectInstance instance) {
		return -1;
	}

	@Redirect(method = "getDamageAfterMagicAbsorb", at=@At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
	public float fakeMax(float a, float b) {
		float mult = 1;
		if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
			int level = this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1;
			mult -= 0.2 * level;
		}
		if (Apoth.Effects.SUNDERING != null && this.hasEffect(Apoth.Effects.SUNDERING)) {
			int level = this.getEffect(Apoth.Effects.SUNDERING).getAmplifier() + 1;
			mult += 0.2 * level;
		}
		return a * mult;
	}

	@Shadow
	public abstract boolean hasEffect(MobEffect ef);

	@Shadow
	public abstract MobEffectInstance getEffect(MobEffect ef);

	@Override
	public int getTeamColor() {
		if (super.getTeamColor() == 16777215) {
			Component name = this.getCustomName();
			if (name != null && name.getStyle().getColor() != null) return name.getStyle().getColor().getValue();
		}
		return super.getTeamColor();
	}

}
