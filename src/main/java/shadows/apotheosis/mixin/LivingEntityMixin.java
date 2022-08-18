package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import shadows.apotheosis.Apoth;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	/**
	 * @author Shadows
	 * @reason Injection of Sundering Potion Effect
	 * Calculates damage taken based on potions. Required for sundering.
	 * Called from {@link LivingEntity#getDamageAfterMagicAbsorb(DamageSource, float)}
	 * TODO: Reduce to @Inject
	 */
	@Overwrite
	public float getDamageAfterMagicAbsorb(DamageSource source, float damage) {
		if (source.isBypassMagic()) {
			return damage;
		} else {
			float mult = 1;
			if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
				int level = this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1;
				mult -= 0.2 * level;
			}
			if (Apoth.Effects.SUNDERING != null && this.hasEffect(Apoth.Effects.SUNDERING) && source != DamageSource.OUT_OF_WORLD) {
				int level = this.getEffect(Apoth.Effects.SUNDERING).getAmplifier() + 1;
				mult += 0.2 * level;
			}

			float newDamage = damage * mult;
			float resisted = damage - newDamage;

			if (resisted > 0.0F && resisted < 3.4028235E37F) {
				if ((Object) this instanceof ServerPlayer sp) {
					sp.awardStat(Stats.CUSTOM.get(Stats.DAMAGE_RESISTED), Math.round(resisted * 10.0F));
				} else if (source.getEntity() instanceof ServerPlayer sp) {
					sp.awardStat(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_RESISTED), Math.round(resisted * 10.0F));
				}
			}

			damage = newDamage;

			if (damage <= 0.0F) {
				return 0.0F;
			} else {
				int k = EnchantmentHelper.getDamageProtection(this.getArmorSlots(), source);

				if (k > 0) {
					damage = CombatRules.getDamageAfterMagicAbsorb(damage, k);
				}

				return damage;
			}
		}
	}

	@Shadow
	public abstract boolean hasEffect(MobEffect ef);

	@Shadow
	public abstract MobEffectInstance getEffect(MobEffect ef);

	@Inject(method = "createLivingAttributes", at = @At("RETURN"))
	private static void createLivingAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		AttributeSupplier.Builder builder = cir.getReturnValue();
		//Formatter::off
		addIfExists(builder,
				Apoth.Attributes.DRAW_SPEED,
				Apoth.Attributes.CRIT_CHANCE,
				Apoth.Attributes.CRIT_DAMAGE,
				Apoth.Attributes.COLD_DAMAGE,
				Apoth.Attributes.FIRE_DAMAGE,
				Apoth.Attributes.LIFE_STEAL,
				Apoth.Attributes.PIERCING,
				Apoth.Attributes.CURRENT_HP_DAMAGE,
				Apoth.Attributes.OVERHEAL,
				Apoth.Attributes.GHOST_HEALTH,
				Apoth.Attributes.MINING_SPEED,
				Apoth.Attributes.ARROW_DAMAGE,
				Apoth.Attributes.ARROW_VELOCITY);
		//Formatter::on
	}

	private static void addIfExists(AttributeSupplier.Builder builder, Attribute... attribs) {
		for (Attribute attrib : attribs)
			if (attrib != null) builder.add(attrib);
	}

}
