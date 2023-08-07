package dev.shadowsoffire.apotheosis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Generates names for various objects, based on stuff.
 *
 * @author Shadows
 */
public class NameHelper {

    /**
     * List of all possible full names.
     */
    private static String[] names = { "Prim", "Tyrael", "Bajorno", "Michael Morbius", "Morbius", "Arun", "Panez", "Doomsday", "Vanamar", "WhatTheDrunk",
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
        "Pig", "Sil", "Ver", "Fish", "Cow", "Chic", "Ken", "Sheep", "Squid", "Hell" };

    /**
     * List of prefixes, that are optionally applied to names.
     */
    private static String[] prefixes = { "Dr. Michael", "Sir", "Mister", "Madam", "Doctor", "Father", "Mother", "Poppa", "Lord", "Lady", "Overseer", "Professor",
        "Mr.", "Mr. President", "Duke", "Duchess", "Dame", "The Honorable", "Chancellor", "Vice-Chancellor", "His Holiness", "Reverend", "Count", "Viscount",
        "Earl", "Captain", "Major", "General", "Senpai" };

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
        "Cheese Sprayer", "Happiness Advocate", "Ghost Hunter", "Head of Potatoes", "Ninja", "Warrior", "Pyromancer"
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

    private static Map<Tier, String> tierKeys = new HashMap<>();
    private static Map<ArmorMaterial, String> materialKeys = new HashMap<>();

