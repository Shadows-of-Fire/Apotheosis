package shadows.deadly.loot.attributes;

import java.util.function.BiConsumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import shadows.deadly.loot.affix.impl.AttributeAffix.IReactiveAttribute;

public class ElementalDmgAttribute extends RangedAttribute implements IReactiveAttribute {

	final BiConsumer<Entity, Float> attackFunc;

	public ElementalDmgAttribute(String unlocalizedNameIn, BiConsumer<Entity, Float> attackFunc) {
		super(null, unlocalizedNameIn, 0, 0, Double.MAX_VALUE);
		this.attackFunc = attackFunc;
	}

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, float level) {
		if (target != null) {
			attackFunc.accept(target, level);
		}
	}

}
