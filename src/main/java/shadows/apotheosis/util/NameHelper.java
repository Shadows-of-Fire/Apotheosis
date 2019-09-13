package shadows.apotheosis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TieredItem;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

/**
 * This class is functional.  It's a mess tho.
 * @author FatherToast
 *
 */
@SuppressWarnings("deprecation")
public class NameHelper {
	/// List of all possible full names.
	public static String[] names = { "Albert", "Andrew", "Anderson", "Andy", "Allan", "Arthur", "Aaron", "Allison", "Arielle", "Amanda", "Anne", "Annie", "Amy", "Alana", "Brandon", "Brady", "Bernard", "Ben", "Benjamin", "Bob", "Bobette", "Brooke", "Brandy", "Beatrice", "Bea", "Bella", "Becky", "Carlton", "Carl", "Calvin", "Cameron", "Carson", "Chase", "Cassandra", "Cassie", "Cas", "Carol", "Carly", "Cherise", "Charlotte", "Cheryl", "Chasity", "Danny", "Drake", "Daniel", "Derrel", "David", "Dave", "Donovan", "Don", "Donald", "Drew", "Derrick", "Darla", "Donna", "Dora", "Danielle", "Edward", "Elliot", "Ed", "Edson", "Elton", "Eddison", "Earl", "Eric", "Ericson", "Eddie", "Ediovany", "Emma", "Elizabeth", "Eliza", "Esperanza", "Esper", "Esmeralda", "Emi", "Emily", "Elaine", "Fernando", "Ferdinand", "Fred", "Feddie", "Fredward", "Frank", "Franklin", "Felix", "Felicia", "Fran", "Greg", "Gregory", "George", "Gerald", "Gina", "Geraldine", "Gabby", "Hendrix", "Henry", "Hobbes", "Herbert", "Heath", "Henderson", "Helga", "Hera", "Helen", "Helena", "Hannah", "Ike", "Issac", "Israel", "Ismael", "Irlanda", "Isabelle", "Irene", "Irenia", "Jimmy", "Jim", "Justin", "Jacob", "Jake", "Jon", "Johnson", "Jonny", "Jonathan", "Josh", "Joshua", "Julian", "Jesus", "Jericho", "Jeb", "Jess", "Joan", "Jill", "Jillian", "Jessica", "Jennifer", "Jenny", "Jen", "Judy", "Kenneth", "Kenny", "Ken", "Keith", "Kevin", "Karen", "Kassandra", "Kassie", "Leonard", "Leo", "Leroy", "Lee", "Lenny", "Luke", "Lucas", "Liam", "Lorraine", "Latasha", "Lauren", "Laquisha", "Livia", "Lydia", "Lila", "Lilly", "Lillian", "Lilith", "Lana", "Mason", "Mike", "Mickey", "Mario", "Manny", "Mark", "Marcus", "Martin", "Marty", "Matthew", "Matt", "Max", "Maximillian", "Marth", "Mia", "Marriah", "Maddison", "Maddie", "Marissa", "Miranda", "Mary", "Martha", "Melonie", "Melody", "Mel", "Minnie", "Nathan", "Nathaniel", "Nate", "Ned", "Nick", "Norman", "Nicholas", "Natasha", "Nicki", "Nora", "Nelly", "Nina", "Orville", "Oliver", "Orlando", "Owen", "Olsen", "Odin", "Olaf", "Ortega", "Olivia", "Patrick", "Pat", "Paul", "Perry", "Pinnochio", "Patrice", "Patricia", "Pennie", "Petunia", "Patti", "Pernelle", "Quade", "Quincy", "Quentin", "Quinn", "Roberto", "Robbie", "Rob", "Robert", "Roy", "Roland", "Ronald", "Richard", "Rick", "Ricky", "Rose", "Rosa", "Rhonda", "Rebecca", "Roberta", "Sparky", "Shiloh", "Stephen", "Steve", "Saul", "Sheen", "Shane", "Sean", "Sampson", "Samuel", "Sammy", "Stefan", "Sasha", "Sam", "Susan", "Suzy", "Shelby", "Samantha", "Sheila", "Sharon", "Sally", "Stephanie", "Sandra", "Sandy", "Sage", "Tim", "Thomas", "Thompson", "Tyson", "Tyler", "Tom", "Tyrone", "Timmothy", "Tamara", "Tabby", "Tabitha", "Tessa", "Tiara", "Tyra", "Uriel", "Ursala", "Uma", "Victor", "Vincent", "Vince", "Vance", "Vinny", "Velma", "Victoria", "Veronica", "Wilson", "Wally", "Wallace", "Will", "Wilard", "William", "Wilhelm", "Xavier", "Xandra", "Young", "Yvonne", "Yolanda", "Zach", "Zachary" };
	/// List of all name parts.
	public static String[] nameParts = { "Grab", "Thar", "Ger", "Ald", "Mas", "On", "O", "Din", "Thor", "Jon", "Ath", "Burb", "En", "A", "E", "I", "U", "Hab", "Bloo", "Ena", "Dit", "Aph", "Ern", "Bor", "Dav", "Id", "Toast", "Son", "Dottir", "For", "Wen", "Lob", "Ed", "Die", "Van", "Y", "Zap", "Ear", "Ben", "Don", "Bran", "Gro", "Jen", "Bob", "Ette", "Ere", "Man", "Qua", "Bro", "Cree", "Per", "Skel", "Ton", "Zom", "Bie", "Wolf", "End", "Er", "Pig", "Sil", "Ver", "Fish", "Cow", "Chic", "Ken", "Sheep", "Squid", "Hell" };
	/// List of salutations.
	public static String[] salutations = { "Sir", "Mister", "Madam", "Doctor", "Father", "Mother" };
	/// List of all mob descriptors.
	public static String[] descriptors = { "Mighty", "Supreme", "Superior", "Ultimate", "Lame", "Wimpy", "Curious", "Sneaky", "Pathetic", "Crying", "Eagle", "Errant", "Unholy", "Questionable", "Mean", "Hungry", "Thirsty", "Feeble", "Wise", "Sage", "Magical", "Mythical", "Legendary", "Not Very Nice", "Jerk", "Doctor", "Misunderstood", "Angry", "Knight", "Bishop", "Godly", "Special", "Toasty", "Shiny", "Shimmering", "Light", "Dark", "Odd-Smelling", "Funky", "Rock Smasher", "Son of Herobrine", "Cracked", "Sticky", "\u00a7kAlien\u00a7r", "Baby", "Manly", "Rough", "Scary", "Undoubtable", "Honest", "Non-Suspicious", "Boring", "Odd", "Lazy", "Super", "Nifty", "Ogre Slayer", "Pig Thief", "Dirt Digger", "Really Cool", "Doominator", "... Something" };
	/// 3D array of all enchantment prefixes and postfixes. (enchantment id, pre or post, values)
	public static Map<Enchantment, String[][]> modifiers = new HashMap<>();
	static {
		modifiers.put(Enchantments.PROTECTION, new String[][] { { "Protective", "Shielding", "Fortified", "Tough", "Sturdy", "Defensive" }, { "Resistance", "Protection", "Shielding", "Fortitude", "Toughness", "Sturdiness", "Defense" } });
		modifiers.put(Enchantments.FIRE_PROTECTION, new String[][] { { "Flame-Resistant", "Flameproof", "Fire-Resistant", "Fireproof", "Cold", "Frigid" }, { "Flame Resistance", "Flame", "Fire Resistance", "Fire", "Coldness", "Ice" } });
		modifiers.put(Enchantments.FEATHER_FALLING, new String[][] { { "Feather", "Feathered", "Mercury", "Hermes", "Winged", "Lightweight", "Soft", "Cushioned" }, { "Feather Falling", "Feathers", "Mercury", "Hermes", "Wings", "Gravity", "Softness", "Cushioning" } });
		modifiers.put(Enchantments.BLAST_PROTECTION, new String[][] { { "Blast-Resistant", "Creeperproof", "Anti-Creeper", "Bomb", "Explosion-Damping", "Bombproof" }, { "Blast Resistance", "Creeper Hugging", "Creeper Slaying", "Bomb Repelling", "Explosion Damping", "Bomb Resistance" } });
		modifiers.put(Enchantments.PROJECTILE_PROTECTION, new String[][] { { "Arrow-Blocking", "Skeletonproof", "Anti-Skeleton", "Arrow-Breaking", "Arrowproof" }, { "Arrow Blocking", "Skeleton Hugging", "Skeleton Slaying", "Arrow Resistance", "Arrow Defense" } });
		modifiers.put(Enchantments.RESPIRATION, new String[][] { { "Waterbreathing", "Dive", "Diving", "Water", "Scuba", "Fishy", "Underwater", "Deep-sea", "Submarine" }, { "Waterbreathing", "Diving", "Deep-Sea Diving", "Water", "Swimming", "Fishiness", "Underwater Exploration", "Deep-sea Exploration", "Submersion" } });
		modifiers.put(Enchantments.AQUA_AFFINITY, new String[][] { { "Aquatic", "Watery", "Wet", "Deep-Sea Mining", "Fish", "Fishy" }, { "Aquatic Mining", "Water", "Wetness", "Deep-Sea Mining", "Fish" } });
		modifiers.put(Enchantments.THORNS, new String[][] { { "Thorned", "Spiked", "Angry", "Vengeful", "Retaliating", "Splintering", "Harmful", "Painful", "Spiny", "Pointy", "Sharp" }, { "Thorns", "Spikes", "Anger", "Vengeance", "Retaliation", "Splinters", "Harm", "Pain", "Spines", "Pointiness", "Sharpness" } });
		modifiers.put(Enchantments.SHARPNESS, new String[][] { { "Sharp", "Razor Sharp", "Pointy", "Razor-Edged", "Serrated", "Painful", "Smart" }, { "Sharpness", "Razor Sharpness", "Pointiness", "Pain", "Smarting" } });
		modifiers.put(Enchantments.SMITE, new String[][] { { "Smiting", "Holy", "Banishing", "Burying", "Purging", "Cleansing", "Wrathful", "Zombie-Slaying", "Skeleton-Slaying", "Undead-Slaying" }, { "Smiting", "Holiness", "Banishing", "Burying", "Purging", "Cleansing", "Wrath", "Zombie Slaying", "Skeleton Slaying", "Undead Slaying" } });
		modifiers.put(Enchantments.BANE_OF_ARTHROPODS, new String[][] { { "Spider-Slaying", "Bug-Crushing", "Flyswatting", "Bugbane", "Arachnophobic", "Spiderbane" }, { "Spider Slaying", "Bug Crushing", "Flyswatting", "Bugbane", "Arachnophobia", "Spiderbane" } });
		modifiers.put(Enchantments.KNOCKBACK, new String[][] { { "Forceful", "Heavy", "Dull", "Powerful", "Pushing", "Launching", "Furious", "Charging", "Ram's" }, { "Forcefulness", "Knockback", "Dullness", "Power", "Pushing", "Launching", "Fury", "Charging", "The Ram" } });
		modifiers.put(Enchantments.FIRE_ASPECT, new String[][] { { "Fiery", "Fiery Dragon", "Fire", "Burning", "Hot", "Volcanic", "Lava", "Dragon", "Tree-Slaying" }, { "Fire", "The Fire Dragon", "Flame", "Burning", "Heat", "Volcanoes", "Lava", "The Dragon", "Tree Slaying" } });
		modifiers.put(Enchantments.LOOTING, new String[][] { { "Looting", "Lucky", "Fortunate", "Greedy", "Grubby", "Thievish", "Thieving" }, { "Looting", "Luck", "Fortune", "Greed", "Grubbiness", "Thievishness", "Thieving" } });
		modifiers.put(Enchantments.EFFICIENCY, new String[][] { { "Efficient", "Quick", "Fast", "Speedy", "Quick-Mining", "Rushing" }, { "Efficiency", "Quickness", "Fastness", "Speed", "Quick-Mining", "Rushing" } });
		modifiers.put(Enchantments.SILK_TOUCH, new String[][] { { "Careful", "Delicate", "Gentle", "Courteous", "Polite", "Ice-Harvesting", "Glass-Removing" }, { "Carefulness", "Delicate Mining", "Gentleness", "Courtesy", "Politeness", "Ice Harvesting", "Glass Removing" } });
		modifiers.put(Enchantments.UNBREAKING, new String[][] { { "Unbreaking", "Reliable", "Trusty", "Flexible", "Unbreakable", "Timeless", "Quality", "Made-Like-They-Used-To-Make-Them" }, { "Unbreaking", "Reliabitlity", "Trustiness", "Flexibility", "Unbreakability", "Timelessness", "Quality" } });
		modifiers.put(Enchantments.FORTUNE, new String[][] { { "Fortunate", "Lucky", "Greedy", "Effective", "Collector's", "Flint-Finding", "Resourceful" }, { "Fortune", "Luck", "Greed", "Effectiveness", "Collecting", "Flint Finding", "Resourcefulness" } });
		modifiers.put(Enchantments.POWER, new String[][] { { "Powerful", "Heart-Seeking", "Head-Seeking", "Killer", "Sniper", "Efficient", "Arrow-Saving", "Ogre-Slaying" }, { "Power", "Heart Seeking", "Head Seeking", "Killing", "Sniping", "Efficiency", "Arrow Saving", "Ogre Slaying" } });
		modifiers.put(Enchantments.PUNCH, new String[][] { { "Forceful", "Heavy", "Self-Defense", "Crushing", "Smashing" }, { "Force", "Heavy Arrows", "Self-Defense", "Crushing", "Smashing" } });
		modifiers.put(Enchantments.FLAME, new String[][] { { "Fiery", "Fiery Dragon", "Fire", "Burning", "Hot", "Volcanic", "Lava", "Dragon", "Fire-Arrow", "Tree-Slaying" }, { "Fire", "The Fire Dragon", "Flame", "Burning", "Heat", "Volcanoes", "Lava", "The Dragon", "Flaming Arrows", "Tree Slaying" } });
		modifiers.put(Enchantments.INFINITY, new String[][] { { "Efficient", "Infinite", "Arrow-Making", "Arrow-Saving", "Boomerang", "Magic Arrow" }, { "Efficiency", "Infinity", "Arrow Making", "Arrow Saving", "Boomerang Arrows", "Magic Arrow Creation" } });
	}

