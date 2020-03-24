package shadows.apotheosis.deadly.loot.affix;

import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import shadows.apotheosis.Apotheosis;
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

/**
 * Current Affix Counts (Prefix/Suffix)
 * Weapons: x/x
 * Ranged: x/x
 * Tools: x/x
 * Armor: x/x
 * Shields: x/x
 */
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
	}

	public static void init() {
	}

	/*
	private static void initEntries() {
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_SWORD), EquipmentType.SWORD, 3));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.BOW), EquipmentType.BOW, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.CROSSBOW), EquipmentType.BOW, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.SHIELD), EquipmentType.SHIELD, 1));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_HELMET), EquipmentType.ARMOR, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_CHESTPLATE), EquipmentType.ARMOR, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_LEGGINGS), EquipmentType.ARMOR, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_BOOTS), EquipmentType.ARMOR, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_SHOVEL), EquipmentType.TOOL, 1));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_AXE), EquipmentType.TOOL, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_PICKAXE), EquipmentType.TOOL, 2));
	}

	private static void initModifiers() {
		LootManager.registerModifier(new AffixModifier("double", AffixOp.MULTIPLY, 2, 6));
		LootManager.registerModifier(new AffixModifier("plus_three", AffixOp.ADD, 3, 2).dontEditName());
		LootManager.registerModifier(new AffixModifier("plus_one", AffixOp.ADD, 1, 10).dontEditName());
		LootManager.registerModifier(new AffixModifier("set_to_five", AffixOp.SET, 5, 1).dontEditName());
	}

	*/
}
