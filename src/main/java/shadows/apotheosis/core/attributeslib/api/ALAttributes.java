package shadows.apotheosis.core.attributeslib.api;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.RegistryObject;
import shadows.apotheosis.core.attributeslib.AttributesLib;

public class ALAttributes {

    /**
     * How fast a ranged weapon is charged. Base Value = (1.0) = 100% default draw speed.
     */
    public static final RegistryObject<Attribute> DRAW_SPEED = AttributesLib.REG_OBJS.attribute("draw_speed");

    /**
     * Chance that any attack will critically strike. Base value = (0.05) = 5% chance to critically strike.<br>
     * Not related to vanilla (jump) critical strikes.
     */
    public static final RegistryObject<Attribute> CRIT_CHANCE = AttributesLib.REG_OBJS.attribute("crit_chance");

    /**
     * Amount of damage caused by critical strikes. Base value = (1.5) = 150% normal damage dealt.<br>
     * Also impacts vanilla (jump) critical strikes.
     */
    public static final RegistryObject<Attribute> CRIT_DAMAGE = AttributesLib.REG_OBJS.attribute("crit_damage");

    /**
     * Bonus magic damage that slows enemies hit. Base value = (0.0) = 0 damage
     */
    public static final RegistryObject<Attribute> COLD_DAMAGE = AttributesLib.REG_OBJS.attribute("cold_damage");

    /**
     * Bonus magic damage that burns enemies hit. Base value = (0.0) = 0 damage
     */
    public static final RegistryObject<Attribute> FIRE_DAMAGE = AttributesLib.REG_OBJS.attribute("fire_damage");

    /**
     * Percent of physical damage converted to health. Base value = (0.0) = 0%
     */
    public static final RegistryObject<Attribute> LIFE_STEAL = AttributesLib.REG_OBJS.attribute("life_steal");

    /**
     * Bonus physical damage dealt equal to enemy's current health. Base value = (0.0) = 0%
     */
    public static final RegistryObject<Attribute> CURRENT_HP_DAMAGE = AttributesLib.REG_OBJS.attribute("current_hp_damage");

    /**
     * Percent of physical damage converted to absorption hearts. Base value = (0.0) = 0%
     */
    public static final RegistryObject<Attribute> OVERHEAL = AttributesLib.REG_OBJS.attribute("overheal");

    /**
     * Extra health that regenerates when not taking damage. Base value = (0.0) = 0 damage
     */
    public static final RegistryObject<Attribute> GHOST_HEALTH = AttributesLib.REG_OBJS.attribute("ghost_health");

    /**
     * Mining Speed. Base value = (1.0) = 100% default break speed
     */
    public static final RegistryObject<Attribute> MINING_SPEED = AttributesLib.REG_OBJS.attribute("mining_speed");

    /**
     * Arrow Damage. Base value = (1.0) = 100% default arrow damage
     */
    public static final RegistryObject<Attribute> ARROW_DAMAGE = AttributesLib.REG_OBJS.attribute("arrow_damage");

    /**
     * Arrow Velocity. Base value = (1.0) = 100% default arrow velocity
     */
    public static final RegistryObject<Attribute> ARROW_VELOCITY = AttributesLib.REG_OBJS.attribute("arrow_velocity");

    /**
     * Experience mulitplier, from killing mobs or breaking ores. Base value = (1.0) = 100% xp gained.
     */
    public static final RegistryObject<Attribute> EXPERIENCE_GAINED = AttributesLib.REG_OBJS.attribute("experience_gained");

    /**
     * Adjusts all healing received. Base value = (1.0) = 100% xp gained.
     */
    public static final RegistryObject<Attribute> HEALING_RECEIVED = AttributesLib.REG_OBJS.attribute("healing_received");

    /**
     * Flat armor penetration. Base value = (0.0) = 0 armor reduced during damage calculations.
     */
    public static final RegistryObject<Attribute> ARMOR_PIERCE = AttributesLib.REG_OBJS.attribute("armor_pierce");

    /**
     * Percentage armor reduction. Base value = (0.0) = 0% of armor reduced during damage calculations.
     */
    public static final RegistryObject<Attribute> ARMOR_SHRED = AttributesLib.REG_OBJS.attribute("armor_shred");

    /**
     * Flat protection penetration. Base value = (0.0) = 0 protection points bypassed during damage calculations.
     */
    public static final RegistryObject<Attribute> PROT_PIERCE = AttributesLib.REG_OBJS.attribute("prot_pierce");

    /**
     * Percentage protection reduction. Base value = (0.0) = 0% of protection points bypassed during damage calculations.
     */
    public static final RegistryObject<Attribute> PROT_SHRED = AttributesLib.REG_OBJS.attribute("prot_shred");

    /**
     * Chance to dodge incoming melee damage. Base value = (0.0) = 0% chance to dodge.<br>
     * "Melee" damage is considered as damage from another entity within the player's attack range.
     * <p>
     * This includes projectile attacks, as long as the projectile actually impacts the player.
     */
    public static final RegistryObject<Attribute> DODGE_CHANCE = AttributesLib.REG_OBJS.attribute("dodge_chance");
}
