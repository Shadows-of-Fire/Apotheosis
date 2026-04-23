package dev.shadowsoffire.apotheosis.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Generates names for various objects, based on stuff.
 *
 * @author Shadows
 */
public class NameHelper {

    /**
     * List of all possible full names.
     */
    private static String[] names = { "Biscuit", "Elisande", "Willow",
        "Bippy", "Butto", "Prim", "Tyrael", "Bajorno", "Michael Morbius", "Morbius", "Arun", "Panez", "Doomsday", "Vanamar", "WhatTheDrunk",
        "Lothrazar", "Chelly", "Chelicia", "Darsh", "Dariush", "Cheese E Piloza", "Bing", "Royal", "NoWayHere", "SwankyStella", "Isosahedron",
        "Asfalis", "Biz", "Icicle", "Darko", "Shadows", "Katarina", "Faellynna", "Diliviel", "Jank", "Albert", "Andrew", "Anderson", "Andy", "Allan",
        "Arthur", "Aaron", "Allison", "Arielle", "Amanda", "Anne", "Annie", "Amy", "Alana", "Brandon", "Brady", "Bernard", "Ben", "Benjamin", "Bob",
        "Bobette", "Brooke", "Brandy", "Beatrice", "Bea", "Bella", "Becky", "Carlton", "Carl", "Calvin", "Cameron", "Carson", "Chase", "Cassandra",
        "Cassie", "Cas", "Carol", "Carly", "Cherise", "Charlotte", "Cheryl", "Chasity", "Danny", "Drake", "Daniel", "Derrel", "David", "Dave", "Donovan",
        "Don", "Donald", "Drew", "Derrick", "Darla", "Donna", "Dora", "Danielle", "Edward", "Elliot", "Ed", "Edson", "Elton", "Eddison", "Earl", "Eric",
        "Ericson", "Eddie", "Ediovany", "Emma", "Elizabeth", "Eliza", "Esperanza", "Esper", "Esmeralda", "Emi", "Emily", "Elaine", "Fernando", "Ferdinand",
        "Fred", "Feddie", "Fredward", "Frank", "Franklin", "Felix", "Felicia", "Fran", "Greg", "Gregory", "George", "Gerald", "Gina", "Geraldine", "Gabby",
        "Hendrix", "Henry", "Hobbes", "Herbert", "Heath", "Henderson", "Helga", "Hera", "Helen", "Helena", "Hannah", "Ike", "Issac", "Israel", "Ismael", "Irlanda",
        "Isabelle", "Irene", "Irenia", "Jimmy", "Jim", "Justin", "Jacob", "Jake", "Jon", "Johnson", "Jonny", "Jonathan", "Josh", "Joshua", "Julian", "Jesus",
        "Jericho", "Jeb", "Jess", "Joan", "Jill", "Jillian", "Jessica", "Jennifer", "Jenny", "Jen", "Judy", "Kenneth", "Kenny", "Ken", "Keith", "Kevin", "Karen",
        "Kassandra", "Kassie", "Leonard", "Leo", "Leroy", "Lee", "Lenny", "Luke", "Lucas", "Liam", "Lorraine", "Latasha", "Lauren", "Laquisha", "Livia",
        "Lydia", "Lila", "Lilly", "Lillian", "Lilith", "Lana", "Mason", "Mike", "Mickey", "Mario", "Manny", "Mark", "Marcus", "Martin", "Marty", "Matthew",
        "Matt", "Max", "Maximillian", "Marth", "Mia", "Marriah", "Maddison", "Maddie", "Marissa", "Miranda", "Mary", "Martha", "Melonie", "Melody", "Mel",
        "Minnie", "Nathan", "Nathaniel", "Nate", "Ned", "Nick", "Norman", "Nicholas", "Natasha", "Nicki", "Nora", "Nelly", "Nina", "Orville", "Oliver",
        "Orlando", "Owen", "Olsen", "Odin", "Olaf", "Ortega", "Olivia", "Patrick", "Pat", "Paul", "Perry", "Pinnochio", "Patrice", "Patricia", "Pennie",
        "Petunia", "Patti", "Pernelle", "Quade", "Quincy", "Quentin", "Quinn", "Roberto", "Robbie", "Rob", "Robert", "Roy", "Roland", "Ronald", "Richard",
        "Rick", "Ricky", "Rose", "Rosa", "Rhonda", "Rebecca", "Roberta", "Sparky", "Shiloh", "Stephen", "Steve", "Saul", "Sheen", "Shane", "Sean", "Sampson",
        "Samuel", "Sammy", "Stefan", "Sasha", "Sam", "Susan", "Suzy", "Shelby", "Samantha", "Sheila", "Sharon", "Sally", "Stephanie", "Sandra", "Sandy",
        "Sage", "Tim", "Thomas", "Thompson", "Tyson", "Tyler", "Tom", "Tyrone", "Timmothy", "Tamara", "Tabby", "Tabitha", "Tessa", "Tiara", "Tyra", "Uriel",
        "Ursala", "Uma", "Victor", "Vincent", "Vince", "Vance", "Vinny", "Velma", "Victoria", "Veronica", "Wilson", "Wally", "Wallace", "Will", "Wilard",
        "William", "Wilhelm", "Xavier", "Xandra", "Young", "Yvonne", "Yolanda", "Zach", "Zachary" };

