package shadows.apotheosis.deadly.affix.impl.shield;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.impl.AttributeAffix;

public class ShieldDamageAffix extends AttributeAffix {

	public ShieldDamageAffix(int weight) {
		super(Attributes.ATTACK_DAMAGE, 1F, 4F, Operation.ADDITION, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.SHIELD;
	}

	@Override
	public float getMax() {
		return 4F;
	}

	@Override
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, float level) {
		return amount * 0.85F;
	}

}
