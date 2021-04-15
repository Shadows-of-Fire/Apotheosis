package shadows.apotheosis.deadly.affix;

import java.io.File;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.impl.armor.ArmorAffix;
import shadows.apotheosis.deadly.affix.impl.armor.ArmorToughnessAffix;
import shadows.apotheosis.deadly.affix.impl.armor.MaxHealthAffix;
import shadows.apotheosis.deadly.affix.impl.generic.EnchantabilityAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.CleaveAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.CurrentHPAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.ExecuteAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.MaxCritAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.OverhealAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.PiercingAffix;
import shadows.apotheosis.deadly.affix.impl.melee.AttackSpeedAffix;
import shadows.apotheosis.deadly.affix.impl.melee.ColdDamageAffix;
import shadows.apotheosis.deadly.affix.impl.melee.CritChanceAffix;
import shadows.apotheosis.deadly.affix.impl.melee.CritDamageAffix;
import shadows.apotheosis.deadly.affix.impl.melee.DamageChainAffix;
import shadows.apotheosis.deadly.affix.impl.melee.FireDamageAffix;
import shadows.apotheosis.deadly.affix.impl.melee.LifeStealAffix;
import shadows.apotheosis.deadly.affix.impl.melee.LootPinataAffix;
import shadows.apotheosis.deadly.affix.impl.melee.ReachDistanceAffix;
import shadows.apotheosis.deadly.affix.impl.ranged.DrawSpeedAffix;
import shadows.apotheosis.deadly.affix.impl.ranged.MagicArrowAffix;
import shadows.apotheosis.deadly.affix.impl.ranged.MovementSpeedAffix;
import shadows.apotheosis.deadly.affix.impl.ranged.SnareHitAffix;
import shadows.apotheosis.deadly.affix.impl.ranged.SnipeDamageAffix;
import shadows.apotheosis.deadly.affix.impl.ranged.SpectralShotAffix;
import shadows.apotheosis.deadly.affix.impl.ranged.TeleportDropsAffix;
import shadows.apotheosis.deadly.affix.impl.shield.ArrowCatcherAffix;
import shadows.apotheosis.deadly.affix.impl.shield.DisengageAffix;
import shadows.apotheosis.deadly.affix.impl.shield.EldritchBlockAffix;
import shadows.apotheosis.deadly.affix.impl.shield.ShieldDamageAffix;
import shadows.apotheosis.deadly.affix.impl.shield.ShieldSpeedAffix;
import shadows.apotheosis.deadly.affix.impl.shield.SpikedAffix;
import shadows.apotheosis.deadly.affix.impl.tool.OmniToolAffix;
import shadows.apotheosis.deadly.affix.impl.tool.RadiusMiningAffix;
import shadows.apotheosis.deadly.affix.impl.tool.TorchPlacementAffix;
import shadows.placebo.config.Configuration;

@EventBusSubscriber(modid = Apotheosis.MODID, bus = Bus.MOD)
@ObjectHolder(Apotheosis.MODID)
public class Affixes {

	//Generic
	public static final Affix REACH_DISTANCE = null;
	public static final Affix ENCHANTABILITY = null;

	//Bow
	public static final Affix DRAW_SPEED = null;
	public static final Affix MOVEMENT_SPEED = null;
	public static final Affix SNIPE_DAMAGE = null;
	public static final Affix SPECTRAL_SHOT = null;
	public static final Affix SNARE_HIT = null;
	public static final Affix MAGIC_ARROW = null;
	public static final Affix TELEPORT_DROPS = null;

	//Sword
	public static final Affix ATTACK_SPEED = null;
	public static final Affix COLD_DAMAGE = null;
	public static final Affix CRIT_CHANCE = null;
	public static final Affix CRIT_DAMAGE = null;
	public static final Affix DAMAGE_CHAIN = null;
	public static final Affix FIRE_DAMAGE = null;
	public static final Affix LIFE_STEAL = null;
	public static final Affix LOOT_PINATA = null;

	//Axe
	public static final Affix PIERCING = null;
	public static final Affix MAX_CRIT = null;
	public static final Affix CLEAVE = null;
	public static final Affix CURRENT_HP_DAMAGE = null;
	public static final Affix EXECUTE = null;
	public static final Affix OVERHEAL = null;

	//Tool
	public static final Affix TORCH_PLACEMENT = null;
	public static final Affix OMNITOOL = null;
	public static final Affix RADIUS_MINING = null;

