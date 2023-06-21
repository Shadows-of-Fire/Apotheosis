package shadows.apotheosis.core.attributeslib.api;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.RegistryObject;
import shadows.apotheosis.Apotheosis;
import shadows.placebo.util.RegObjHelper;

public class ALAttributes {

	// TODO: 1.20 Breaking Change - Re-namespace to Attributes Lib
	private static final RegObjHelper R = new RegObjHelper(Apotheosis.MODID);

	/**
	 * Bonus to how fast a ranged weapon is charged. Base Value = (1.0) = 100%
	 */
	public static final RegistryObject<Attribute> DRAW_SPEED = R.attribute("draw_speed");

	/**
	 * Chance that a non-jump-attack will critically strike.  Base value = (1.0) = 0%
	 */
	public static final RegistryObject<Attribute> CRIT_CHANCE = R.attribute("crit_chance");

	/**
	 * Amount of damage caused by critical strikes. Base value = (1.0) = 100%
	 * Not related to vanilla critical strikes.
	 */
	public static final RegistryObject<Attribute> CRIT_DAMAGE = R.attribute("crit_damage");

	/**
	 * Bonus magic damage that slows enemies hit. Base value = (0.0) = 0 damage
	 */
	public static final RegistryObject<Attribute> COLD_DAMAGE = R.attribute("cold_damage");

	/**
	 * Bonus magic damage that burns enemies hit. Base value = (0.0) = 0 damage
	 */
	public static final RegistryObject<Attribute> FIRE_DAMAGE = R.attribute("fire_damage");

	/**
	 * Percent of physical damage converted to health. Base value = (1.0) = 0%
	 */
	public static final RegistryObject<Attribute> LIFE_STEAL = R.attribute("life_steal");

	/**
	 * Percent of physical damage that bypasses armor. Base value = (1.0) = 0%
	 */
	public static final RegistryObject<Attribute> PIERCING = R.attribute("piercing");

	/**
	 * Bonus physical damage dealt equal to enemy's current health. Base value = (1.0) = 0%
	 */
	public static final RegistryObject<Attribute> CURRENT_HP_DAMAGE = R.attribute("current_hp_damage");

	/**
	 * Percent of physical damage converted to absorption hearts. Base value = (1.0) = 0%
	 */
	public static final RegistryObject<Attribute> OVERHEAL = R.attribute("overheal");

	/**
	 * Extra health that regenerates when not taking damage. Base value = (0.0) = 0 damage
	 */
	public static final RegistryObject<Attribute> GHOST_HEALTH = R.attribute("ghost_health");

	/**
	 * Mining Speed. Base value = (1.0) = 100% default break speed
	 */
	public static final RegistryObject<Attribute> MINING_SPEED = R.attribute("mining_speed");

	/**
	 * Arrow Damage. Base value = (1.0) = 100% default arrow damage
	 */
	public static final RegistryObject<Attribute> ARROW_DAMAGE = R.attribute("arrow_damage");

	/**
	 * Arrow Velocity. Base value = (1.0) = 100% default arrow velocity
	 */
	public static final RegistryObject<Attribute> ARROW_VELOCITY = R.attribute("arrow_velocity");

	/**
	 * Experience mulitplier, from killing mobs or breaking ores. Base value = (1.0) = 100% xp gained.
	 */
	public static final RegistryObject<Attribute> EXPERIENCE_GAINED = R.attribute("experience_gained");

	/**
	 * Adjusts all healing received. Base value = (1.0) = 100% xp gained.
	 */
	public static final RegistryObject<Attribute> HEALING_RECEIVED = R.attribute("healing_received");

}
