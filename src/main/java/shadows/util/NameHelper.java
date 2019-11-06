package shadows.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Preconditions;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Generates names for various objects, based on stuff.
 * @author Shadows
 *
 */
public class NameHelper {

	/**
	 * List of all possible full names.
	*/
	private static String[] names = { "Albert", "Andrew", "Anderson", "Andy", "Allan", "Arthur", "Aaron", "Allison", "Arielle", "Amanda", "Anne", "Annie", "Amy", "Alana", "Brandon", "Brady", "Bernard", "Ben", "Benjamin", "Bob", "Bobette", "Brooke", "Brandy", "Beatrice", "Bea", "Bella", "Becky", "Carlton", "Carl", "Calvin", "Cameron", "Carson", "Chase", "Cassandra", "Cassie", "Cas", "Carol", "Carly", "Cherise", "Charlotte", "Cheryl", "Chasity", "Danny", "Drake", "Daniel", "Derrel", "David", "Dave", "Donovan", "Don", "Donald", "Drew", "Derrick", "Darla", "Donna", "Dora", "Danielle", "Edward", "Elliot", "Ed", "Edson", "Elton", "Eddison", "Earl", "Eric", "Ericson", "Eddie", "Ediovany", "Emma", "Elizabeth", "Eliza", "Esperanza", "Esper", "Esmeralda", "Emi", "Emily", "Elaine", "Fernando", "Ferdinand", "Fred", "Feddie", "Fredward", "Frank", "Franklin", "Felix", "Felicia", "Fran", "Greg", "Gregory", "George", "Gerald", "Gina", "Geraldine", "Gabby", "Hendrix", "Henry", "Hobbes", "Herbert", "Heath", "Henderson", "Helga", "Hera", "Helen", "Helena", "Hannah", "Ike", "Issac", "Israel", "Ismael", "Irlanda", "Isabelle", "Irene", "Irenia", "Jimmy", "Jim", "Justin", "Jacob", "Jake", "Jon", "Johnson", "Jonny", "Jonathan", "Josh", "Joshua", "Julian", "Jesus", "Jericho", "Jeb", "Jess", "Joan", "Jill", "Jillian", "Jessica", "Jennifer", "Jenny", "Jen", "Judy", "Kenneth", "Kenny", "Ken", "Keith", "Kevin", "Karen", "Kassandra", "Kassie", "Leonard", "Leo", "Leroy", "Lee", "Lenny", "Luke", "Lucas", "Liam", "Lorraine", "Latasha", "Lauren", "Laquisha", "Livia", "Lydia", "Lila", "Lilly", "Lillian", "Lilith", "Lana", "Mason", "Mike", "Mickey", "Mario", "Manny", "Mark", "Marcus", "Martin", "Marty", "Matthew", "Matt", "Max", "Maximillian", "Marth", "Mia", "Marriah", "Maddison", "Maddie", "Marissa", "Miranda", "Mary", "Martha", "Melonie", "Melody", "Mel", "Minnie", "Nathan", "Nathaniel", "Nate", "Ned", "Nick", "Norman", "Nicholas", "Natasha", "Nicki", "Nora", "Nelly", "Nina", "Orville", "Oliver", "Orlando", "Owen", "Olsen", "Odin", "Olaf", "Ortega", "Olivia", "Patrick", "Pat", "Paul", "Perry", "Pinnochio", "Patrice", "Patricia", "Pennie", "Petunia", "Patti", "Pernelle", "Quade", "Quincy", "Quentin", "Quinn", "Roberto", "Robbie", "Rob", "Robert", "Roy", "Roland", "Ronald", "Richard", "Rick", "Ricky", "Rose", "Rosa", "Rhonda", "Rebecca", "Roberta", "Sparky", "Shiloh", "Stephen", "Steve", "Saul", "Sheen", "Shane", "Sean", "Sampson", "Samuel", "Sammy", "Stefan", "Sasha", "Sam", "Susan", "Suzy", "Shelby", "Samantha", "Sheila", "Sharon", "Sally", "Stephanie", "Sandra", "Sandy", "Sage", "Tim", "Thomas", "Thompson", "Tyson", "Tyler", "Tom", "Tyrone", "Timmothy", "Tamara", "Tabby", "Tabitha", "Tessa", "Tiara", "Tyra", "Uriel", "Ursala", "Uma", "Victor", "Vincent", "Vince", "Vance", "Vinny", "Velma", "Victoria", "Veronica", "Wilson", "Wally", "Wallace", "Will", "Wilard", "William", "Wilhelm", "Xavier", "Xandra", "Young", "Yvonne", "Yolanda", "Zach", "Zachary" };