	//Armor
	public static final Affix ARMOR = null;
	public static final Affix ARMOR_TOUGHNESS = null;
	public static final Affix MAX_HEALTH = null;

	//Shield
	public static final Affix ARROW_CATCHER = null;
	public static final Affix SHIELD_SPEED = null;
	public static final Affix DISENGAGE = null;
	public static final Affix SPIKED_SHIELD = null;
	public static final Affix ELDRITCH_BLOCK = null;
	public static final Affix SHIELD_DAMAGE = null;

	@SubscribeEvent
	public static void register(Register<Affix> e) {
		IForgeRegistry<Affix> reg = e.getRegistry();
		Affix.config = new Configuration(new File(Apotheosis.configDir, "affixes.cfg"));
		register(reg, ReachDistanceAffix::new, "reach_distance", 3);
		register(reg, EnchantabilityAffix::new, "enchantability", 3);
		registerBowAffixes(reg);
		registerSwordAffixes(reg);
		registerAxeAffixes(reg);
		registerToolAffixes(reg);
		registerArmorAffixes(reg);
		registerShieldAffixes(reg);
		if (Affix.config.hasChanged()) Affix.config.save();
	}

	static void registerBowAffixes(IForgeRegistry<Affix> reg) {
		register(reg, DrawSpeedAffix::new, "draw_speed", 5);
		register(reg, MovementSpeedAffix::new, "movement_speed", 5);
		register(reg, SnipeDamageAffix::new, "snipe_damage", 3);
		register(reg, SpectralShotAffix::new, "spectral_shot", 2);
		register(reg, SnareHitAffix::new, "snare_hit", 1);
		register(reg, MagicArrowAffix::new, "magic_arrow", 1);
		register(reg, TeleportDropsAffix::new, "teleport_drops", 2);
	}

	static void registerSwordAffixes(IForgeRegistry<Affix> reg) {
		register(reg, AttackSpeedAffix::new, "attack_speed", 5);
		register(reg, ColdDamageAffix::new, "cold_damage", 5);
		register(reg, CritChanceAffix::new, "crit_chance", 2);
		register(reg, CritDamageAffix::new, "crit_damage", 3);
		register(reg, DamageChainAffix::new, "damage_chain", 1);
		register(reg, FireDamageAffix::new, "fire_damage", 5);
		register(reg, LifeStealAffix::new, "life_steal", 3);
		register(reg, LootPinataAffix::new, "loot_pinata", 2);
	}

	static void registerAxeAffixes(IForgeRegistry<Affix> reg) {
		register(reg, PiercingAffix::new, "piercing", 0);
		register(reg, MaxCritAffix::new, "max_crit", 1);
		register(reg, CleaveAffix::new, "cleave", 3);
		register(reg, CurrentHPAffix::new, "current_hp_damage", 2);
		register(reg, ExecuteAffix::new, "execute", 5);
		register(reg, OverhealAffix::new, "overheal", 4);
	}

	static void registerToolAffixes(IForgeRegistry<Affix> reg) {
		register(reg, TorchPlacementAffix::new, "torch_placement", 4);
		register(reg, OmniToolAffix::new, "omnitool", 2);
		register(reg, RadiusMiningAffix::new, "radius_mining", 2);
	}

	static void registerArmorAffixes(IForgeRegistry<Affix> reg) {
		register(reg, ArmorAffix::new, "armor", 5);
		register(reg, ArmorToughnessAffix::new, "armor_toughness", 5);
		register(reg, MaxHealthAffix::new, "max_health", 5);
	}

	static void registerShieldAffixes(IForgeRegistry<Affix> reg) {
		register(reg, ArrowCatcherAffix::new, "arrow_catcher", 1);
		register(reg, ShieldSpeedAffix::new, "shield_speed", 5);
		register(reg, DisengageAffix::new, "disengage", 3);
		register(reg, SpikedAffix::new, "spiked_shield", 2);
		register(reg, EldritchBlockAffix::new, "eldritch_block", 1);
		register(reg, ShieldDamageAffix::new, "shield_damage", 3);
	}

	static void register(IForgeRegistry<Affix> reg, Int2ObjectFunction<Affix> factory, String name, int weight) {
		weight = Affix.config.getInt("Weight", name, weight, 0, Integer.MAX_VALUE, "The weight of this affix, relative to others that may apply to the same item.");
		reg.register(factory.apply(weight).setRegistryName(name));
	}

}