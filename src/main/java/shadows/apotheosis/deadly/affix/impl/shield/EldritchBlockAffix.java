package shadows.apotheosis.deadly.affix.impl.shield;

import java.util.Random;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Applies Weakness/Sundering to the attacker.
 */
public class EldritchBlockAffix extends Affix {

	public EldritchBlockAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, AffixModifier modifier) {
		int lvl = 1 + rand.nextInt(2);
		if (modifier != null) lvl = (int) modifier.editLevel(this, lvl);
		return lvl;
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SHIELD;
	}

	@Override
	public float getMin() {
		return 1;
	}

	@Override
	public float getMax() {
		return 3;
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return (int) super.upgradeLevel(curLvl, newLvl);
	}

	@Override
	public float obliterateLevel(float level) {
		return (int) super.obliterateLevel(level);
	}

	@Override
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, float level) {
		if (source.getEntity() instanceof LivingEntity) {
			LivingEntity attacker = (LivingEntity) source.getEntity();
			attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, (int) level - 1));
			if (ApotheosisObjects.SUNDERING != null) attacker.addEffect(new MobEffectInstance(ApotheosisObjects.SUNDERING, 200, (int) level - 1));
		}
		return amount;
	}

}