	/**
	 * List of all name parts.
	 */
	private static String[] nameParts = { "Grab", "Thar", "Ger", "Ald", "Mas", "On", "O", "Din", "Thor", "Jon", "Ath", "Burb", "En", "A", "E", "I", "U", "Hab", "Bloo", "Ena", "Dit", "Aph", "Ern", "Bor", "Dav", "Id", "Toast", "Son", "Dottir", "For", "Wen", "Lob", "Ed", "Die", "Van", "Y", "Zap", "Ear", "Ben", "Don", "Bran", "Gro", "Jen", "Bob", "Ette", "Ere", "Man", "Qua", "Bro", "Cree", "Per", "Skel", "Ton", "Zom", "Bie", "Wolf", "End", "Er", "Pig", "Sil", "Ver", "Fish", "Cow", "Chic", "Ken", "Sheep", "Squid", "Hell" };

	/**
	 * List of prefixes, that are optionally applied to names.
	 */
	private static String[] prefixes = { "Sir", "Mister", "Madam", "Doctor", "Father", "Mother" };

	/**
	 * List of suffixes, that are optionally applied to names.  A suffix will always be preceeded by "the"
	 * That is, selecting "Mighty" from this list would incur the addition of "The Mighty" to the name.
	 */
	private static String[] suffixes = { "Mighty", "Supreme", "Superior", "Ultimate", "Lame", "Wimpy", "Curious", "Sneaky", "Pathetic", "Crying", "Eagle", "Errant", "Unholy", "Questionable", "Mean", "Hungry", "Thirsty", "Feeble", "Wise", "Sage", "Magical", "Mythical", "Legendary", "Not Very Nice", "Jerk", "Doctor", "Misunderstood", "Angry", "Knight", "Bishop", "Godly", "Special", "Toasty", "Shiny", "Shimmering", "Light", "Dark", "Odd-Smelling", "Funky", "Rock Smasher", "Son of Herobrine", "Cracked", "Sticky", "\u00a7kAlien\u00a7r", "Baby", "Manly", "Rough", "Scary", "Undoubtable", "Honest", "Non-Suspicious", "Boring", "Odd", "Lazy", "Super", "Nifty", "Ogre Slayer", "Pig Thief", "Dirt Digger", "Really Cool", "Doominator", "... Something" };

	/**
	 * Possible primary names for helmets.
	 */
	private static String[] helms = { "Helmet", "Cap", "Crown", "Great Helm", "Bassinet", "Sallet", "Close Helm", "Barbute" };

	/**
	 * Possible primary names for chestplates.
	 */
	private static String[] chestplates = { "Chestplate", "Tunic", "Brigandine", "Hauberk", "Cuirass" };

	/**
	 * Possible primary names for leggings.
	 */
	private static String[] leggings = { "Leggings", "Pants", "Tassets", "Cuisses", "Schynbalds" };

	/**
	 * Possible primary names for boots.
	 */
	private static String[] boots = { "Boots", "Shoes", "Greaves", "Sabatons", "Sollerets" };

