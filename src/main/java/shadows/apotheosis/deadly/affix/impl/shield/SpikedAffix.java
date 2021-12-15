package shadows.apotheosis.deadly.affix.impl.shield;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

public class SpikedAffix extends AttributeAffix {

	public SpikedAffix(int weight) {
		super(CustomAttributes.REFLECTION, 0.4F, 1.0F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SHIELD;
	}

	@Override
	public float getMax() {
		return 1.5F;
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