    /**
     * List of all name parts.
     */
    private static String[] nameParts = { "Prim", "Morb", "Ius", "Kat", "Chel", "Bing", "Darsh", "Jank", "Dark", "Osto", "Grab", "Thar",
        "Ger", "Ald", "Mas", "On", "O", "Din", "Thor", "Jon", "Ath", "Burb", "En", "A", "E", "I", "U", "Hab", "Bloo", "Ena",
        "Dit", "Aph", "Ern", "Bor", "Dav", "Id", "Toast", "Son", "For", "Wen", "Lob", "Van", "Zap", "Ear", "Ben", "Don", "Bran",
        "Gro", "Jen", "Bob", "Ette", "Ere", "Man", "Qua", "Bro", "Cree", "Per", "Skel", "Ton", "Zom", "Bie", "Wolf", "End", "Er",
        "Pig", "Sil", "Ver", "Fish", "Cow", "Chic", "Ken", "Sheep", "Squid", "Hell", "Dra", "Gor", "Nyx", "Fae", "Lux", "Vex",
        "Hex", "Rune", "Frost", "Flame", "Storm", "Shade", "Dawn", "Dusk", "Ash", "Mist", "Might", "Fury", "Rage", "Doom", "Grim",
        "Void", "Rend", "Slay", "Ar", "Or", "Ur", "El", "Al", "Im", "Un", "En", "Ix", "Ox", "Dire", "Dark", "Bright", "Swift", "Glow",
        "Shine", "Gleam", "Spark"
    };

    /**
     * List of prefixes, that are optionally applied to names.
     */
    private static String[] prefixes = { "Dr. Michael", "Sir", "Mister", "Madam", "Doctor", "Father", "Mother", "Poppa", "Lord", "Lady", "Overseer", "Professor",
        "Mr.", "Mr. President", "Duke", "Duchess", "Dame", "The Honorable", "Chancellor", "Vice-Chancellor", "His Holiness", "Reverend", "Count", "Viscount",
        "Earl", "Captain", "Major", "General", "Senpai", "Discount" };

    /**
     * List of suffixes, that are optionally applied to names. A suffix will always be preceeded by "the"
     * That is, selecting "Mighty" from this list would incur the addition of "The Mighty" to the name.
     */
    private static String[] suffixes = { "Morbius", "Dragonborn", "Rejected", "Mighty", "Supreme", "Superior", "Ultimate", "Lame", "Wimpy", "Curious", "Sneaky",
        "Pathetic", "Crying", "Eagle", "Errant", "Unholy", "Questionable", "Mean", "Hungry", "Thirsty", "Feeble", "Wise", "Sage", "Magical", "Mythical",
        "Legendary", "Not Very Nice", "Jerk", "Doctor", "Misunderstood", "Angry", "Knight", "Bishop", "Godly", "Special", "Toasty", "Shiny", "Shimmering",
        "Light", "Dark", "Odd-Smelling", "Funky", "Rock Smasher", "Son of Herobrine", "Cracked", "Sticky", "\u00a7kAlien\u00a7r", "Baby", "Manly", "Rough",
        "Scary", "Undoubtable", "Honest", "Non-Suspicious", "Boring", "Odd", "Lazy", "Super", "Nifty", "Ogre Slayer", "Pig Thief", "Dirt Digger", "Really Cool",
        "Doominator", "... Something", "Extra-Fishy", "Gorilla Slaughterer", "Marbles Winner", "AC Rizzlord", "President", "Burger Chef", "Professional Animator",
        "Cheese Sprayer", "Happiness Advocate", "Ghost Hunter", "Head of Potatoes", "Ninja", "Warrior", "Pyromancer", "Trombone Player", "Airport Technician",
        "Grand Magistrix", "Starved", "Terrifying", "Expert Cloud Watcher", "Cookie Enthusiast", "Grass Toucher", "Coffee Addict", "Mildly Confused"
    };

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
    private static String[] swords = { "Sword", "Cutter", "Slicer", "Dicer", "Knife", "Blade", "Machete", "Brand", "Claymore", "Cutlass", "Foil", "Dagger", "Glaive", "Rapier", "Saber", "Scimitar", "Shortsword", "Longsword",
        "Broadsword", "Calibur" };

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
    private static String[] shields = { "Shield", "Buckler", "Targe", "Greatshield", "Blockade", "Bulwark", "Tower Shield", "Protector", "Aegis" };

