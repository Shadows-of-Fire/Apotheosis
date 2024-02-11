package dev.shadowsoffire.apotheosis.ench;

import dev.shadowsoffire.placebo.config.Configuration;

public class EnchConfig {

    public static boolean showEnchantedBookMetadata = true;
    public static int sculkShelfNoiseChance = 200;

    public static void load(Configuration c) {
        c.setTitle("Apotheosis Enchantment Module Config");

        showEnchantedBookMetadata = c.getBoolean("Show Enchanted Book Metadata", "client", true, "If enchanted book metadata (treasure, tradeable, etc) are shown in the tooltip.");
        sculkShelfNoiseChance = c.getInt("Sculkshelf Noise Chance", "client", 200, 0, 32767, "The 1/n chance that a sculkshelf plays a sound, per client tick. Set to 0 to disable.");

        if (c.hasChanged()) c.save();
    }

}