	/// Returns a mash name.
	public static String buildName(Random random) {
		String name = NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)] + NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)].toLowerCase();
		if (random.nextInt(2) == 0) {
			name += NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)].toLowerCase();
		}
		return name;
	}

	/// Applies a random name to a mob and returns the root name (to be passed to the item name method).
	public static String setEntityName(Random random, LivingEntity entity) {
		String root = random.nextInt(2) == 0 ? NameHelper.names[random.nextInt(NameHelper.names.length)] : NameHelper.buildName(random);
		String name = root;
		if (random.nextInt(5) == 0) {
			name = NameHelper.salutations[random.nextInt(NameHelper.salutations.length)] + " " + name;
		}
		if (random.nextInt(2) == 0) {
			name += " ";
			if (random.nextInt(10) == 0) {
				if (random.nextInt(2) == 0) {
					name += "Mac";
				} else {
					name += "Mc";
				}
			}
			name += NameHelper.buildName(random);
		} else {
			name += " the " + NameHelper.descriptors[random.nextInt(NameHelper.descriptors.length)];
		}
		entity.setCustomName(new StringTextComponent(name));
		return root;
	}

	/// Sets the item's name based on what it is, the owner's name, and its main feature.
	public static void setItemName(Random random, ItemStack itemStack, String name, Enchantment enchantment) {
		name += "'s ";

		boolean prefixed = false;
		if (random.nextInt(5) == 0) {
			prefixed = true;
		} else if (random.nextInt(2) == 0) {
			prefixed = true;
			if (NameHelper.modifiers.get(enchantment) != null) {
				String[] temp = modifiers.get(enchantment)[0];
				name += temp[random.nextInt(temp.length)] + " ";
			}
		}

		String material = null;
		if (itemStack.getItem() instanceof TieredItem) {
			IItemTier tier = ((TieredItem) itemStack.getItem()).getTier();
			if (tier instanceof Enum<?>) material = (((Enum<?>) tier).name());
		}
		if (material != null) {
			String[][] materials = { { "Wooden", "Wood", "Hardwood", "Balsa Wood", "Mahogany", "Plywood" }, { "Stone", "Rock", "Marble", "Cobblestone", }, { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" }, { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" }, { "Golden", "Gold", "Gilt", "Auric", "Ornate" } };
			int index = -1;
			if (material.equals(ItemTier.WOOD.toString())) {
				index = 0;
			} else if (material.equals(ItemTier.STONE.toString())) {
				index = 1;
			} else if (material.equals(ItemTier.IRON.toString())) {
				index = 2;
			} else if (material.equals(ItemTier.DIAMOND.toString())) {
				index = 3;
			} else if (material.equals(ItemTier.GOLD.toString())) {
				index = 4;
			}
			if (index < 0) {
				name += WordUtils.capitalize(material.toLowerCase()) + " ";
			} else {
				name += materials[index][random.nextInt(materials[index].length)] + " ";
			}

			String[] type = { "Tool" };
			if (itemStack.getItem() instanceof SwordItem) {
				type = new String[] { "Sword", "Cutter", "Slicer", "Dicer", "Knife", "Blade", "Machete", "Brand", "Claymore", "Cutlass", "Foil", "Dagger", "Glaive", "Rapier", "Saber", "Scimitar", "Shortsword", "Longsword", "Broadsword", "Calibur" };
			} else if (itemStack.getItem() instanceof AxeItem) {
				type = new String[] { "Axe", "Chopper", "Hatchet", "Tomahawk", "Cleaver", "Hacker", "Tree-Cutter", "Truncator" };
			} else if (itemStack.getItem() instanceof PickaxeItem) {
				type = new String[] { "Pickaxe", "Pick", "Mattock", "Rock-Smasher", "Miner" };
			} else if (itemStack.getItem() instanceof ShovelItem) {
				type = new String[] { "Shovel", "Spade", "Digger", "Excavator", "Trowel", "Scoop" };
			}
			name += type[random.nextInt(type.length)];
		} else if (itemStack.getItem() instanceof BowItem) {
			String[] type = { "Bow", "Shortbow", "Longbow", "Flatbow", "Recurve Bow", "Reflex Bow", "Self Bow", "Composite Bow", "Arrow-Flinger" };
			name += type[random.nextInt(type.length)];
		} else if (itemStack.getItem() instanceof ArmorItem) {
			String[][] materials = { { "Leather", "Rawhide", "Lamellar", "Cow Skin" }, { "Chainmail", "Chain", "Chain Link", "Scale" }, { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" }, { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" }, { "Golden", "Gold", "Gilt", "Auric", "Ornate" } };
			material = ((ArmorItem) itemStack.getItem()).getArmorMaterial().toString();
			int index = -1;
			if (material.equals(ArmorMaterial.LEATHER.toString())) {
				index = 0;
			} else if (material.equals(ArmorMaterial.CHAIN.toString())) {
				index = 1;
			} else if (material.equals(ArmorMaterial.IRON.toString())) {
				index = 2;
			} else if (material.equals(ArmorMaterial.DIAMOND.toString())) {
				index = 3;
			} else if (material.equals(ArmorMaterial.GOLD.toString())) {
				index = 4;
			}
			if (index < 0) {
				name += WordUtils.capitalize(material.toLowerCase()) + " ";
			} else {
				name += materials[index][random.nextInt(materials[index].length)] + " ";
			}

			String[] type = { "Armor" };
			switch (((ArmorItem) itemStack.getItem()).getEquipmentSlot()) {
			case HEAD:
				type = new String[] { "Helmet", "Cap", "Crown", "Great Helm", "Bassinet", "Sallet", "Close Helm", "Barbute" };
				break;
			case CHEST:
				type = new String[] { "Chestplate", "Tunic", "Brigandine", "Hauberk", "Cuirass" };
				break;
			case LEGS:
				type = new String[] { "Leggings", "Pants", "Tassets", "Cuisses", "Schynbalds" };
				break;
			case FEET:
				type = new String[] { "Boots", "Shoes", "Greaves", "Sabatons", "Sollerets" };
				break;
			default:
			}
			name += type[random.nextInt(type.length)];
		} else {
			name += itemStack.getItem().getDisplayName(itemStack);
		}
		if (!prefixed && modifiers.get(enchantment) != null) {
			String[] temp = modifiers.get(enchantment)[1];
			name += " of " + temp[random.nextInt(temp.length)];
		}
		itemStack.setDisplayName(new StringTextComponent(name).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));
	}
}