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
import shadows.deadly.gen.BossItem.EquipmentType;
import shadows.deadly.loot.AffixModifier;
import shadows.deadly.loot.AffixModifier.Operation;
import shadows.deadly.loot.LootEntry;
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
	public static final Affix WEAK_DAMAGE = null;
	public static final Affix WEAK_ARMOR = null;
	//TODO SWIM_SPEED
	//TODO ATTACK_SPEED
	//TODO MORE_IDEAS_PLS

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
			new RangedAffix(3, 15, false, 1).setRegistryName("resistance"),
			new AttributeAffix(SharedMonsterAttributes.ATTACK_DAMAGE, 0.3F, 1.0F, 0, true, 1).setRegistryName("weak_damage"),
			new AttributeAffix(SharedMonsterAttributes.ARMOR, 0.3F, 1.0F, 0, true, 1).setRegistryName("weak_armor")
		);
		//Formatter::on
	}

	public static void init() {
		initArmor();
		initSwords();
		initShield();
		initRanged();
		initTools();
		initEpic();
		initEntries();
		initModifiers();
	}

	private static void initSwords() {
		LootManager.registerAffix(EquipmentType.SWORD, FIRE_DAMAGE);
		LootManager.registerAffix(EquipmentType.SWORD, LUNAR_DAMAGE);
		LootManager.registerAffix(EquipmentType.SWORD, SOLAR_DAMAGE);
		LootManager.registerAffix(EquipmentType.SWORD, COLD_DAMAGE);
		LootManager.registerAffix(EquipmentType.SWORD, ALWAYS_CRIT);
		LootManager.registerAffix(EquipmentType.SWORD, CRIT_DAMAGE);
		LootManager.registerWeakAffix(EquipmentType.SWORD, WEAK_DAMAGE);
	}

	private static void initArmor() {
		LootManager.registerAffix(EquipmentType.ARMOR, MOVEMENT_SPEED);
		LootManager.registerAffix(EquipmentType.ARMOR, ARMOR_TOUGHNESS);
		LootManager.registerAffix(EquipmentType.ARMOR, ARMOR);
		LootManager.registerAffix(EquipmentType.ARMOR, MAGIC_RESIST);
		LootManager.registerWeakAffix(EquipmentType.ARMOR, WEAK_ARMOR);
	}

	private static void initRanged() {
		LootManager.registerAffix(EquipmentType.BOW, MOVEMENT_SPEED);
		LootManager.registerAffix(EquipmentType.BOW, SHARPSHOOTER);
	}

	private static void initTools() {
		LootManager.registerAffix(EquipmentType.TOOL, REACH_DISTANCE);
		LootManager.registerAffix(EquipmentType.TOOL, SIFTING);
	}

	private static void initShield() {
		LootManager.registerAffix(EquipmentType.SHIELD, MOVEMENT_SPEED);
		LootManager.registerAffix(EquipmentType.SHIELD, ARMOR_TOUGHNESS);
		LootManager.registerAffix(EquipmentType.SHIELD, ARMOR);
		LootManager.registerAffix(EquipmentType.SHIELD, RESISTANCE);
	}

	private static void initEpic() {
		LootManager.registerEpicAffix(EquipmentType.SWORD, QUARTZ_FUSED);
		LootManager.registerEpicAffix(EquipmentType.ARMOR, ETERNAL);
		LootManager.registerEpicAffix(EquipmentType.TOOL, ETERNAL);
		LootManager.registerEpicAffix(EquipmentType.TOOL, FORTUNATE);
		LootManager.registerEpicAffix(EquipmentType.SHIELD, REFLECTIVE);
		LootManager.registerEpicAffix(EquipmentType.BOW, POWER);
	}

	private static void initEntries() {
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_SWORD), EquipmentType.SWORD, 3));
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.BOW), EquipmentType.BOW, 2));
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
		LootManager.registerModifier(new AffixModifier("double", Operation.MULTIPLY, 2, 6));
		LootManager.registerModifier(new AffixModifier("plus_three", Operation.ADD, 3, 2).dontEditName());
		LootManager.registerModifier(new AffixModifier("plus_one", Operation.ADD, 1, 10).dontEditName());
		LootManager.registerModifier(new AffixModifier("set_to_five", Operation.SET, 5, 1).dontEditName());
	}

}
