package dev.shadowsoffire.apotheosis.adventure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import dev.shadowsoffire.apotheosis.adventure.boss.BossEvents.BossSpawnRules;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraftforge.registries.ForgeRegistries;

public class AdventureConfig {

    public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
    public static final Map<ResourceLocation, LootCategory> TYPE_OVERRIDES = new HashMap<>();
    public static final Map<ResourceLocation, Pair<Float, BossSpawnRules>> BOSS_SPAWN_RULES = new HashMap<>();

    /**
     * These lists contain "loot table matchers" for the drop chances for loot tables.
     * Loot table matchers take the form of domain:pattern and the float chance is 0..1
     * Omitting the domain causes the pattern to be run for all domains.
     * The pattern is only run on the loot table's path.
     */
    public static final List<LootPatternMatcher> AFFIX_ITEM_LOOT_RULES = new ArrayList<>();
    public static final List<LootPatternMatcher> GEM_LOOT_RULES = new ArrayList<>();
    public static final Map<ResourceLocation, RarityClamp> GEM_DIM_RARITIES = new HashMap<>();

    /**
     * Loot table matchers and dimensional rarities for affix conversion rules.
     */
    public static final List<LootPatternMatcher> AFFIX_CONVERT_LOOT_RULES = new ArrayList<>();
    public static final Map<ResourceLocation, RarityClamp> AFFIX_CONVERT_RARITIES = new HashMap<>();

    // Boss Stats
    public static boolean curseBossItems = false;
    public static float bossAnnounceRange = 96;
    public static float bossAnnounceVolume = 0.75F;
    public static boolean bossAnnounceIgnoreY = false;
    public static int bossSpawnCooldown = 3600;
    public static boolean bossAutoAggro = false;
    public static boolean bossGlowOnSpawn = true;
    public static List<SoundEvent> bossAnnounceSounds = new ArrayList<>();

    // Generation
    public static float spawnerValueChance = 0.11F;

    // Affix
    public static float randomAffixItem = 0.075F;
    public static float gemDropChance = 0.045F;
    public static float gemBossBonus = 0.33F;
    public static boolean disableQuarkOnAffixItems = true;
    public static Supplier<Item> torchItem = () -> Items.TORCH;
    public static boolean cleaveHitsPlayers = false;

