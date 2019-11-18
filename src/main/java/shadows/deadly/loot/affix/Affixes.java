package shadows.deadly.loot.affix;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import shadows.Apotheosis;
import shadows.ApotheosisObjects;
import shadows.deadly.loot.AffixModifier;
import shadows.deadly.loot.AffixModifier.Operation;
import shadows.deadly.loot.LootEntry;
import shadows.deadly.loot.LootEntry.Type;
import shadows.deadly.loot.LootManager;
import shadows.deadly.loot.affix.impl.AttributeAffix;
import shadows.deadly.loot.affix.impl.EnchantmentAffix;
import shadows.deadly.loot.affix.impl.GenericAffix;
import shadows.deadly.loot.affix.impl.RangedAffix;
import shadows.deadly.loot.affix.impl.SharpshooterAffix;
import shadows.deadly.loot.attributes.CustomAttributes;

@EventBusSubscriber(modid = Apotheosis.MODID)
@ObjectHolder(Apotheosis.MODID)
public class Affixes {

	public static final Affix FIRE_DAMAGE = null;
	public static final Affix LUNAR_DAMAGE = null;
	public static final Affix SOLAR_DAMAGE = null;
	public static final Affix COLD_DAMAGE = null;
	public static final Affix QUARTZ_FUSED = null;
	public static final Affix MOVEMENT_SPEED = null;
	public static final Affix ARMOR_TOUGHNESS = null;
	public static final Affix ENDER_SLAYING = null;
	public static final Affix ARMOR = null;
	public static final Affix ALWAYS_CRIT = null;
	public static final Affix CRIT_DAMAGE = null;
	public static final Affix SHARPSHOOTER = null;
	public static final Affix ETERNAL = null;
	public static final Affix REACH_DISTANCE = null;
	public static final Affix FORTUNATE = null;
	public static final Affix REFLECTIVE = null;
	public static final Affix POWER = null;
	public static final Affix MAGIC_RESIST = null;
	public static final Affix SIFTING = null;
	public static final Affix RESISTANCE = null;

	@SubscribeEvent
	public static void register(Register<Affix> e) {
		//Formatter::off
		e.getRegistry().registerAll(
			new AttributeAffix(CustomAttributes.FIRE_DAMAGE, 0.5F, 4.0F, 0, true, 3).setRegistryName("fire_damage"),
			new AttributeAffix(CustomAttributes.LUNAR_DAMAGE, 0.5F, 6.0F, 0, false, 1).setRegistryName("lunar_damage"),
			new AttributeAffix(CustomAttributes.SOLAR_DAMAGE, 0.5F, 6.0F, 0, false, 1).setRegistryName("solar_damage"),
			new AttributeAffix(CustomAttributes.COLD_DAMAGE, 0.5F, 4.0F, 0, true, 3).setRegistryName("cold_damage"),
			new EnchantmentAffix(Enchantments.SHARPNESS, 15, true, 2).setRegistryName("quartz_fused"),
			new AttributeAffix(SharedMonsterAttributes.MOVEMENT_SPEED, 0.05F, 0.2F, 1, true, 3).setRegistryName("movement_speed"),
			new AttributeAffix(SharedMonsterAttributes.ARMOR_TOUGHNESS, 0.2F, 1.5F, 0, true, 1).setRegistryName("armor_toughness"),
			new AttributeAffix(CustomAttributes.VORPAL_DAMAGE, 1.0F, 5.0F, 0, true, 2).setRegistryName("ender_slaying"),
			new AttributeAffix(SharedMonsterAttributes.ARMOR, 1.0F, 3.0F, 0, true, 3).setRegistryName("armor"),
			new GenericAffix(true, 1).setRegistryName("always_crit"),
			new AttributeAffix(CustomAttributes.CRIT_DAMAGE, 0.1F, 1.0F, 1, false, 2).setRegistryName("crit_damage"),
			new SharpshooterAffix(1).setRegistryName("sharpshooter"),
			new EnchantmentAffix(Enchantments.UNBREAKING, 15, true, 1).setRegistryName("eternal"),
			new AttributeAffix(EntityPlayer.REACH_DISTANCE, 1.0F, 3.0F, 0, false, 2).setRegistryName("reach_distance"),
			new EnchantmentAffix(Enchantments.FORTUNE, 10, false, 1).setRegistryName("fortunate"),
			new EnchantmentAffix(() -> ApotheosisObjects.REFLECTIVE, 8, false, 2).setRegistryName("reflective"),
			new EnchantmentAffix(Enchantments.POWER, 10, true, 2).setRegistryName("power"),
			new AttributeAffix(CustomAttributes.MAGIC_RESIST, 1, 5, 0, false, 1).setRegistryName("magic_resist"),
			new RangedAffix(0.0001F, 0.001F, true, 1).setRegistryName("sifting"),
			new RangedAffix(3, 15, false, 1).setRegistryName("resistance")
		);
		//Formatter::on
	}

