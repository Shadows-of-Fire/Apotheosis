package shadows.apotheosis.deadly.affix.attributes;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
import shadows.apotheosis.Apotheosis;

/**
 * Holds special attributes that are added by apoth.  These require custom handling because minecraft isn't nice.
 */
@EventBusSubscriber(bus = Bus.MOD, modid = Apotheosis.MODID)
@ObjectHolder(Apotheosis.MODID)
public class CustomAttributes {

	public static final Attribute DRAW_SPEED = new RangedAttribute("apoth.draw_speed", 1, 0, 1024).setRegistryName(Apotheosis.MODID, "draw_speed");
	public static final Attribute SNIPE_DAMAGE = new RangedAttribute("apoth.snipe_damage", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "snipe_damage");
	public static final Attribute FIRE_DAMAGE = new RangedAttribute("apoth.fire_damage", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "fire_damage");
	public static final Attribute COLD_DAMAGE = new RangedAttribute("apoth.cold_damage", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "cold_damage");
	public static final Attribute LIFE_STEAL = new RangedAttribute("apoth.life_steal", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "life_steal");
	public static final Attribute PIERCING_DAMAGE = new RangedAttribute("apoth.piercing_damage", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "piercing_damage");
	public static final Attribute CURRENT_HP_DAMAGE = new RangedAttribute("apoth.current_hp_damage", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "current_hp_damage");
	public static final Attribute CRIT_CHANCE = new RangedAttribute("apoth.crit_chance", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "crit_chance");
	public static final Attribute CRIT_DAMAGE = new RangedAttribute("apoth.crit_damage", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "crit_damage");
	public static final Attribute OVERHEALING = new RangedAttribute("apoth.overhealing", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "overhealing");
	public static final Attribute REFLECTION = new RangedAttribute("apoth.reflection", 0, 0, 1024).setRegistryName(Apotheosis.MODID, "reflection");

	@SubscribeEvent
	public static void register(Register<Attribute> e) {
		e.getRegistry().registerAll(DRAW_SPEED, SNIPE_DAMAGE, FIRE_DAMAGE, COLD_DAMAGE, LIFE_STEAL, PIERCING_DAMAGE, CURRENT_HP_DAMAGE, CRIT_CHANCE, CRIT_DAMAGE, OVERHEALING, REFLECTION);
	}
}