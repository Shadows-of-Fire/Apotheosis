package dev.shadowsoffire.apotheosis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apoth.BuiltInRegs;
import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AdventureConfig {

    public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
    public static final Map<Item, LootCategory> TYPE_OVERRIDES = new HashMap<>(); // TODO: Turn this into a datamap or a collection of item tags.

    public static float augmentedMobChance = 0.075F;

    // Boss Stats
    public static boolean curseBossItems = false;
    public static float bossAnnounceRange = 96;
    public static float bossAnnounceVolume = 0.75F;
    public static boolean bossAnnounceIgnoreY = false;
    public static int bossSpawnCooldown = 3600;
    public static boolean bossAutoAggro = false;
    public static boolean bossGlowOnSpawn = true;

    // Generation
    public static float spawnerValueChance = 0.11F;

    // Affix
    public static float randomAffixItem = 0.075F;
    public static boolean disableQuarkOnAffixItems = true;
    public static Item torchItem = Items.TORCH;
    public static boolean cleaveHitsPlayers = false;

    // Wandering Trader
    public static boolean undergroundTrader = true;

    public static boolean charmsInCuriosOnly = false; // TODO: Hook this up.

    // Augmenting
    public static int upgradeSigilCost = 2;
    public static int upgradeLevelCost = 225;
    public static int rerollSigilCost = 1;
    public static int rerollLevelCost = 175;

    public static void load(Configuration c) {
        c.setTitle("Apotheosis Adventure Module Config");

        TYPE_OVERRIDES.clear();
        TYPE_OVERRIDES.putAll(Apotheosis.IMC_TYPE_OVERRIDES);
        String[] overrides = c.getStringList("Equipment Type Overrides", "affixes", new String[] { "minecraft:iron_sword|apotheosis:melee_weapon", "minecraft:shulker_shell|apotheosis:none" },
            "A list of type overrides for the affix loot system.  Format is <itemname>|<type>.\nValid types are: none, melee_weapon, trident, shield, breaker, bow\nSynced.");
        for (String s : overrides) {
            String[] split = s.split("\\|");
            try {
                ResourceLocation key = ResourceLocation.parse(split[1].toLowerCase(Locale.ROOT));
                LootCategory type = BuiltInRegs.LOOT_CATEGORY.get(key);
                if (type.isArmor()) {
                    throw new UnsupportedOperationException("Cannot override an item to an armor type.");
                }

                if (type.isNone() && !LootCategories.NONE.getKey().equals(key)) {
                    throw new UnsupportedOperationException("Unknown loot category: " + key);
                }

                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(split[0]));
                if (item == Items.AIR) {
                    throw new UnsupportedOperationException("Unknown item: " + split[0]);
                }

                TYPE_OVERRIDES.put(item, type);
            }
            catch (Exception e) {
                Apotheosis.LOGGER.error("Invalid type override entry: " + s + " will be ignored!");
                e.printStackTrace();
            }
        }

        randomAffixItem = c.getFloat("Random Affix Chance", "affixes", randomAffixItem, 0, 1, "The chance that a naturally spawned mob will be granted an affix item. 0 = 0%, 1 = 100%\nServer-authoritative.");
        cleaveHitsPlayers = c.getBoolean("Cleave Players", "affixes", cleaveHitsPlayers, "If affixes that cleave can hit players (excluding the user).\nServer-authoritative.");

        disableQuarkOnAffixItems = c.getBoolean("Disable Quark Tooltips for Affix Items", "affixes", true, "If Quark's Attribute Tooltip handling is disabled for affix items.\nClientside.");

        String torch = c.getString("Torch Placement Item", "affixes", "minecraft:torch",
            "The item that will be used when attempting to place torches with the torch placer affix.  Must be a valid item that places a block on right click.\nSynced.");

        try {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(torch));
            if (item == Items.AIR) {
                throw new UnsupportedOperationException("Unknown item: " + torch);
            }
            torchItem = item;
        }
        catch (Exception ex) {
            Apotheosis.LOGGER.error("Invalid torch item {}", torch);
            torchItem = Items.TORCH;
        }

        curseBossItems = c.getBoolean("Curse Boss Items", "bosses", curseBossItems,
            "If boss items are always cursed.  Enable this if you want bosses to be less overpowered by always giving them a negative effect.\nServer-authoritative.");
        bossAnnounceRange = c.getFloat("Boss Announce Range", "bosses", bossAnnounceRange, 0, 1024,
            "The range at which boss spawns will be announced.  If you are closer than this number of blocks (ignoring y-level), you will receive the announcement.\nServer-authoritative.");
        bossAnnounceVolume = c.getFloat("Boss Announce Volume", "bosses", bossAnnounceVolume, 0, 1, "The volume of the boss announcement sound. 0 to disable.\nClientside.");
        bossAnnounceIgnoreY = c.getBoolean("Boss Announce Ignore Y", "bosses", bossAnnounceIgnoreY, "If the boss announcement range ignores y-level.\nServer-authoritative.");
        bossSpawnCooldown = c.getInt("Boss Spawn Cooldown", "bosses", bossSpawnCooldown, 0, 720000, "The time, in ticks, that must pass between any two natural boss spawns in a single dimension.\nServer-authoritative.");
        bossAutoAggro = c.getBoolean("Boss Auto-Aggro", "bosses", bossAutoAggro, "If true, invading bosses will automatically target the closest player.\nServer-authoritative.");
        bossGlowOnSpawn = c.getBoolean("Boss Glowing On Spawn", "bosses", bossGlowOnSpawn, "If true, bosses will glow when they spawn.\nServer-authoritative.");

        String[] dims = c.getStringList("Generation Dimension Whitelist", "worldgen", new String[] { "overworld" }, "The dimensions that the deadly module will generate in.\nServer-authoritative.");
        DIM_WHITELIST.clear();
        for (String s : dims) {
            try {
                DIM_WHITELIST.add(ResourceLocation.parse(s.trim()));
            }
            catch (ResourceLocationException e) {
                Apotheosis.LOGGER.error("Invalid dim whitelist entry: " + s + " will be ignored");
            }
        }

        spawnerValueChance = c.getFloat("Spawner Value Chance", "spawners", spawnerValueChance, 0, 1, "The chance that a Rogue Spawner has a \"valuable\" chest instead of a standard one. 0 = 0%, 1 = 100%\nServer-authoritative.");

        undergroundTrader = c.getBoolean("Underground Trader", "wanderer", undergroundTrader, "If the Wandering Trader can attempt to spawn underground.\nServer-authoritative.");

        upgradeSigilCost = c.getInt("Upgrade Sigil Cost", "augmenting", upgradeSigilCost, 0, 64, "The number of Sigils of Enhancement it costs to upgrade an affix in the Augmenting Table.\nSynced.");
        upgradeLevelCost = c.getInt("Upgrade Level Cost", "augmenting", upgradeLevelCost, 0, 65536, "The number of experience levels it costs to upgrade an affix in the Augmenting Table.\nSynced.");
        rerollSigilCost = c.getInt("Reroll Sigil Cost", "augmenting", rerollSigilCost, 0, 64, "The number of Sigils of Enhancement it costs to reroll an affix in the Augmenting Table.\nSynced.");
        rerollLevelCost = c.getInt("Reroll Level Cost", "augmenting", rerollLevelCost, 0, 65536, "The number of experience levels it costs to reroll an affix in the Augmenting Table.\nSynced.");
    }

    public static boolean canGenerateIn(WorldGenLevel world) {
        ResourceKey<Level> key = world.getLevel().dimension();
        return DIM_WHITELIST.contains(key.location());
    }

    public static record ConfigPayload(Map<Item, LootCategory> catOverrides, Item affixTorch, int upgradeSigilCost, int upgradeLevelCost, int rerollSigilCost, int rerollLevelCost) implements CustomPacketPayload {

        public static final Type<ConfigPayload> TYPE = new Type<>(Apotheosis.loc("config"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.ITEM), LootCategory.STREAM_CODEC), ConfigPayload::catOverrides,
            ByteBufCodecs.registry(Registries.ITEM), ConfigPayload::affixTorch,
            ByteBufCodecs.VAR_INT, ConfigPayload::upgradeSigilCost,
            ByteBufCodecs.VAR_INT, ConfigPayload::upgradeLevelCost,
            ByteBufCodecs.VAR_INT, ConfigPayload::rerollSigilCost,
            ByteBufCodecs.VAR_INT, ConfigPayload::rerollLevelCost,
            ConfigPayload::new);

        public ConfigPayload() {
            this(AdventureConfig.TYPE_OVERRIDES, AdventureConfig.torchItem, AdventureConfig.upgradeSigilCost, AdventureConfig.upgradeLevelCost, AdventureConfig.rerollSigilCost, AdventureConfig.rerollLevelCost);
        }

        @Override
        public Type<ConfigPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<ConfigPayload> {

            @Override
            public Type<ConfigPayload> getType() {
                return TYPE;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ConfigPayload> getCodec() {
                return CODEC;
            }

            @Override
            public void handle(ConfigPayload msg, IPayloadContext ctx) {
                AdventureConfig.TYPE_OVERRIDES.clear();
                AdventureConfig.TYPE_OVERRIDES.putAll(msg.catOverrides);
                AdventureConfig.torchItem = msg.affixTorch();
                AdventureConfig.upgradeSigilCost = msg.upgradeSigilCost;
                AdventureConfig.upgradeLevelCost = msg.upgradeLevelCost;
                AdventureConfig.rerollSigilCost = msg.rerollSigilCost;
                AdventureConfig.rerollLevelCost = msg.rerollLevelCost;
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

            @Override
            public String getVersion() {
                return "2";
            }

        }

    }

}
