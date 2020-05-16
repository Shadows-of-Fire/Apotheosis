package shadows.apotheosis.deadly.loot.affix;

import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.loot.affix.impl.armor.ArmorAffix;
import shadows.apotheosis.deadly.loot.affix.impl.armor.ArmorToughnessAffix;
import shadows.apotheosis.deadly.loot.affix.impl.armor.MaxHealthAffix;
import shadows.apotheosis.deadly.loot.affix.impl.generic.EnchantabilityAffix;
import shadows.apotheosis.deadly.loot.affix.impl.heavy.CleaveAffix;
import shadows.apotheosis.deadly.loot.affix.impl.heavy.CurrentHPAffix;
import shadows.apotheosis.deadly.loot.affix.impl.heavy.ExecuteAffix;
import shadows.apotheosis.deadly.loot.affix.impl.heavy.MaxCritAffix;
import shadows.apotheosis.deadly.loot.affix.impl.heavy.OverhealAffix;
import shadows.apotheosis.deadly.loot.affix.impl.heavy.PiercingAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.AttackSpeedAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.ColdDamageAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.CritChanceAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.CritDamageAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.DamageChainAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.FireDamageAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.LifeStealAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.LootPinataAffix;
import shadows.apotheosis.deadly.loot.affix.impl.melee.ReachDistanceAffix;
import shadows.apotheosis.deadly.loot.affix.impl.ranged.DrawSpeedAffix;
import shadows.apotheosis.deadly.loot.affix.impl.ranged.MagicArrowAffix;
import shadows.apotheosis.deadly.loot.affix.impl.ranged.MovementSpeedAffix;
import shadows.apotheosis.deadly.loot.affix.impl.ranged.SnareHitAffix;
import shadows.apotheosis.deadly.loot.affix.impl.ranged.SnipeDamageAffix;
import shadows.apotheosis.deadly.loot.affix.impl.ranged.SpectralShotAffix;
import shadows.apotheosis.deadly.loot.affix.impl.ranged.TeleportDropsAffix;
import shadows.apotheosis.deadly.loot.affix.impl.tool.OmniToolAffix;
import shadows.apotheosis.deadly.loot.affix.impl.tool.TorchPlacementAffix;

@EventBusSubscriber(modid = Apotheosis.MODID, bus = Bus.MOD)
@ObjectHolder(Apotheosis.MODID)
public class Affixes {

	public static final Affix DRAW_SPEED = null;
	public static final Affix MOVEMENT_SPEED = null;
	public static final Affix SNIPE_DAMAGE = null;
	public static final Affix SPECTRAL_SHOT = null;
	public static final Affix SNARE_HIT = null;
	public static final Affix MAGIC_ARROW = null;
	public static final Affix TELEPORT_DROPS = null;
	public static final Affix ATTACK_SPEED = null;
	public static final Affix COLD_DAMAGE = null;
	public static final Affix CRIT_CHANCE = null;
	public static final Affix CRIT_DAMAGE = null;
	public static final Affix DAMAGE_CHAIN = null;
	public static final Affix FIRE_DAMAGE = null;
	public static final Affix LIFE_STEAL = null;
	public static final Affix LOOT_PINATA = null;
	public static final Affix REACH_DISTANCE = null;
	public static final Affix PIERCING = null;
	public static final Affix MAX_CRIT = null;
	public static final Affix CLEAVE = null;
	public static final Affix CURRENT_HP_DAMAGE = null;
	public static final Affix EXECUTE = null;
	public static final Affix OVERHEAL = null;
	//public static final Affix AUTO_SMELT = null; Requires a harvest drops event
	public static final Affix TORCH_PLACEMENT = null;
	public static final Affix OMNITOOL = null;
	//public static final Affix FOOD_SOURCE = null;
	//public static final Affix RADIUS_MINING = null;
	//public static final Affix SIFTING = null;
	public static final Affix ARMOR = null;
	public static final Affix ARMOR_TOUGHNESS = null;
	public static final Affix MAX_HEALTH = null;
	public static final Affix ENCHANTABILITY = null;

	@SubscribeEvent
	public static void register(Register<Affix> e) {
		IForgeRegistry<Affix> reg = e.getRegistry();
		reg.register(new DrawSpeedAffix(3).setRegistryName("draw_speed"));
		reg.register(new MovementSpeedAffix(3).setRegistryName("movement_speed"));
		reg.register(new SnipeDamageAffix(2).setRegistryName("snipe_damage"));
		reg.register(new SpectralShotAffix(1).setRegistryName("spectral_shot"));
		reg.register(new SnareHitAffix(2).setRegistryName("snare_hit"));
		reg.register(new MagicArrowAffix(1).setRegistryName("magic_arrow"));
		reg.register(new TeleportDropsAffix(1).setRegistryName("teleport_drops"));
		reg.register(new AttackSpeedAffix(2).setRegistryName("attack_speed"));
		reg.register(new ColdDamageAffix(3).setRegistryName("cold_damage"));
		reg.register(new CritChanceAffix(1).setRegistryName("crit_chance"));
		reg.register(new CritDamageAffix(2).setRegistryName("crit_damage"));
		reg.register(new DamageChainAffix(1).setRegistryName("damage_chain"));
		reg.register(new FireDamageAffix(3).setRegistryName("fire_damage"));
		reg.register(new LifeStealAffix(2).setRegistryName("life_steal"));
		reg.register(new LootPinataAffix(1).setRegistryName("loot_pinata"));
		reg.register(new ReachDistanceAffix(3).setRegistryName("reach_distance"));
		reg.register(new PiercingAffix(0).setRegistryName("piercing"));
		reg.register(new MaxCritAffix(1).setRegistryName("max_crit"));
		reg.register(new CleaveAffix(1).setRegistryName("cleave"));
		reg.register(new CurrentHPAffix(2).setRegistryName("current_hp_damage"));
		reg.register(new ExecuteAffix(3).setRegistryName("execute"));
		reg.register(new OverhealAffix(2).setRegistryName("overheal"));
		reg.register(new TorchPlacementAffix(4).setRegistryName("torch_placement"));
		reg.register(new OmniToolAffix(2).setRegistryName("omnitool"));
		reg.register(new ArmorAffix(3).setRegistryName("armor"));
		reg.register(new ArmorToughnessAffix(2).setRegistryName("armor_toughness"));
		reg.register(new MaxHealthAffix(1).setRegistryName("max_health"));
		reg.register(new EnchantabilityAffix(3).setRegistryName("enchantability"));
	}

}
