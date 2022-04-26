package shadows.apotheosis.deadly.affix.impl.ranged;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Targets more than 30 blocks away take additional damage.
 */
public class SnipeDamageAffix extends RangedAffix {

	public SnipeDamageAffix(LootRarity rarity, int min, int max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void onArrowImpact(AbstractArrow arrow, HitResult res, HitResult.Type type, float level) {
		Entity shooter = arrow.getOwner();
		if (shooter != null && type == HitResult.Type.ENTITY) {
			if (shooter.distanceToSqr(((EntityHitResult) res).getEntity()) > 30 * 30) {
				arrow.setBaseDamage(arrow.getBaseDamage() + level);
			}
		}
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		return Math.round(super.generateLevel(stack, rand, modifier));
	}
}