	/**
	 * Possible primary names for swords.
	 */
	private static String[] swords = { "Sword", "Cutter", "Slicer", "Dicer", "Knife", "Blade", "Machete", "Brand", "Claymore", "Cutlass", "Foil", "Dagger", "Glaive", "Rapier", "Saber", "Scimitar", "Shortsword", "Longsword", "Broadsword", "Calibur" };

	/**
	 * Possible primary names for axes.
	 */
	private static String[] axes = { "Axe", "Chopper", "Hatchet", "Tomahawk", "Cleaver", "Hacker", "Tree-Cutter", "Truncator" };

	/**
	 * Possible primary names for pickaxes.
	 */
	private static String[] pickaxes = { "Pickaxe", "Pick", "Mattock", "Rock-Smasher", "Miner" };

	/**
	 * Possible primary names for shovels.
	 */
	private static String[] shovels = { "Shovel", "Spade", "Digger", "Excavator", "Trowel", "Scoop" };

	/**
	 * Possible primary names for bows.
	 */
	private static String[] bows = { "Bow", "Shortbow", "Longbow", "Flatbow", "Recurve Bow", "Reflex Bow", "Self Bow", "Composite Bow", "Arrow-Flinger" };

	/**
	 * Array of descriptors for items based on tool material.
	 */
	private static Map<String, String[]> materials = new HashMap<>();
	static {
		materials.put(ToolMaterial.WOOD.toString(), new String[] { "Wooden", "Wood", "Hardwood", "Balsa Wood", "Mahogany", "Plywood" });
		materials.put(ToolMaterial.STONE.toString(), new String[] { "Stone", "Rock", "Marble", "Cobblestone", });
		materials.put(ToolMaterial.IRON.toString(), new String[] { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" });
		materials.put(ToolMaterial.DIAMOND.toString(), new String[] { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" });
		materials.put(ToolMaterial.GOLD.toString(), new String[] { "Golden", "Gold", "Gilt", "Auric", "Ornate" });
	}

	/**
	 * Array of descriptors for items based on armor material.
	 */
	private static Map<String, String[]> armors = new HashMap<>();
	static {
		armors.put(ArmorMaterial.LEATHER.toString(), new String[] { "Leather", "Rawhide", "Lamellar", "Cow Skin" });
		armors.put(ArmorMaterial.CHAIN.toString(), new String[] { "Chainmail", "Chain", "Chain Link", "Scale" });
		armors.put(ArmorMaterial.IRON.toString(), new String[] { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" });
		armors.put(ArmorMaterial.DIAMOND.toString(), new String[] { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" });
		armors.put(ArmorMaterial.GOLD.toString(), new String[] { "Golden", "Gold", "Gilt", "Auric", "Ornate" });
	}

	/**
	 * Map of enchantments to name variables.  The sub arrays contain prefixes and suffixes, respectively.
	 */
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

	/**
	 * Makes a name using {@link NameHelper#nameParts}.
	 * The name is made out of a random value from name parts, combined with up to two more values from the array.
	 * The selected values are not unique, and may overlap.
	 */
	public static String nameFromParts(Random random) {
		String name = NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)] + NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)].toLowerCase();
		if (random.nextBoolean()) {
			name += NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)].toLowerCase();
		}
		return name;
	}

	/**
	 * Applies a random name to an entity.
	 * The root name is either randomly selected from {@link NameHelper#names} or generated by {@link NameHelper#nameFromParts(Random)}
	 * There is a 20% chance for a prefix to be selected from {@link NameHelper#prefixes}
	 * There is a 50% chance for a suffix to be selected from {@link NameHelper#suffixes}
	 * @return The root name of the entity, without any prefixes or suffixes.
	 */
	public static String setEntityName(Random random, EntityLiving entity) {
		String root;

		if (names.length > 0 && nameParts.length > 0) {
			root = random.nextBoolean() ? NameHelper.names[random.nextInt(NameHelper.names.length)] : NameHelper.nameFromParts(random);
		} else if (names.length > 0) {
			root = NameHelper.names[random.nextInt(NameHelper.names.length)];
		} else {
			root = NameHelper.nameFromParts(random);
		}

		String name = root;
		if (random.nextInt(5) == 0 && prefixes.length > 0) name = NameHelper.prefixes[random.nextInt(NameHelper.prefixes.length)] + " " + name;
		if (random.nextBoolean() && suffixes.length > 0) {
			name += " the " + NameHelper.suffixes[random.nextInt(NameHelper.suffixes.length)];
		}
		entity.setCustomNameTag(name);
		return root;
	}

	/**
	 * Applies a random name to an itemstack, based on the owning entity name, and a primary enchantment.
	 * There is a 50% chance to select a prefix based on the passed enchantment.  If this fails, a suffix will be selected instead.
	 * An additional prefix will be selected based on the item type.
	 * This is a best-guess system.  One half of the name is based on the tool material, the other half is based on the item type.
	 * The secondary half will fall back to the item display name, if what the item is cannot be inferred.
	 * A random color will be selected as the final step.  Uses one of the first 15 {@link TextFormatting} values.
	 * @param itemStack The stack to be named.
	 * @param name The name of the owning entity, usually created by {@link NameHelper#setEntityName(Random, EntityLiving)}
	 * @param enchantment The primary enchantment of the item, used to retrieve a descriptor from {@link NameHelper#modifiers}
	 */
	public static void setItemName(Random random, ItemStack itemStack, String name, Enchantment enchantment) {
		name += "'s ";

		boolean prefix = random.nextBoolean();

		if (prefix && NameHelper.modifiers.get(enchantment) != null) {
			String[] temp = modifiers.get(enchantment)[0];
			if (temp.length > 0) name += temp[random.nextInt(temp.length)] + " ";
		}

		String material = null;
		if (itemStack.getItem() instanceof ItemSword) {
			material = ((ItemSword) itemStack.getItem()).getToolMaterialName();
		} else if (itemStack.getItem() instanceof ItemTool) {
			material = ((ItemTool) itemStack.getItem()).getToolMaterialName();
		}

		if (material != null) {
			String[] descriptors = getMaterialDescriptors(material);
			name += descriptors[random.nextInt(descriptors.length)] + " ";

			String[] type = { "Tool" };
			if (itemStack.getItem() instanceof ItemSword) {
				type = swords;
			} else if (itemStack.getItem() instanceof ItemAxe) {
				type = axes;
			} else if (itemStack.getItem() instanceof ItemPickaxe) {
				type = pickaxes;
			} else if (itemStack.getItem() instanceof ItemSpade) {
				type = shovels;
			}
			name += type[random.nextInt(type.length)];
		} else if (itemStack.getItem() instanceof ItemBow) {
			String[] type = bows;
			name += type[random.nextInt(type.length)];
		} else if (itemStack.getItem() instanceof ItemArmor) {

			material = ((ItemArmor) itemStack.getItem()).getArmorMaterial().toString();
			String[] descriptors = getArmorDescriptors(material);
			name += descriptors[random.nextInt(descriptors.length)] + " ";

			String[] type = { "Armor" };
			switch (((ItemArmor) itemStack.getItem()).armorType) {
			case HEAD:
				type = helms;
				break;
			case CHEST:
				type = chestplates;
				break;
			case LEGS:
				type = leggings;
				break;
			case FEET:
				type = boots;
				break;
			default:
			}
			name += type[random.nextInt(type.length)];
		} else {
			name += itemStack.getItem().getItemStackDisplayName(itemStack);
		}

		if (!prefix && modifiers.get(enchantment) != null) {
			String[] temp = modifiers.get(enchantment)[1];
			if (temp.length > 0) name += " of " + temp[random.nextInt(temp.length)];
		}

		itemStack.setStackDisplayName(TextFormatting.values()[random.nextInt(15)] + name);
	}

	private static String[] getMaterialDescriptors(String materialName) {
		return materials.computeIfAbsent(materialName, s -> new String[] { "" });
	}

	private static String[] getArmorDescriptors(String materialName) {
		return armors.computeIfAbsent(materialName, s -> new String[] { "" });
	}

	public static void load(Configuration c) {

		names = c.getStringList("Names", "entity", names, "A list of full names, which are used in the generation of boss names. May be empty only if name parts is not empty.");
		nameParts = c.getStringList("Name Parts", "entity", nameParts, "A list of name pieces, which can be spliced together to create full names.  May be empty only if names is not empty.");
		Preconditions.checkArgument(names.length != 0 || nameParts.length != 0, "Both names and name parts are empty in apotheosis/names.cfg, this is not allowed.");

		prefixes = c.getStringList("Prefixes", "entity", prefixes, "A list of prefixes, which are used in the generation of boss names. May be empty.");
		suffixes = c.getStringList("Suffixes", "entity", suffixes, "A list of suffixes, which are used in the generation of boss names. A suffix is always preceeded by \"The\". May be empty.");

		helms = c.getStringList("Helms", "items", helms, "A list of root names for helms, used in the generation of item names. May not be empty.");
		chestplates = c.getStringList("chestplates", "items", chestplates, "A list of root names for chestplates, used in the generation of item names. May not be empty.");
		leggings = c.getStringList("leggings", "items", leggings, "A list of root names for leggings, used in the generation of item names. May not be empty.");
		boots = c.getStringList("boots", "items", boots, "A list of root names for boots, used in the generation of item names. May not be empty.");

		Preconditions.checkArgument(helms.length > 0 && chestplates.length > 0 && leggings.length > 0 && boots.length > 0, "Detected empty lists for armor root names in apotheosis/names.cfg, this is not allowed.");

		swords = c.getStringList("swords", "items", swords, "A list of root names for swords, used in the generation of item names. May not be empty.");
		axes = c.getStringList("axes", "items", axes, "A list of root names for axes, used in the generation of item names. May not be empty.");
		pickaxes = c.getStringList("pickaxes", "items", pickaxes, "A list of root names for pickaxes, used in the generation of item names. May not be empty.");
		shovels = c.getStringList("shovels", "items", shovels, "A list of root names for shovels, used in the generation of item names. May not be empty.");
		bows = c.getStringList("bows", "items", bows, "A list of root names for bows, used in the generation of item names. May not be empty.");

		Preconditions.checkArgument(swords.length > 0 && axes.length > 0 && pickaxes.length > 0 && shovels.length > 0 && bows.length > 0, "Detected empty lists for weapon root names in apotheosis/names.cfg, this is not allowed.");

		for (ToolMaterial mat : ToolMaterial.values()) {
			String[] read = c.getStringList(mat.toString(), "tools", materials.getOrDefault(mat.toString(), new String[0]), "A list of material-based prefix names for the given tool material. May be empty.");
			if (read.length > 0) materials.put(mat.toString(), read);
		}

		for (ArmorMaterial mat : ArmorMaterial.values()) {
			String[] read = c.getStringList(mat.toString(), "armors", armors.getOrDefault(mat.toString(), new String[0]), "A list of material-based prefix names for the given armor material. May be empty.");
			if (read.length > 0) armors.put(mat.toString(), read);
		}

		for (Enchantment e : ForgeRegistries.ENCHANTMENTS) {
			String[][] mods = modifiers.get(e);
			if (mods == null) {
				mods = new String[][] { {}, {} };
				modifiers.put(e, mods);
			}
			mods[0] = c.getStringList("Prefixes", "enchantments." + e.getRegistryName().toString(), mods[0], "The possible prefixes associated with this enchantment.");
			mods[1] = c.getStringList("Suffixes", "enchantments." + e.getRegistryName().toString(), mods[1], "The possible suffixes associated with this enchantment.");
		}

		if (c.hasChanged()) c.save();
	}
}