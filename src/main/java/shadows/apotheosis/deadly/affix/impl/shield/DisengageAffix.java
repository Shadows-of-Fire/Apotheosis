package shadows.apotheosis.deadly.affix.impl.shield;

import java.util.Random;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

public class DisengageAffix extends Affix {

	public DisengageAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, AffixModifier modifier) {
		return 1;
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
		return 1;
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return 1;
	}

	@Override
	public float obliterateLevel(float level) {
		return 1;
	}

	@Override
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, float level) {
		Entity tSource = source.getEntity();
		if (tSource != null && tSource.distanceToSqr(entity) <= 9) {
			Vec3 look = entity.getLookAngle();
			entity.setDeltaMovement(new Vec3(1 * -look.x, 0.25, 1 * -look.z));
			entity.hurtMarked = true;
			entity.setOnGround(false);
		}
		return amount;
	}

}
