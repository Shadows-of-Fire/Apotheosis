package shadows.apotheosis.deadly.affix.impl.shield;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
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
		Entity tSource = source.getTrueSource();
		if (tSource != null && tSource.getDistanceSq(entity) <= 9) {
			Vector3d look = entity.getLookVec();
			entity.setMotion(new Vector3d(1 * -look.x, 0.25, 1 * -look.z));
			entity.velocityChanged = true;
			entity.setOnGround(false);
		}
		return amount;
	}

}