    public static void load(Configuration c) {
        c.setTitle("Apotheosis Adventure Module Config");

        TYPE_OVERRIDES.clear();
        TYPE_OVERRIDES.putAll(AdventureModule.IMC_TYPE_OVERRIDES);
        String[] overrides = c.getStringList("Equipment Type Overrides", "affixes", new String[] { "minecraft:iron_sword|sword", "minecraft:shulker_shell|none" },
            "A list of type overrides for the affix loot system.  Format is <itemname>|chance|<type>.\nValid types are: none, sword, trident, shield, heavy_weapon, pickaxe, shovel, crossbow, bow");
        for (String s : overrides) {
            String[] split = s.split("\\|");
            try {
                LootCategory type = LootCategory.byId(split[1].toLowerCase(Locale.ROOT));
                if (type.isArmor()) throw new UnsupportedOperationException("Cannot override an item to an armor type.");
                TYPE_OVERRIDES.put(new ResourceLocation(split[0]), type);
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("Invalid type override entry: " + s + " will be ignored!");
                e.printStackTrace();
            }
        }

        randomAffixItem = c.getFloat("Random Affix Chance", "affixes", randomAffixItem, 0, 1, "The chance that a naturally spawned mob will be granted an affix item. 0 = 0%, 1 = 100%");
        gemDropChance = c.getFloat("Gem Drop Chance", "affixes", gemDropChance, 0, 1, "The chance that a mob will drop a gem. 0 = 0%, 1 = 100%");
        gemBossBonus = c.getFloat("Gem Boss Bonus", "affixes", gemBossBonus, 0, 1, "The flat bonus chance that bosses have to drop a gem, added to Gem Drop Chance. 0 = 0%, 1 = 100%");
        cleaveHitsPlayers = c.getBoolean("Cleave Players", "affixes", cleaveHitsPlayers, "If affixes that cleave can hit players (excluding the user).");

        String[] lootRules = c.getStringList("Affix Item Loot Rules", "affixes", new String[] { "minecraft:chests.*|0.35", ".*chests.*|0.3", "twilightforest:structures.*|0.3" },

            """
                Loot Rules, in the form of Loot Table Matchers, permitting affix items to spawn in loot tables.
                The format for these is domain:pattern|chance and domain is optional.  Domain is a modid, pattern is a regex string, and chance is a float 0..1 chance for the item to spawn in any matched tables.
                If you omit the domain, the format is pattern|chance, and the matcher will run for all domains.
                The pattern MUST be a valid regex string, and should match the paths of desired loot tables under the specified domain.  Note: "Match Any Character" is ".*" (dot star) and not "*" (star).
                If there is a match, an item has a chance to spawn in that loot table.""");

        AFFIX_ITEM_LOOT_RULES.clear();
        for (String s : lootRules) {
            try {
                AFFIX_ITEM_LOOT_RULES.add(LootPatternMatcher.parse(s));
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("Invalid affix item loot rule: " + s + " will be ignored");
                e.printStackTrace();
            }
        }

        lootRules = c.getStringList("Gem Loot Rules", "gems", new String[] { "minecraft:chests.*|0.25", ".*chests.*|0.20", "twilightforest:structures.*|0.20" },
            "Loot Rules, in the form of Loot Table Matchers, permitting gems to spawn in loot tables.  See comment on \"Affix Item Loot Rules\" for description.");
        GEM_LOOT_RULES.clear();
        for (String s : lootRules) {
            try {
                GEM_LOOT_RULES.add(LootPatternMatcher.parse(s));
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("Invalid gem loot rule: " + s + " will be ignored");
                e.printStackTrace();
            }
        }

        lootRules = c.getStringList("Affix Convert Loot Rules", "affixes", new String[] { ".*blocks.*|0", ".*|0.35" },
            "Loot Rules, in the form of Loot Table Matchers, permitting affixes to be added to any valid item. Here, the chance refers to the chance an item receives affixes. See comment on \"Affix Item Loot Rules\" for description.");
        AFFIX_CONVERT_LOOT_RULES.clear();
        for (String s : lootRules) {
            try {
                AFFIX_CONVERT_LOOT_RULES.add(LootPatternMatcher.parse(s));
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("Invalid affix convert loot rule: " + s + " will be ignored");
                e.printStackTrace();
            }
        }

        String[] convertRarities = c.getStringList("Affix Convert Rarities", "affixes", new String[] { "overworld|common|rare", "the_nether|uncommon|epic", "the_end|rare|mythic", "twilightforest:twilight_forest|uncommon|epic" },
            "Dimensional rarities for affix conversion (see \"Affix Convert Loot Rules\"), in the form of dimension|min|max. A dimension not listed uses all rarities.");
        AFFIX_CONVERT_RARITIES.clear();
        for (String s : convertRarities) {
            try {
                String[] split = s.split("\\|");
                ResourceLocation dim = new ResourceLocation(split[0]);
                var min = RarityRegistry.byLegacyId(split[1]);
                var max = RarityRegistry.byLegacyId(split[2]);
                AFFIX_CONVERT_RARITIES.put(dim, new RarityClamp.Simple(min, max));
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("Invalid Affix Convert Rarity: " + s + " will be ignored");
                e.printStackTrace();
            }
        }

        String[] gemDimRarities = c.getStringList("Gem Dimensional Rarities", "gems", new String[] { "overworld|common|mythic", "the_nether|uncommon|mythic", "the_end|rare|mythic", "twilightforest:twilight_forest|uncommon|mythic" },
            "Dimensional rarities for gem drops, in the form of dimension|min|max. A dimension not listed uses all rarities.");
        GEM_DIM_RARITIES.clear();
        for (String s : gemDimRarities) {
            try {
                String[] split = s.split("\\|");
                ResourceLocation dim = new ResourceLocation(split[0]);
                var min = RarityRegistry.byLegacyId(split[1]);
                var max = RarityRegistry.byLegacyId(split[2]);
                GEM_DIM_RARITIES.put(dim, new RarityClamp.Simple(min, max));
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("Invalid Gem Dimensional Rarity: " + s + " will be ignored");
                e.printStackTrace();
            }
        }

        disableQuarkOnAffixItems = c.getBoolean("Disable Quark Tooltips for Affix Items", "affixes", true, "If Quark's Attribute Tooltip handling is disabled for affix items");

        String torch = c.getString("Torch Placement Item", "affixes", "minecraft:torch",
            "The item that will be used when attempting to place torches with the torch placer affix.  Must be a valid item that places a block on right click.");
        torchItem = () -> {
            try {
                Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(torch));
                return i == Items.AIR ? Items.TORCH : i;
            }
            catch (Exception ex) {
                AdventureModule.LOGGER.error("Invalid torch item {}", torch);
                return Items.TORCH;
            }
        };

