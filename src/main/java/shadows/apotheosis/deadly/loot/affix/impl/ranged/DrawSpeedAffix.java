package shadows.apotheosis.deadly.loot.affix.impl.ranged;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.impl.AttributeAffix;
import shadows.apotheosis.deadly.loot.attributes.CustomAttributes;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;

/**
 * Decreases how long it takes to fully charge a bow.
 */
public class DrawSpeedAffix extends AttributeAffix {

	private static final float[] values = { 0.1F, 0.2F, 0.25F, 0.33F, 0.5F, 1.0F, 1.1F, 1.2F, 1.25F, 1.33F, 1.5F };

	public DrawSpeedAffix(int weight) {
		super(CustomAttributes.DRAW_SPEED, 0.1F, 1.5F, Operation.MULTIPLY_TOTAL, weight);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.RANGED;
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		EquipmentSlotType type = EquipmentType.getTypeFor(stack).getSlot(stack);
		float lvl = values[rand.nextInt(values.length)];
		AttributeModifier modif = new AttributeModifier(this.getRegistryName() + "_" + attr.get().getRegistryName(), lvl, op);
		stack.addAttributeModifier(attr.get(), modif, type);
		return lvl;
	}

}