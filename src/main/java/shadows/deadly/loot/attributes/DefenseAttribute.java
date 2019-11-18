package shadows.deadly.loot.attributes;

import java.util.function.BiFunction;

import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.DamageSource;
import shadows.deadly.loot.affix.impl.AttributeAffix.IReactiveAttribute;

public class DefenseAttribute extends RangedAttribute implements IReactiveAttribute {

	final BiFunction<Float, DamageSource, Integer> defenseFunc;

	public DefenseAttribute(String unlocalizedNameIn, BiFunction<Float, DamageSource, Integer> defenseFunc) {
		super(null, unlocalizedNameIn, 0, 0, Double.MAX_VALUE);
		this.defenseFunc = defenseFunc;
	}

	@Override
	public int getProtectionLevel(float level, DamageSource source) {
		return defenseFunc.apply(level, source);
	}

}