        curseBossItems = c.getBoolean("Curse Boss Items", "bosses", curseBossItems, "If boss items are always cursed.  Enable this if you want bosses to be less overpowered by always giving them a negative effect.");
        bossAnnounceRange = c.getFloat("Boss Announce Range", "bosses", bossAnnounceRange, 0, 1024,
            "The range at which boss spawns will be announced.  If you are closer than this number of blocks (ignoring y-level), you will receive the announcement.");
        bossAnnounceVolume = c.getFloat("Boss Announce Volume", "bosses", bossAnnounceVolume, 0, 1, "The volume of the boss announcement sound. 0 to disable. This control is clientside.");
        bossAnnounceIgnoreY = c.getBoolean("Boss Announce Ignore Y", "bosses", bossAnnounceIgnoreY, "If the boss announcement range ignores y-level.");
        bossSpawnCooldown = c.getInt("Boss Spawn Cooldown", "bosses", bossSpawnCooldown, 0, 720000, "The time, in ticks, that must pass between any two natural boss spawns in a single dimension.");
        bossAutoAggro = c.getBoolean("Boss Auto-Aggro", "bosses", bossAutoAggro, "If true, invading bosses will automatically target the closest player.");
        bossGlowOnSpawn = c.getBoolean("Boss Glowing On Spawn", "bosses", bossGlowOnSpawn, "If true, bosses will glow when they spawn.");
        String[] bossAnnounce = c.getStringList("Boss Announce Sounds", "bosses",
                new String[] {
                        "block.end_portal.spawn"
                },
                "List of sound effects to play when boss spawns are announced. This control is clientside.");
        bossAnnounceSounds.clear();
        for (String s : bossAnnounce) {
            try {
                bossAnnounceSounds.add(SoundEvent.createVariableRangeEvent(new ResourceLocation(s.trim())));
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("Invalid boss announce sound: " + s + " will be ignored");
                e.printStackTrace();
            }
        }

        String[] dims = c.getStringList("Boss Spawn Dimensions", "bosses",
            new String[] {
                "minecraft:overworld|0.018|NEEDS_SKY",
                "minecraft:the_nether|0.025|ANY",
                "minecraft:the_end|0.018|SURFACE_OUTER_END",
                "twilightforest:twilight_forest|0.05|NEEDS_SURFACE"
            },
            "Dimensions where bosses can spawn naturally, spawn chance, and spawn rules.\nFormat is dimname|chance|rule, chance is a float from 0..1."
                + "\nValid rules are visible here https://github.com/Shadows-of-Fire/Apotheosis/blob/1.19/src/main/java/shadows/apotheosis/adventure/boss/BossEvents.java#L174C27-L174C27");

        BOSS_SPAWN_RULES.clear();
        for (String s : dims) {
            try {
                String[] split = s.split("\\|");
                BOSS_SPAWN_RULES.put(new ResourceLocation(split[0]), Pair.of(Float.parseFloat(split[1]), BossSpawnRules.valueOf(split[2].toUpperCase(Locale.ROOT))));
            }
            catch (Exception e) {
                AdventureModule.LOGGER.error("Invalid boss spawn rules: " + s + " will be ignored");
                e.printStackTrace();
            }
        }

        dims = c.getStringList("Generation Dimension Whitelist", "worldgen", new String[] { "overworld" }, "The dimensions that the deadly module will generate in.");
        DIM_WHITELIST.clear();
        for (String s : dims) {
            try {
                DIM_WHITELIST.add(new ResourceLocation(s.trim()));
            }
            catch (ResourceLocationException e) {
                AdventureModule.LOGGER.error("Invalid dim whitelist entry: " + s + " will be ignored");
            }
        }

        spawnerValueChance = c.getFloat("Spawner Value Chance", "spawners", spawnerValueChance, 0, 1, "The chance that a Rogue Spawner has a \"valuable\" chest instead of a standard one. 0 = 0%, 1 = 100%");
    }

    public static boolean canGenerateIn(WorldGenLevel world) {
        ResourceKey<Level> key = world.getLevel().dimension();
        return DIM_WHITELIST.contains(key.location());
    }

    public static record LootPatternMatcher(@Nullable String domain, Pattern pathRegex, float chance) {

        public boolean matches(ResourceLocation id) {
            return (this.domain == null || this.domain.equals(id.getNamespace())) && this.pathRegex.matcher(id.getPath()).matches();
        }

        public static LootPatternMatcher parse(String s) throws Exception {
            int pipe = s.lastIndexOf('|');
            int colon = s.indexOf(':');
            float chance = Float.parseFloat(s.substring(pipe + 1));
            String domain = colon == -1 ? null : s.substring(0, colon);
            Pattern pattern = Pattern.compile(s.substring(colon + 1, pipe));
            return new LootPatternMatcher(domain, pattern, chance);
        }
    }

}
