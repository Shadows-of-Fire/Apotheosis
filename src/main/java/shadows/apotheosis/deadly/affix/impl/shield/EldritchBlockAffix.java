package shadows.apotheosis.deadly.affix.impl.shield;

import java.util.Random;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
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
		if (source.getTrueSource() instanceof LivingEntity) {
			LivingEntity attacker = (LivingEntity) source.getTrueSource();
			attacker.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 200, (int) level - 1));
			if (ApotheosisObjects.SUNDERING != null) attacker.addPotionEffect(new EffectInstance(ApotheosisObjects.SUNDERING, 200, (int) level - 1));
		}
		return amount;
	}

}
