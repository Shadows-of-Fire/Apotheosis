package shadows.apotheosis.deadly.affix;

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
		reg.register(new ReachDistanceAffix(5).setRegistryName("reach_distance"));
		reg.register(new EnchantabilityAffix(5).setRegistryName("enchantability"));
		registerBowAffixes(reg);
		registerSwordAffixes(reg);
		registerAxeAffixes(reg);
		registerToolAffixes(reg);
		registerArmorAffixes(reg);
		registerShieldAffixes(reg);
	}

	static void registerBowAffixes(IForgeRegistry<Affix> reg) {
		reg.register(new DrawSpeedAffix(5).setRegistryName("draw_speed"));
		reg.register(new MovementSpeedAffix(5).setRegistryName("movement_speed"));
		reg.register(new SnipeDamageAffix(3).setRegistryName("snipe_damage"));
		reg.register(new SpectralShotAffix(2).setRegistryName("spectral_shot"));
		reg.register(new SnareHitAffix(1).setRegistryName("snare_hit"));
		reg.register(new MagicArrowAffix(1).setRegistryName("magic_arrow"));
		reg.register(new TeleportDropsAffix(2).setRegistryName("teleport_drops"));
	}

	static void registerSwordAffixes(IForgeRegistry<Affix> reg) {
		reg.register(new AttackSpeedAffix(5).setRegistryName("attack_speed"));
		reg.register(new ColdDamageAffix(5).setRegistryName("cold_damage"));
		reg.register(new CritChanceAffix(2).setRegistryName("crit_chance"));
		reg.register(new CritDamageAffix(3).setRegistryName("crit_damage"));
		reg.register(new DamageChainAffix(1).setRegistryName("damage_chain"));
		reg.register(new FireDamageAffix(5).setRegistryName("fire_damage"));
		reg.register(new LifeStealAffix(3).setRegistryName("life_steal"));
		reg.register(new LootPinataAffix(2).setRegistryName("loot_pinata"));
	}

	static void registerAxeAffixes(IForgeRegistry<Affix> reg) {
		reg.register(new PiercingAffix(0).setRegistryName("piercing"));
		reg.register(new MaxCritAffix(1).setRegistryName("max_crit"));
		reg.register(new CleaveAffix(3).setRegistryName("cleave"));
		reg.register(new CurrentHPAffix(2).setRegistryName("current_hp_damage"));
		reg.register(new ExecuteAffix(5).setRegistryName("execute"));
		reg.register(new OverhealAffix(4).setRegistryName("overheal"));
	}

	static void registerToolAffixes(IForgeRegistry<Affix> reg) {
		reg.register(new TorchPlacementAffix(4).setRegistryName("torch_placement"));
		reg.register(new OmniToolAffix(2).setRegistryName("omnitool"));
		reg.register(new RadiusMiningAffix(2).setRegistryName("radius_mining"));
	}

	static void registerArmorAffixes(IForgeRegistry<Affix> reg) {
		reg.register(new ArmorAffix(5).setRegistryName("armor"));
		reg.register(new ArmorToughnessAffix(5).setRegistryName("armor_toughness"));
		reg.register(new MaxHealthAffix(5).setRegistryName("max_health"));
	}

	static void registerShieldAffixes(IForgeRegistry<Affix> reg) {
		reg.register(new ArrowCatcherAffix(1).setRegistryName("arrow_catcher"));
		reg.register(new ShieldSpeedAffix(5).setRegistryName("shield_speed"));
		reg.register(new DisengageAffix(3).setRegistryName("disengage"));
		reg.register(new SpikedAffix(2).setRegistryName("spiked_shield"));
		reg.register(new EldritchBlockAffix(1).setRegistryName("eldritch_block"));
		reg.register(new ShieldDamageAffix(3).setRegistryName("shield_damage"));
	}

}