    /**
     * Array of descriptors for items based on tool material.
     */
    private static Map<String, String[]> tierNames = new HashMap<>();
    static {
        tierNames.put(Tiers.WOOD.name(), new String[] { "Wooden", "Wood", "Hardwood", "Balsa Wood", "Mahogany", "Plywood" });
        tierNames.put(Tiers.STONE.name(), new String[] { "Stone", "Rock", "Marble", "Cobblestone", });
        tierNames.put(Tiers.IRON.name(), new String[] { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" });
        tierNames.put(Tiers.GOLD.name(), new String[] { "Golden", "Gold", "Gilt", "Auric", "Ornate" });
        tierNames.put(Tiers.DIAMOND.name(), new String[] { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" });
        tierNames.put(Tiers.NETHERITE.name(), new String[] { "Burnt", "Embered", "Fiery", "Hellborn", "Flameforged" });
        tierNames.put("twilightforest_ironwood_sword", new String[] { "Ironwood", "Earthbound", "Oaken", "Ironcapped" });
        tierNames.put("twilightforest_knightmetal_sword", new String[] { "Knightmetal", "Knightly", "Phantom-Forged" });
        tierNames.put("twilightforest_steeleaf_sword", new String[] { "Steeleaf", "Organic", "Natural", "Cobaltstem", "Tungstenpetal" });
        tierNames.put("twilightforest_fiery_sword", new String[] { "Fiery", "Flaming", "Hydra-Infused", "Infernal" });
    }

    /**
     * Array of descriptors for items based on armor material.
     */
    private static Map<String, String[]> materialNames = new HashMap<>();
    static {
        materialNames.put(ArmorMaterials.LEATHER.name(), new String[] { "Leather", "Rawhide", "Lamellar", "Cow Skin" });
        materialNames.put(ArmorMaterials.CHAIN.name(), new String[] { "Chainmail", "Chain", "Chain Link", "Scale" });
        materialNames.put(ArmorMaterials.IRON.name(), tierNames.get(Tiers.IRON.name()));
        materialNames.put(ArmorMaterials.GOLD.name(), tierNames.get(Tiers.GOLD.name()));
        materialNames.put(ArmorMaterials.DIAMOND.name(), tierNames.get(Tiers.DIAMOND.name()));
        materialNames.put(ArmorMaterials.NETHERITE.name(), tierNames.get(Tiers.NETHERITE.name()));
        materialNames.put(ArmorMaterials.TURTLE.name(), new String[] { "Tortollan", "Very Tragic", "Environmental", "Organic" });
        materialNames.put("ARMOR_IRONWOOD", tierNames.get("twilightforest_ironwood_sword"));
        materialNames.put("ARMOR_KNIGHTLY", tierNames.get("twilightforest_knightmetal_sword"));
        materialNames.put("ARMOR_STEELEAF", tierNames.get("twilightforest_steeleaf_sword"));
        materialNames.put("ARMOR_FIERY", tierNames.get("twilightforest_fiery_sword"));
        materialNames.put("ARMOR_ARCTIC", new String[] { "Arctic", "Frostforged", "Caribou Skin", "Gutskin", "Insulating" });
        materialNames.put("ARMOR_YETI", new String[] { "Yeti", "Abominable", "Snow-Demon", "Grinch" });
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
     * The root name is either randomly selected from {@link NameHelper#names} or generated by {@link NameHelper#nameFromParts(Random)}
     * There is a 50% chance for a prefix to be selected from {@link NameHelper#prefixes}
     * There is a 80% chance for a suffix to be selected from {@link NameHelper#suffixes}
     *
     * @return The root name of the entity, without any prefixes or suffixes.
     */
    public static String setEntityName(RandomSource rand, Mob entity) {
        String root;

        if (names.length > 0 && nameParts.length > 0) {
            root = rand.nextFloat() < 0.8F ? NameHelper.names[rand.nextInt(NameHelper.names.length)] : NameHelper.nameFromParts(rand);
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
     * Applies a random name to an itemstack, based on the owning entity name, and the item itself.
     * An additional prefix will be selected based on the item type.
     * This is a best-guess system. One half of the name is based on the material, the other half is based on the item type.
     * The secondary half will fall back to the item display name, if what the item is cannot be inferred.
     *
     * @param stack The stack to be named.
     * @param name  The name of the owning entity, usually created by {@link NameHelper#setEntityName(Random, EntityLiving)}
     * @return The name of the item, without the owning prefix of the boss's name
     */
    public static Component setItemName(RandomSource random, ItemStack stack) {
        MutableComponent name = (MutableComponent) stack.getItem().getName(stack);
        String baseName = name.getString();

        if (stack.getItem() instanceof TieredItem) { // Tools or Weapons
            Tier tier = ((TieredItem) stack.getItem()).getTier();
            String[] tierNames = getTierNames(tier);
            if (tierNames.length == 0) {
                String[] split = baseName.split(" ");
                String rebuilt = "";
                for (int i = 0; i < split.length - 1; i++) {
                    rebuilt += split[i] + " ";
                }
                name = Component.literal(rebuilt);
            }
            else {
                name = Component.literal(tierNames[random.nextInt(tierNames.length)] + " ");
            }

            String[] type = { "Tool" };
            Set<ToolAction> types = ToolAction.getActions().stream().filter(stack::canPerformAction).collect(Collectors.toSet());

            if (stack.getItem() instanceof SwordItem) {
                type = swords;
            }
            else if (types.contains(ToolActions.AXE_DIG)) {
                type = axes;
            }
            else if (types.contains(ToolActions.PICKAXE_DIG)) {
                type = pickaxes;
            }
            else if (types.contains(ToolActions.SHOVEL_DIG)) {
                type = shovels;
            }
            else if (types.contains(ToolActions.SHIELD_BLOCK)) {
                type = shields;
            }
            name.append(type[random.nextInt(type.length)]);
        }
        else if (stack.getItem() instanceof ProjectileWeaponItem) { // Special Bow Handling
            String[] type = bows;
            name = Component.literal(type[random.nextInt(type.length)]);
        }
        else if (stack.getItem() instanceof ArmorItem) { // Armors
            ArmorMaterial armorMat = ((ArmorItem) stack.getItem()).getMaterial();
            String[] matNames = getMaterialNames(armorMat);
            if (matNames.length == 0) {
                String[] split = baseName.split(" ");
                String rebuilt = "";
                for (int i = 0; i < split.length - 1; i++) {
                    rebuilt += split[i] + " ";
                }
                name = Component.literal(rebuilt);
            }
            else {
                name = Component.literal(matNames[random.nextInt(matNames.length)] + " ");
            }

            String[] type = { "Armor" };
            switch (((ArmorItem) stack.getItem()).getEquipmentSlot()) {
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
            name.append(type[random.nextInt(type.length)]);
        }

        stack.setHoverName(name);
        return name;
    }

    public static String[] getTierNames(Tier materialName) {
        return tierNames.computeIfAbsent(getKey(materialName), s -> new String[0]);
    }

    public static String[] getMaterialNames(ArmorMaterial materialName) {
        return materialNames.computeIfAbsent(getKey(materialName), s -> new String[0]);
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

        Map<Tier, List<Item>> itemsByTier = new HashMap<>();
        Map<ArmorMaterial, List<Item>> armorsByTier = new HashMap<>();
        for (Item i : ForgeRegistries.ITEMS) {
            try {
                if (i instanceof TieredItem) {
                    Tier mat = ((TieredItem) i).getTier();
                    itemsByTier.computeIfAbsent(mat, m -> new ArrayList<>()).add(i);
                }
                if (i instanceof ArmorItem) {
                    ArmorMaterial mat = ((ArmorItem) i).getMaterial();
                    armorsByTier.computeIfAbsent(mat, m -> new ArrayList<>()).add(i);
                }
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("The item {} has thrown an exception while attempting to access it's tier.", ForgeRegistries.ITEMS.getKey(i));
                e.printStackTrace();
            }
        }

        for (Map.Entry<Tier, List<Item>> e : itemsByTier.entrySet()) {
            Tier tier = e.getKey();
            List<Item> items = e.getValue();
            String key = getID(tier, items);
            tierKeys.put(tier, key);
            String[] read = c.getStringList(key, "tools", tierNames.getOrDefault(tier, new String[0]), computeComment(items, tier::getRepairIngredient));
            if (read.length > 0) tierNames.put(key, read);
        }

        for (Map.Entry<ArmorMaterial, List<Item>> e : armorsByTier.entrySet()) {
            ArmorMaterial tier = e.getKey();
            List<Item> items = e.getValue();
            String key = getID(tier, items);
            String[] read = c.getStringList(key, "armors", materialNames.getOrDefault(tier, new String[0]), computeComment(items, tier::getRepairIngredient));
            if (read.length > 0) materialNames.put(key, read);
        }

        suffixFormat = c.getString("Suffix Format", "formatting", suffixFormat, "The format string that will be used when a suffix is applied.");
        ownershipFormat = c.getString("Ownership Format", "formatting", ownershipFormat, "The format string that will be used to indicate ownership.");

        if (c.hasChanged()) c.save();
    }

    private static String computeComment(List<Item> items, Supplier<Ingredient> repair) {
        String cmt = "A list of material-based prefix names for this material group. May be empty.\n";
        cmt += "Items in this group: ";
        for (Item i : items)
            cmt += ForgeRegistries.ITEMS.getKey(i) + ", ";
        cmt = cmt.substring(0, cmt.length() - 2);
        return cmt + "\n";
    }

    private static String getID(Object o, List<Item> items) {
        if (o instanceof Enum<?>) return ((Enum<?>) o).name();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(items.get(0));
        return id.getNamespace() + "_" + id.getPath();
    }

    private static String getKey(Tier tier) {
        return tierKeys.getOrDefault(tier, "");
    }

    private static String getKey(ArmorMaterial mat) {
        return materialKeys.getOrDefault(mat, "");
    }

}
