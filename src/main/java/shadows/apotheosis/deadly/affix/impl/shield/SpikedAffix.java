package shadows.apotheosis.deadly.affix.impl.shield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
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
		Entity tSource = source.getTrueSource();
		if (tSource != null && tSource.getDistanceSq(entity) <= 9) {
			tSource.attackEntityFrom(causeSpikeDamage(entity), level * amount);
		}
		return super.onShieldBlock(entity, stack, source, amount, level);
	}

	public static DamageSource causeSpikeDamage(Entity source) {
		return new EntityDamageSource("apoth_spiked", source).setIsThornsDamage().setMagicDamage();
	}

}
