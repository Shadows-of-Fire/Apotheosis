package shadows.deadly.loot.affixes;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.storage.loot.RandomValueRange;
import shadows.deadly.gen.BossItem.EquipmentType;
import shadows.deadly.loot.AffixModifier;

public class AttributeAffix extends Affix {

	protected final IAttribute attr;
	protected final RandomValueRange range;
	protected final int op;
	protected final boolean reactive;

	public AttributeAffix(IAttribute attr, RandomValueRange range, int op, int weight) {
		super(weight);
		this.attr = attr;
		this.range = range;
		this.op = op;
		reactive = attr instanceof IReactiveAttribute;
	}

	public AttributeAffix(IAttribute attr, float min, float max, int op, int weight) {
		this(attr, new RandomValueRange(min, max), op, weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		EntityEquipmentSlot type = EquipmentType.getTypeFor(stack).getSlot(stack);
		float lvl = range.generateFloat(rand);
		AttributeModifier modif = new AttributeModifier("affix_" + attr.getName(), lvl, op);
		stack.addAttributeModifier(attr.getName(), modif, type);
		return lvl;
	}

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, float level) {
		if (reactive) ((IReactiveAttribute) attr).onEntityDamaged(user, target, level);
	}

	@Override
	public void onUserHurt(EntityLivingBase user, Entity attacker, float level) {
		if (reactive) ((IReactiveAttribute) attr).onUserHurt(user, attacker, level);
	}

	@Override
	public int getProtectionLevel(float level, DamageSource source) {
		if (reactive) return ((IReactiveAttribute) attr).getProtectionLevel(level, source);
		return super.getProtectionLevel(level, source);
	}

	@Override
	public float getExtraDamageFor(float level, EnumCreatureAttribute creatureType) {
		if (reactive) return ((IReactiveAttribute) attr).getExtraDamageFor(level, creatureType);
		return super.getExtraDamageFor(level, creatureType);
	}

	public static interface IReactiveAttribute extends IAttribute {

		default void onEntityDamaged(EntityLivingBase user, Entity target, float level) {
		}

		default void onUserHurt(EntityLivingBase user, Entity attacker, float level) {
		}

		default int getProtectionLevel(float level, DamageSource source) {
			return 0;
		}

		default float getExtraDamageFor(float level, EnumCreatureAttribute creatureType) {
			return 0;
		}
	}

}
