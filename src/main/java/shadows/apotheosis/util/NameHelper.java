package shadows.apotheosis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import net.minecraft.entity.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.BowItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TieredItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.placebo.config.Configuration;

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
	 * Possible primary names for shields.
	 */
	private static String[] shields = { "Shield", "Defender", "Ardent Defender", "Greatshield", "Blockade", "Bulwark", "Tower Shield", "Protector", "Aegis" };

	/**
	 * Array of descriptors for items based on tool material.
	 */
	private static Map<IItemTier, String[]> materials = new HashMap<>();
	static {
		materials.put(ItemTier.WOOD, new String[] { "Wooden", "Wood", "Hardwood", "Balsa Wood", "Mahogany", "Plywood" });
		materials.put(ItemTier.STONE, new String[] { "Stone", "Rock", "Marble", "Cobblestone", });
		materials.put(ItemTier.IRON, new String[] { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" });
		materials.put(ItemTier.DIAMOND, new String[] { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" });
		materials.put(ItemTier.GOLD, new String[] { "Golden", "Gold", "Gilt", "Auric", "Ornate" });
		materials.put(ItemTier.NETHERITE, new String[] { "Burnt", "Embered", "Fiery", "Hellborn", "Flameforged" });
	}

	/**
	 * Array of descriptors for items based on armor material.
	 */
	private static Map<IArmorMaterial, String[]> armors = new HashMap<>();
	static {
		armors.put(ArmorMaterial.LEATHER, new String[] { "Leather", "Rawhide", "Lamellar", "Cow Skin" });
		armors.put(ArmorMaterial.CHAIN, new String[] { "Chainmail", "Chain", "Chain Link", "Scale" });
		armors.put(ArmorMaterial.IRON, new String[] { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" });
		armors.put(ArmorMaterial.DIAMOND, new String[] { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" });
		armors.put(ArmorMaterial.GOLD, new String[] { "Golden", "Gold", "Gilt", "Auric", "Ornate" });
		armors.put(ArmorMaterial.NETHERITE, new String[] { "Burnt", "Embered", "Fiery", "Hellborn", "Flameforged" });
		armors.put(ArmorMaterial.TURTLE, new String[] { "Tortollan", "Very Tragic", "Environmental", "Organic" });
	}

	public static String suffixFormat = "%s the %s";
	public static String ownershipFormat = "%s's";
	public static String chainFormat = "%s %s";

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
	public static String setEntityName(Random random, MobEntity entity) {
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
			name = String.format(suffixFormat, name, NameHelper.suffixes[random.nextInt(NameHelper.suffixes.length)]);
		}
		entity.setCustomName(new StringTextComponent(name));
		return root;
	}

	/**
	 * Applies a random name to an itemstack, based on the owning entity name, and the item itself.
	 * An additional prefix will be selected based on the item type.
	 * This is a best-guess system.  One half of the name is based on the material, the other half is based on the item type.
	 * The secondary half will fall back to the item display name, if what the item is cannot be inferred.
	 * @param itemStack The stack to be named.
	 * @param name The name of the owning entity, usually created by {@link NameHelper#setEntityName(Random, EntityLiving)}
	 * @return The name of the item, without the owning prefix of the boss's name
	 */
	public static TextComponent setItemName(Random random, ItemStack itemStack, String bossName) {
		TextComponent name = (TextComponent) itemStack.getDisplayName();

		if (itemStack.getItem() instanceof TieredItem) {
			IItemTier material = ((TieredItem) itemStack.getItem()).getTier();
			String[] descriptors = getMaterialDescriptors(material);
			name = new StringTextComponent(descriptors[random.nextInt(descriptors.length)] + " ");

			String[] type = { "Tool" };
			Set<ToolType> types = itemStack.getToolTypes();

			if (itemStack.getItem() instanceof SwordItem) {
				type = swords;
			} else if (types.contains(ToolType.AXE)) {
				type = axes;
			} else if (types.contains(ToolType.PICKAXE)) {
				type = pickaxes;
			} else if (types.contains(ToolType.SHOVEL)) {
				type = shovels;
			}
			name.appendString(type[random.nextInt(type.length)]);
		} else if (itemStack.getItem() instanceof BowItem) {
			String[] type = bows;
			name = new StringTextComponent(type[random.nextInt(type.length)]);
		} else if (itemStack.getItem() instanceof ArmorItem) {
			IArmorMaterial amaterial = ((ArmorItem) itemStack.getItem()).getArmorMaterial();
			String[] descriptors = getArmorDescriptors(amaterial);
			name = new StringTextComponent(descriptors[random.nextInt(descriptors.length)] + " ");

			String[] type = { "Armor" };
			switch (((ArmorItem) itemStack.getItem()).getEquipmentSlot()) {
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
			name.appendString(type[random.nextInt(type.length)]);
		} else if (itemStack.isShield(null)) {
			String[] type = shields;
			name = new StringTextComponent(type[random.nextInt(type.length)]);
		}

		itemStack.setDisplayName(name);
		return name;
	}

	private static String[] getMaterialDescriptors(IItemTier materialName) {
		return materials.computeIfAbsent(materialName, s -> new String[] { "" });
	}

	private static String[] getArmorDescriptors(IArmorMaterial materialName) {
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
		shields = c.getStringList("shields", "items", shields, "A list of root names for shields, used in the generation of item names. May not be empty.");

		Preconditions.checkArgument(swords.length > 0 && axes.length > 0 && pickaxes.length > 0 && shovels.length > 0 && bows.length > 0, "Detected empty lists for weapon root names in apotheosis/names.cfg, this is not allowed.");

		Map<IItemTier, List<Item>> itemsByTier = new HashMap<>();
		Map<IArmorMaterial, List<Item>> armorsByTier = new HashMap<>();
		for (Item i : ForgeRegistries.ITEMS) {
			try {
				if (i instanceof TieredItem) {
					IItemTier mat = ((TieredItem) i).getTier();
					itemsByTier.computeIfAbsent(mat, m -> new ArrayList<>()).add(i);
				}
				if (i instanceof ArmorItem) {
					IArmorMaterial mat = ((ArmorItem) i).getArmorMaterial();
					armorsByTier.computeIfAbsent(mat, m -> new ArrayList<>()).add(i);
				}
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("The item {} has thrown an exception while attempting to access it's tier.", i.getRegistryName());
				e.printStackTrace();
			}
		}

		for (Map.Entry<IItemTier, List<Item>> e : itemsByTier.entrySet()) {
			IItemTier tier = e.getKey();
			List<Item> items = e.getValue();
			String[] read = c.getStringList(getID(tier, items), "tools", materials.getOrDefault(tier, new String[0]), computeComment(items, tier::getRepairMaterial));
			if (read.length > 0) materials.put(tier, read);
		}

		for (Map.Entry<IArmorMaterial, List<Item>> e : armorsByTier.entrySet()) {
			IArmorMaterial tier = e.getKey();
			List<Item> items = e.getValue();
			String[] read = c.getStringList(getID(tier, items), "armors", armors.getOrDefault(tier, new String[0]), computeComment(items, tier::getRepairMaterial));
			if (read.length > 0) armors.put(tier, read);
		}

		suffixFormat = c.getString("Suffix Format", "formatting", suffixFormat, "The format string that will be used when a suffix is applied.");
		ownershipFormat = c.getString("Ownership Format", "formatting", ownershipFormat, "The format string that will be used to indicate ownership.");

		if (c.hasChanged()) c.save();
	}

	private static String computeComment(List<Item> items, Supplier<Ingredient> repair) {
		String cmt = "A list of material-based prefix names for this material group. May be empty.\n";
		cmt += "Items in this group: ";
		for (Item i : items)
			cmt += i.getRegistryName() + ", ";
		cmt = cmt.substring(0, cmt.length() - 2);
		cmt += "\nRepair Material: " + getFirstMatch(repair);
		return cmt + "\n";
	}

	private static String getID(Object o, List<Item> items) {
		if (o instanceof Enum<?>) return ((Enum<?>) o).name();
		ResourceLocation id = items.get(0).getRegistryName();
		return id.getNamespace() + "_" + id.getPath();
	}

	private static String getFirstMatch(Supplier<Ingredient> supplier) {
		try {
			Ingredient repair = supplier.get();
			return repair == null || repair.hasNoMatchingItems() ? "null" : repair.getMatchingStacks()[0].getItem().getRegistryName().toString();
		} catch (Exception e) {
			return "null";
		}
	}

}