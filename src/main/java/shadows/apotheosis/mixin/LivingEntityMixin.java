package shadows.apotheosis.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import shadows.apotheosis.Apoth;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	/**
	 * @author Shadows
	 * @reason Injection of the Sundering potion effect, which is applied during resistance calculations.
	 * @param value Damage modifier percentage after resistance has been applied [1.0, -inf]
	 * @param max Zero
	 * @param source The damage source
	 * @param damage The initial damage amount
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"), method = "getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F")
	public float apoth_sundering_max(float value, float max, DamageSource source, float damage) {
		if (Apoth.Effects.SUNDERING.isPresent() && this.hasEffect(Apoth.Effects.SUNDERING.get()) && source != DamageSource.OUT_OF_WORLD) {
			int level = this.getEffect(Apoth.Effects.SUNDERING.get()).getAmplifier() + 1;
			value += damage * level * 0.2F;
		}
		return Math.max(value, max);
	}

	/**
	 * @author Shadows
	 * @reason Used to enter an if-condition so the above mixin always triggers.
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"), method = "getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F")
	public boolean apoth_sundering_hasEffect(LivingEntity ths, MobEffect effect) {
		return true;
	}

	/**
	 * @author Shadows
	 * @reason Used to prevent an NPE since we're faking true on hasEffect
	 */
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getAmplifier()I"), method = "getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F")
	public int apoth_sundering_getAmplifier(@Nullable MobEffectInstance inst) {
		return inst == null ? -1 : inst.getAmplifier();
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
