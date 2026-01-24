package dev.shadowsoffire.apotheosis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AdventureConfig {

    public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();

    // Boss Stats
    public static boolean curseBossItems = false;
    public static float bossAnnounceRange = NaturalSpawner.SPAWN_DISTANCE_BLOCK + 12;
    public static int bossSpawnCooldown = 3600;
    public static boolean bossAutoAggro = false;
    public static boolean bossGlowOnSpawn = true;

    // Generation
    public static float spawnerValueChance = 0.11F;

    // Affix
    public static boolean disableQuarkOnAffixItems = true;
    public static Item torchItem = Items.TORCH;
    public static boolean cleaveHitsPlayers = false;

    // Wandering Trader
    public static boolean undergroundTrader = true;

    public static boolean charmsInCuriosOnly = false;

    // Augmenting
    public static int upgradeSigilCost = 2;
    public static int upgradeLevelCost = 225;
    public static int rerollSigilCost = 1;
    public static int rerollLevelCost = 175;

    public static boolean enableItemLinking = true;
    public static int itemLinkingCooldown = 100;
    public static boolean enableAffixItemEffects = true;

    public static boolean enableEquipmentCompare = true;

    public static boolean enableManualWorldTierChanges = true;

    public static void load(Configuration c) {
        c.setTitle("Apotheosis Adventure Module Config");

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
        bossAnnounceRange = c.getFloat("Boss Announcement Range", "bosses", bossAnnounceRange, 0, 1024,
            "The range at which boss spawns will be announced.  If you are closer than this number of blocks (ignoring y-level), you will receive the announcement.\nServer-authoritative.");
        bossSpawnCooldown = c.getInt("Boss Spawn Cooldown", "bosses", bossSpawnCooldown, 0, 720000, "The time, in ticks, that must pass between any two natural boss spawns in a single dimension.\nServer-authoritative.");
        bossAutoAggro = c.getBoolean("Boss Auto-Aggro", "bosses", bossAutoAggro, "If true, invading bosses will automatically target the closest player.\nServer-authoritative.");
        bossGlowOnSpawn = c.getBoolean("Boss Glowing On Spawn", "bosses", bossGlowOnSpawn, "If true, bosses will glow when they spawn.\nServer-authoritative.");

        String[] dims = c.getStringList("Generation Dimension Whitelist", "worldgen", new String[] { "overworld" }, "The dimensions that Apotheosis's worldgen will generate in.\nServer-authoritative.");
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

        charmsInCuriosOnly = c.getBoolean("Restrict Charms to Curios", "potion_charms", charmsInCuriosOnly, "If Potion Charms will only work when in a curios slot, instead of in the inventory.");

        enableItemLinking = c.getBoolean("Enable Item Linking", "quality_of_life", enableItemLinking, "If linking items to chat (via the hotkey) is enabled.\nServer-Authoritative.");
        itemLinkingCooldown = c.getInt("Item Linking Cooldown", "quality_of_life", itemLinkingCooldown, 0, 65536, "The cooldown, in ticks, between player item links.\nServer-Authoritative.");
        enableEquipmentCompare = c.getBoolean("Enable Equipment Comparisons", "quality_of_life", enableEquipmentCompare, "If equipment comparison popups are enabled when the hotkey is held.\nClientside.");
        enableAffixItemEffects = c.getBoolean("Enable Affix Item Effects", "flair", enableAffixItemEffects, "If affix item effects (custom shadows, beams, particles, etc) are enabled.\nClientside.");

        enableManualWorldTierChanges = c.getBoolean("Enable Manual World Tier Changes", "world_tiers", enableManualWorldTierChanges,
            "If players can change their world tier manually in the World Tier Selection Screen.\nNote: Disabling this does NOT automatically change world tiers when unlocked. You will need to set that up yourself.\nServer-Authoritative.");
    }

    public static boolean canGenerateIn(WorldGenLevel world) {
        ResourceKey<Level> key = world.getLevel().dimension();
        return DIM_WHITELIST.contains(key.location());
    }

    public static record ConfigPayload(Item affixTorch, int upgradeSigilCost, int upgradeLevelCost, int rerollSigilCost, int rerollLevelCost, boolean charmsInCuriosOnly, boolean manualWorldTierChanges) implements CustomPacketPayload {

        public static final Type<ConfigPayload> TYPE = new Type<>(Apotheosis.loc("config"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ConfigPayload> CODEC = NeoForgeStreamCodecs.composite(
            ByteBufCodecs.registry(Registries.ITEM), ConfigPayload::affixTorch,
            ByteBufCodecs.VAR_INT, ConfigPayload::upgradeSigilCost,
            ByteBufCodecs.VAR_INT, ConfigPayload::upgradeLevelCost,
            ByteBufCodecs.VAR_INT, ConfigPayload::rerollSigilCost,
            ByteBufCodecs.VAR_INT, ConfigPayload::rerollLevelCost,
            ByteBufCodecs.BOOL, ConfigPayload::charmsInCuriosOnly,
            ByteBufCodecs.BOOL, ConfigPayload::manualWorldTierChanges,
            ConfigPayload::new);

        public ConfigPayload() {
            this(AdventureConfig.torchItem, AdventureConfig.upgradeSigilCost, AdventureConfig.upgradeLevelCost, AdventureConfig.rerollSigilCost, AdventureConfig.rerollLevelCost, AdventureConfig.charmsInCuriosOnly,
                AdventureConfig.enableManualWorldTierChanges);
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
                AdventureConfig.torchItem = msg.affixTorch();
                AdventureConfig.upgradeSigilCost = msg.upgradeSigilCost;
                AdventureConfig.upgradeLevelCost = msg.upgradeLevelCost;
                AdventureConfig.rerollSigilCost = msg.rerollSigilCost;
                AdventureConfig.rerollLevelCost = msg.rerollLevelCost;
                AdventureConfig.enableManualWorldTierChanges = msg.manualWorldTierChanges;
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
                return "4";
            }

        }

    }

}
