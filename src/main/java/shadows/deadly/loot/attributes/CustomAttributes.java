package shadows.deadly.loot.attributes;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import shadows.Apotheosis;

/**
 * Holds special attributes that are added by apoth.  These require custom handling because minecraft isn't nice.
 */
public class CustomAttributes {

	public static final DamageSource LUNAR = new DamageSource("apoth.lunar").setMagicDamage().setDamageBypassesArmor();
	public static final DamageSource SOLAR = new DamageSource("apoth.solar").setMagicDamage().setDamageBypassesArmor();
	public static final DamageSource COLD = new DamageSource("apoth.cold").setMagicDamage();

	public static final IAttribute FIRE_DAMAGE = new ElementalDmgAttribute("apoth.firedmg", (e, l) -> {
		e.setFire(3);
		e.attackEntityFrom(DamageSource.IN_FIRE, Apotheosis.localAtkStrength * l);
	});
	public static final IAttribute LUNAR_DAMAGE = new ElementalDmgAttribute("apoth.lunardmg", (e, l) -> {
		e.attackEntityFrom(LUNAR, Apotheosis.localAtkStrength * (e.world.isDaytime() ? l : 2 * l));
	});
	public static final IAttribute SOLAR_DAMAGE = new ElementalDmgAttribute("apoth.solardmg", (e, l) -> {
		e.attackEntityFrom(SOLAR, Apotheosis.localAtkStrength * (!e.world.isDaytime() ? l : 2 * l));
	});
	public static final IAttribute COLD_DAMAGE = new ElementalDmgAttribute("apoth.colddmg", (e, l) -> {
		if (e instanceof EntityLivingBase) {
			((EntityLivingBase) e).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100));
		}
		e.attackEntityFrom(COLD, Apotheosis.localAtkStrength * l);
	});
}
