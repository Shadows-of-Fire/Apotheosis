package shadows.apotheosis.deadly.affix.impl.shield;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

public class SpikedAffix extends RangedAffix {

	public SpikedAffix(LootRarity rarity, float min, float max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public boolean canApply(LootCategory type) { return type == LootCategory.SHIELD; }

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, float level) {
		Entity tSource = source.getEntity();
		if (tSource != null && tSource.distanceToSqr(entity) <= 9) {
			tSource.hurt(causeSpikeDamage(entity), level * amount);
		}
		return super.onShieldBlock(entity, stack, source, amount, level);
	}

	public static DamageSource causeSpikeDamage(Entity source) {
		return new EntityDamageSource("apoth_spiked", source).setThorns().setMagic();
	}
}