	public static void init() {
		initArmor();
		initWeapons();
		initShield();
		initRanged();
		initTools();
		initEpic();
		initEntries();
		initModifiers();
	}

	private static void initWeapons() {
		LootManager.registerAffix(Type.WEAPON, FIRE_DAMAGE);
		LootManager.registerAffix(Type.WEAPON, LUNAR_DAMAGE);
		LootManager.registerAffix(Type.WEAPON, SOLAR_DAMAGE);
		LootManager.registerAffix(Type.WEAPON, COLD_DAMAGE);
		LootManager.registerAffix(Type.WEAPON, ALWAYS_CRIT);
		LootManager.registerAffix(Type.WEAPON, CRIT_DAMAGE);
	}

	private static void initArmor() {
		LootManager.registerAffix(Type.ARMOR, MOVEMENT_SPEED);
		LootManager.registerAffix(Type.ARMOR, ARMOR_TOUGHNESS);
		LootManager.registerAffix(Type.ARMOR, ARMOR);
		LootManager.registerAffix(Type.ARMOR, MAGIC_RESIST);
	}

	private static void initRanged() {
		LootManager.registerAffix(Type.RANGED, MOVEMENT_SPEED);
		LootManager.registerAffix(Type.RANGED, SHARPSHOOTER);
	}

	private static void initTools() {
		LootManager.registerAffix(Type.TOOL, REACH_DISTANCE);
		LootManager.registerAffix(Type.TOOL, SIFTING);
	}

	private static void initShield() {
		LootManager.registerAffix(Type.SHIELD, MOVEMENT_SPEED);
		LootManager.registerAffix(Type.SHIELD, ARMOR_TOUGHNESS);
		LootManager.registerAffix(Type.SHIELD, ARMOR);
		LootManager.registerAffix(Type.SHIELD, RESISTANCE);
	}

	private static void initEpic() {
		LootManager.registerEpicAffix(Type.WEAPON, QUARTZ_FUSED);
		LootManager.registerEpicAffix(Type.ARMOR, ETERNAL);
		LootManager.registerEpicAffix(Type.TOOL, ETERNAL);
		LootManager.registerEpicAffix(Type.TOOL, FORTUNATE);
		LootManager.registerEpicAffix(Type.SHIELD, REFLECTIVE);
		LootManager.registerEpicAffix(Type.RANGED, POWER);
	}

	private static void initEntries() {
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_SWORD), Type.WEAPON, 3));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.BOW), Type.RANGED, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.SHIELD), Type.SHIELD, 1));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_HELMET), Type.ARMOR, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_CHESTPLATE), Type.ARMOR, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_LEGGINGS), Type.ARMOR, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_BOOTS), Type.ARMOR, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_SHOVEL), Type.TOOL, 1));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_AXE), Type.TOOL, 2));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_PICKAXE), Type.TOOL, 2));
	}

	private static void initModifiers() {
		LootManager.registerModifier(new AffixModifier("double", Operation.MULTIPLY, 2, 3));
		LootManager.registerModifier(new AffixModifier("plus_three", Operation.ADD, 3, 1));
		LootManager.registerModifier(new AffixModifier("plus_one", Operation.ADD, 1, 5).dontEditName());
	}

}
