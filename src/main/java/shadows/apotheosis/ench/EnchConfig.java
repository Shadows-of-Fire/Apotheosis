package shadows.apotheosis.ench;

import shadows.placebo.config.Configuration;

public class EnchConfig {

    public static boolean showEnchantedBookMetadata = true;

    public static void load(Configuration c) {
        c.setTitle("Apotheosis Enchantment Module Config");

        showEnchantedBookMetadata = c.getBoolean("Show Enchanted Book Metadata", "tooltips", true, "If enchanted book metadata (treasure, tradeable, etc) are shown in the tooltip.");

        if (c.hasChanged()) c.save();
    }

}