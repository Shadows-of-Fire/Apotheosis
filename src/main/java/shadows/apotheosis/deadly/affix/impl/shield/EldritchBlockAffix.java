package shadows.apotheosis.deadly.affix.impl.shield;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Applies Weakness/Sundering to the attacker.
 */
public class EldritchBlockAffix extends Affix {

	@Override
	public boolean isPrefix() {
		return true;
	}

	public EldritchBlockAffix(LootRarity rarity, int weight) {
		super(rarity, weight);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory == LootCategory.SHIELD;
	}

	@Override
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, float level) {
		if (source.getEntity() instanceof LivingEntity attacker) {
			attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
			if (Apoth.Effects.SUNDERING != null) attacker.addEffect(new MobEffectInstance(Apoth.Effects.SUNDERING, 200, 1));
		}
		return amount;
	}

}
