package shadows.apotheosis.adventure.affix.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

/**
 * Disengage
 */
public class RetreatingAffix extends Affix {

	public RetreatingAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.SHIELD && rarity.isAtLeast(LootRarity.EPIC);
	}

	@Override
	public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
		Entity tSource = source.getEntity();
		if (tSource != null && tSource.distanceToSqr(entity) <= 9) {
			Vec3 look = entity.getLookAngle();
			entity.setDeltaMovement(new Vec3(1 * -look.x, 0.25, 1 * -look.z));
			entity.hurtMarked = true;
			entity.setOnGround(false);
		}
		return super.onShieldBlock(stack, rarity, level, entity, source, amount);
	}

}
