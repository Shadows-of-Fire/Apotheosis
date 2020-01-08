package shadows.apotheosis.deadly.loot.affix.impl;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.storage.loot.RandomValueRange;
import shadows.apotheosis.deadly.gen.BossItem.EquipmentType;
import shadows.apotheosis.deadly.loot.AffixModifier;
import shadows.apotheosis.deadly.loot.affix.Affix;

public class AttributeAffix extends Affix {

	protected final IAttribute attr;
	protected final RandomValueRange range;
	protected final Operation op;
	protected final boolean reactive;

	public AttributeAffix(IAttribute attr, RandomValueRange range, Operation op, boolean prefix, int weight) {
		super(prefix, weight);
		this.attr = attr;
		this.range = range;
		this.op = op;
		reactive = attr instanceof IReactiveAttribute;
	}

	public AttributeAffix(IAttribute attr, float min, float max, Operation op, boolean prefix, int weight) {
		this(attr, new RandomValueRange(min, max), op, prefix, weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		EquipmentSlotType type = EquipmentType.getTypeFor(stack).getSlot(stack);
		float lvl = range.generateFloat(rand);
		if (modifier != null) lvl = modifier.editLevel(lvl);
		AttributeModifier modif = new AttributeModifier("affix_" + attr.getName(), lvl, op);
		stack.addAttributeModifier(attr.getName(), modif, type);
		return lvl;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (reactive) ((IReactiveAttribute) attr).onEntityDamaged(user, target, level);
	}

	@Override
	public void onUserHurt(LivingEntity user, Entity attacker, float level) {
		if (reactive) ((IReactiveAttribute) attr).onUserHurt(user, attacker, level);
	}

	@Override
	public int getProtectionLevel(float level, DamageSource source) {
		if (reactive) return ((IReactiveAttribute) attr).getProtectionLevel(level, source);
		return super.getProtectionLevel(level, source);
	}

	@Override
	public float getExtraDamageFor(float level, CreatureAttribute creatureType) {
		if (reactive) return ((IReactiveAttribute) attr).getExtraDamageFor(level, creatureType);
		return super.getExtraDamageFor(level, creatureType);
	}

	public static interface IReactiveAttribute extends IAttribute {

		default void onEntityDamaged(LivingEntity user, Entity target, float level) {
		}

		default void onUserHurt(LivingEntity user, Entity attacker, float level) {
		}

		default int getProtectionLevel(float level, DamageSource source) {
			return 0;
		}

		default float getExtraDamageFor(float level, CreatureAttribute creatureType) {
			return 0;
		}
	}

}
