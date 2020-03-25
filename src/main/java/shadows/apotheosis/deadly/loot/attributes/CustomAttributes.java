package shadows.apotheosis.deadly.loot.attributes;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

/**
 * Holds special attributes that are added by apoth.  These require custom handling because minecraft isn't nice.
 */
public class CustomAttributes {

	public static final IAttribute DRAW_SPEED = new Attribute("apoth.draw_speed", 1, 0, 1024);
	public static final IAttribute SNIPE_DAMAGE = new Attribute("apoth.snipe_damage", 0, 0, 1024);
	public static final IAttribute FIRE_DAMAGE = new Attribute("apoth.fire_damage", 0, 0, 1024);
	public static final IAttribute COLD_DAMAGE = new Attribute("apoth.cold_damage", 0, 0, 1024);
	public static final IAttribute LIFE_STEAL = new Attribute("apoth.life_steal", 0, 0, 1024);
	public static final IAttribute PIERCING_DAMAGE = new Attribute("apoth.piercing_damage", 0, 0, 1024);
	public static final IAttribute CURRENT_HP_DAMAGE = new Attribute("apoth.current_hp_damage", 0, 0, 1024);
	public static final IAttribute CRIT_CHANCE = new Attribute("apoth.crit_chance", 0, 0, 1024);
	public static final IAttribute CRIT_DAMAGE = new Attribute("apoth.crit_damage", 0, 0, 1024);
	public static final IAttribute OVERHEALING = new Attribute("apoth.overhealing", 0, 0, 1024);

	public static class Attribute extends RangedAttribute {

		public Attribute(String key, double baseValue, double minValue, double maxValue) {
			super(null, key, baseValue, minValue, maxValue);
		}

	}
}