    /**
     * Ordered map of material path-fragment → prefix name candidates. Fragments are matched
     * by {@code path.contains(fragment)} and the first matching entry wins, so longer/more
     * specific keys must appear before any shorter keys they would otherwise collide with.
     */
    private static Map<String, String[]> materialNames = new LinkedHashMap<>();
    static {
        materialNames.put("netherite", new String[] { "Burnt", "Embered", "Fiery", "Hellborn", "Flameforged" });
        materialNames.put("diamond", new String[] { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" });
        materialNames.put("chainmail", new String[] { "Chainmail", "Chain", "Chain Link", "Scale" });
        materialNames.put("ironwood", new String[] { "Ironwood", "Earthbound", "Oaken", "Ironcapped" });
        materialNames.put("knightmetal", new String[] { "Knightmetal", "Knightly", "Phantom-Forged" });
        materialNames.put("steeleaf", new String[] { "Steeleaf", "Organic", "Natural", "Cobaltstem", "Tungstenpetal" });
        materialNames.put("leather", new String[] { "Leather", "Rawhide", "Lamellar", "Cow Skin" });
        materialNames.put("golden", new String[] { "Golden", "Gold", "Gilt", "Auric", "Ornate" });
        materialNames.put("wooden", new String[] { "Wooden", "Wood", "Hardwood", "Balsa Wood", "Mahogany", "Plywood" });
        materialNames.put("turtle", new String[] { "Tortollan", "Very Tragic", "Environmental", "Organic" });
        materialNames.put("stone", new String[] { "Stone", "Rock", "Marble", "Cobblestone" });
        materialNames.put("fiery", new String[] { "Fiery", "Flaming", "Hydra-Infused", "Infernal" });
        materialNames.put("iron", new String[] { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" });
    }

    public static String suffixFormat = "%s the %s";
    public static String ownershipFormat = "%s's";
    public static String chainFormat = "%s %s";

    /**
     * Makes a name using {@link NameHelper#nameParts}.
     * The name is made out of a random value from name parts, combined with up to two more values from the array.
     * The selected values are not unique, and may overlap.
     */
    public static String nameFromParts(RandomSource random) {
        String name = NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)] + NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)].toLowerCase();
        if (random.nextFloat() < 0.4F) {
            name += NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)].toLowerCase();
        }
        if (random.nextFloat() < 0.15F) {
            name += NameHelper.nameParts[random.nextInt(NameHelper.nameParts.length)].toLowerCase();
        }
        return name;
    }

    /**
     * Applies a random name to an entity.
     * The root name is either randomly selected from {@link NameHelper#names} or generated by {@link NameHelper#nameFromParts(RandomSource)}
     * There is a 50% chance for a prefix to be selected from {@link NameHelper#prefixes}
     * There is a 80% chance for a suffix to be selected from {@link NameHelper#suffixes}
     *
     * @return The root name of the entity, without any prefixes or suffixes.
     */
    public static String setEntityName(RandomSource rand, Mob entity) {
        String root;

        if (names.length > 0 && nameParts.length > 0) {
            root = rand.nextFloat() < 0.45F ? NameHelper.names[rand.nextInt(NameHelper.names.length)] : NameHelper.nameFromParts(rand);
        }
        else if (names.length > 0) {
            root = NameHelper.names[rand.nextInt(NameHelper.names.length)];
        }
        else {
            root = NameHelper.nameFromParts(rand);
        }

        String name = root;
        if (rand.nextFloat() < 0.3F && prefixes.length > 0) {
            name = NameHelper.prefixes[rand.nextInt(NameHelper.prefixes.length)] + " " + name;
        }
        if (rand.nextFloat() < 0.8F && suffixes.length > 0) {
            name = String.format(suffixFormat, name, NameHelper.suffixes[rand.nextInt(NameHelper.suffixes.length)]);
        }
        entity.setCustomName(Component.literal(name));
        entity.setCustomNameVisible(true);
        return root;
    }

    /**
     * Applies a random name to an itemstack based on the item itself. An additional prefix is selected
     * from the item's material, detected by scanning the registry path for a recognizable material
     * fragment (e.g. {@code netherite_sword} → {@code netherite}).
     *
     * @param stack The stack to be named.
     * @return The name of the item, without the owning prefix of the boss's name.
     */
    public static Component setItemName(RandomSource random, ItemStack stack) {
        MutableComponent name = (MutableComponent) stack.getItem().getName(stack);
        String baseName = name.getString();
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        Tool tool = stack.get(DataComponents.TOOL);
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

        if (tool != null) {
            name = buildMaterialPrefix(random, path, baseName);

            String[] type = { "Tool" };
            if (stack.is(ItemTags.SWORDS) || stack.canPerformAction(ItemAbilities.SWORD_SWEEP)) {
                type = swords;
            }
            else if (stack.is(ItemTags.AXES) || stack.canPerformAction(ItemAbilities.AXE_STRIP)) {
                type = axes;
            }
            else if (stack.is(ItemTags.PICKAXES)) {
                type = pickaxes;
            }
            else if (stack.is(ItemTags.SHOVELS) || stack.canPerformAction(ItemAbilities.SHOVEL_FLATTEN)) {
                type = shovels;
            }
            else if (stack.has(DataComponents.BLOCKS_ATTACKS)) {
                type = shields;
            }
            name.append(type[random.nextInt(type.length)]);
        }
        else if (stack.getItem() instanceof ProjectileWeaponItem) {
            name = Component.literal(bows[random.nextInt(bows.length)]);
        }
        else if (equippable != null && equippable.slot() != EquipmentSlot.BODY) {
            name = buildMaterialPrefix(random, path, baseName);

            String[] type = { "Armor" };
            EquipmentSlot slot = equippable.slot();
            if (slot == EquipmentSlot.HEAD) {
                type = helms;
            }
            else if (slot == EquipmentSlot.CHEST) {
                type = chestplates;
            }
            else if (slot == EquipmentSlot.LEGS) {
                type = leggings;
            }
            else if (slot == EquipmentSlot.FEET) {
                type = boots;
            }
            name.append(type[random.nextInt(type.length)]);
        }

        stack.set(DataComponents.CUSTOM_NAME, name.withStyle(name.getStyle().withItalic(false)));
        return name;
    }

    private static MutableComponent buildMaterialPrefix(RandomSource random, String path, String baseName) {
        String[] matNames = findMaterialNames(path);
        if (matNames.length == 0) {
            return Component.literal(stripLastToken(baseName));
        }
        return Component.literal(matNames[random.nextInt(matNames.length)] + " ");
    }

    private static String[] findMaterialNames(String path) {
        for (Map.Entry<String, String[]> entry : materialNames.entrySet()) {
            if (path.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new String[0];
    }

    private static String stripLastToken(String baseName) {
        String[] split = baseName.split(" ");
        if (split.length <= 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            sb.append(split[i]).append(' ');
        }
        return sb.toString();
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

        Map<String, List<Item>> byMaterial = new LinkedHashMap<>();
        for (String key : materialNames.keySet()) {
            byMaterial.put(key, new ArrayList<>());
        }
        for (Item i : BuiltInRegistries.ITEM) {
            String path = BuiltInRegistries.ITEM.getKey(i).getPath();
            for (String key : byMaterial.keySet()) {
                if (path.contains(key)) {
                    byMaterial.get(key).add(i);
                    break;
                }
            }
        }
        for (Map.Entry<String, List<Item>> e : byMaterial.entrySet()) {
            String key = e.getKey();
            List<Item> items = e.getValue();
            String[] read = c.getStringList(key, "materials", materialNames.get(key), buildMaterialComment(items));
            if (read.length > 0) {
                materialNames.put(key, read);
            }
        }

        suffixFormat = c.getString("Suffix Format", "formatting", suffixFormat, "The format string that will be used when a suffix is applied.");
        ownershipFormat = c.getString("Ownership Format", "formatting", ownershipFormat, "The format string that will be used to indicate ownership.");

        if (c.hasChanged()) {
            c.save();
        }
    }

    private static String buildMaterialComment(List<Item> items) {
        String cmt = "A list of material-based prefix names for items whose registry path contains this fragment. May be empty.\n";
        if (items.isEmpty()) {
            return cmt + "No items currently match this fragment.\n";
        }
        cmt += "Matching items: ";
        cmt += items.stream().map(i -> BuiltInRegistries.ITEM.getKey(i).toString()).collect(Collectors.joining(", "));
        return cmt + "\n";
    }

}
