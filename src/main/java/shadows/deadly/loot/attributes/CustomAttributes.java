package shadows.deadly.loot.attributes;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

/**
 * Holds special attributes that are added by apoth.  These require custom handling because minecraft isn't nice.
 */
public class CustomAttributes {

	public static final IAttribute FIRE_DAMAGE = new RangedAttribute(null, "apoth.firedmg", 0.0D, 0.0D, 2048.0D);
	public static final IAttribute LUNAR_DAMAGE = new RangedAttribute(null, "apoth.lunardmg", 0.0D, 0.0D, 2048.0D);
	public static final IAttribute SOLAR_DAMAGE = new RangedAttribute(null, "apoth.solardmg", 0.0D, 0.0D, 2048.0D);
	public static final IAttribute COLD_DAMAGE = new RangedAttribute(null, "apoth.colddmg", 0.0D, 0.0D, 2048.0D);

}
