package dev.shadowsoffire.apotheosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.AdventureConfig.ConfigPayload;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.compat.PatchouliCompat;
import dev.shadowsoffire.apotheosis.compat.curios.CuriosCompat;
// import dev.shadowsoffire.apotheosis.compat.curios.CuriosCompat; // Disabled: Curios has no 26.1 build yet
import dev.shadowsoffire.apotheosis.compat.gateways.GatewaysCompat;
// import dev.shadowsoffire.apotheosis.compat.twilight.AdventureTwilightCompat; // Disabled: Twilight Forest has no 26.1 build yet
import dev.shadowsoffire.apotheosis.data.AffixLootEntryProvider;
import dev.shadowsoffire.apotheosis.data.AffixProvider;
import dev.shadowsoffire.apotheosis.data.ApothAdvancementProvider;
import dev.shadowsoffire.apotheosis.data.ApothDataMapProvider;
import dev.shadowsoffire.apotheosis.data.ApothLootProvider;
import dev.shadowsoffire.apotheosis.data.ApothPaintingTagsProvider;
import dev.shadowsoffire.apotheosis.data.ApothPaintingsProvider;
import dev.shadowsoffire.apotheosis.data.ApothRecipeProvider;
import dev.shadowsoffire.apotheosis.data.AugmentationProvider;
import dev.shadowsoffire.apotheosis.data.EliteProvider;
import dev.shadowsoffire.apotheosis.data.GLMProvider;
import dev.shadowsoffire.apotheosis.data.GearSetProvider;
import dev.shadowsoffire.apotheosis.data.GearSetTagsProvider;
import dev.shadowsoffire.apotheosis.data.GemProvider;
import dev.shadowsoffire.apotheosis.data.InvaderProvider;
import dev.shadowsoffire.apotheosis.data.PurityWeightsProvider;
import dev.shadowsoffire.apotheosis.data.RarityOverrideProvider;
import dev.shadowsoffire.apotheosis.data.RarityProvider;
import dev.shadowsoffire.apotheosis.data.RogueSpawnerProvider;
import dev.shadowsoffire.apotheosis.data.SongProvider;
import dev.shadowsoffire.apotheosis.data.TierAugmentProvider;
import dev.shadowsoffire.apotheosis.data.WandererTradesProvider;
import dev.shadowsoffire.apotheosis.data.gateways.ApothGateProvider;
// import dev.shadowsoffire.apotheosis.data.twilight.TwilightAffixLootProvider; // Disabled: Twilight Forest has no 26.1 build yet
// import dev.shadowsoffire.apotheosis.data.twilight.TwilightGearSetProvider; // Disabled: Twilight Forest has no 26.1 build yet
// import dev.shadowsoffire.apotheosis.data.twilight.TwilightInvaderProvider; // Disabled: Twilight Forest has no 26.1 build yet
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootRule;
import dev.shadowsoffire.apotheosis.loot.RarityOverrideRegistry;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.ApothMobEvents;
import dev.shadowsoffire.apotheosis.mobs.registries.AugmentRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.util.EntityModifier;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload;
import dev.shadowsoffire.apotheosis.net.GemCaseSelectPayload;
import dev.shadowsoffire.apotheosis.net.LinkItemToChatPayload;
import dev.shadowsoffire.apotheosis.net.RadialStatePayload;
import dev.shadowsoffire.apotheosis.net.RerollResultPayload;
import dev.shadowsoffire.apotheosis.net.WorldTierPayload;
import dev.shadowsoffire.apotheosis.socket.gem.ExtraGemBonusRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.PurityWeightsRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTile;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawnerRegistry;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.datagen.DataGenBuilder;
import dev.shadowsoffire.placebo.datagen.RegisterFieldOrderingsEvent;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import dev.shadowsoffire.placebo.util.RunnableReloader;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

@Mod(Apotheosis.MODID)
public class Apotheosis {

    public static final String MODID = "apotheosis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final boolean DEBUG_WORLDGEN = "on".equalsIgnoreCase(System.getenv("APOTH_DEBUG_WORLDGEN"));
    public static final boolean DEBUG_MOBS = "on".equalsIgnoreCase(System.getenv("APOTH_DEBUG_MOBS"));
    public static final boolean STAGES_LOADED = ModList.get().isLoaded("gamestages");

    public Apotheosis(IEventBus bus) {
        Apoth.bootstrap(bus);
        bus.register(this);
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR.value(), 200D, "maxValue");
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR_TOUGHNESS.value(), 100D, "maxValue");
        LootRule.initCodecs();
        SpawnCondition.initCodecs();
        EntityModifier.initCodecs();
        GemBonus.initCodecs();
        if (ModList.get().isLoaded("gateways")) {
            GatewaysCompat.register(bus);
        }

        // TODO(26.1): Restore Twilight/Patchouli/Curios compat once their 26.1 builds ship.
        if (ModList.get().isLoaded("twilightforest")) {
            // AdventureTwilightCompat.register();
        }

        if (ModList.get().isLoaded("patchouli")) {
            PatchouliCompat.register();
        }

        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.register(bus);
        }
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            TabFillingRegistry.register(Apoth.Tabs.ADVENTURE.getKey(),
                Items.COMMON_MATERIAL, Items.UNCOMMON_MATERIAL, Items.RARE_MATERIAL, Items.EPIC_MATERIAL, Items.MYTHIC_MATERIAL, Items.GEM_DUST, Items.GEM_FUSED_SLATE,
                Items.SIGIL_OF_SOCKETING, Items.SIGIL_OF_WITHDRAWAL, Items.SIGIL_OF_REBIRTH, Items.SIGIL_OF_ENHANCEMENT, Items.SIGIL_OF_UNNAMING, Items.SIGIL_OF_MALICE, Items.SIGIL_OF_SUPREMACY,
                Items.SALVAGING_TABLE, Items.GEM_CUTTING_TABLE, Items.SIMPLE_REFORGING_TABLE, Items.REFORGING_TABLE, Items.AUGMENTING_TABLE, Items.GEM_CASE, Items.ENDER_GEM_CASE,
                Items.IRON_UPGRADE_SMITHING_TEMPLATE, Items.GOLD_UPGRADE_SMITHING_TEMPLATE, Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE,
                Items.MUSIC_DISC_FLASH, Items.MUSIC_DISC_GLIMMER, Items.MUSIC_DISC_SHIMMER,
                Items.SPAWNER_CHAIN, Items.SPAWNER_RUNE, Items.INFUSED_SPAWNER_RUNE,
                Items.FRONTIER_SPAWNER_UPGRADE_RUNE, Items.ASCENT_SPAWNER_UPGRADE_RUNE, Items.SUMMIT_SPAWNER_UPGRADE_RUNE, Items.PINNACLE_SPAWNER_UPGRADE_RUNE,
                Items.SPAWN_RANGE_SPAWNER_RUNE, Items.REDSTONE_CONTROL_SPAWNER_RUNE, Items.IGNORE_LIGHT_SPAWNER_RUNE, Items.INITIAL_HEALTH_SPAWNER_RUNE,
                Items.SILENT_SPAWNER_RUNE, Items.YOUTHFUL_SPAWNER_RUNE, Items.BURNING_SPAWNER_RUNE, Items.NO_AI_SPAWNER_RUNE,
                Items.IGNORE_CONDITIONS_SPAWNER_RUNE, Items.IGNORE_PLAYERS_SPAWNER_RUNE, Items.ECHOING_SPAWNER_RUNE,
                Items.GEM, // Gem is at the end because it also generates all the dynamic variants.
                Items.BOSS_SUMMONER // Except this stupid little creature
            );

            TabFillingRegistry.register(CreativeModeTabs.FOOD_AND_DRINKS, Items.POTION_CHARM);
        });
        PayloadHelper.registerPayload(new BossSpawnPayload.Provider());
        PayloadHelper.registerPayload(new RerollResultPayload.Provider());
        PayloadHelper.registerPayload(new RadialStatePayload.Provider());
        PayloadHelper.registerPayload(new WorldTierPayload.Provider());
        PayloadHelper.registerPayload(new ConfigPayload.Provider());
        PayloadHelper.registerPayload(new LinkItemToChatPayload.Provider());
        PayloadHelper.registerPayload(new GemCaseSelectPayload.Provider());
        NeoForge.EVENT_BUS.register(new AdventureEvents());
        NeoForge.EVENT_BUS.register(new ApothMobEvents());
        RarityRegistry.INSTANCE.registerToBus();
        RarityOverrideRegistry.INSTANCE.registerToBus();
        AffixRegistry.INSTANCE.registerToBus();
        ExtraGemBonusRegistry.INSTANCE.registerToBus();
        GemRegistry.INSTANCE.registerToBus();
        AffixLootRegistry.INSTANCE.registerToBus();
        InvaderRegistry.INSTANCE.registerToBus();
        RogueSpawnerRegistry.INSTANCE.registerToBus();
        EliteRegistry.INSTANCE.registerToBus();
        PurityWeightsRegistry.INSTANCE.registerToBus();
        AugmentRegistry.INSTANCE.registerToBus();
        TierAugmentRegistry.INSTANCE.registerToBus();
        loadConfig(true);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event -> event.addListener(Apotheosis.loc("config_reloader"), RunnableReloader.of(() -> loadConfig(false))));
    }

    @SubscribeEvent
    public void caps(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.Item.BLOCK, Apoth.Tiles.SALVAGING_TABLE, (be, side) -> be.getItemHandler());
        e.registerBlockEntity(Capabilities.Item.BLOCK, Apoth.Tiles.REFORGING_TABLE, (be, side) -> be.getInventory());
        e.registerBlockEntity(Capabilities.Item.BLOCK, Apoth.Tiles.AUGMENTING_TABLE, (be, side) -> be.getInventory());
        e.registerBlockEntity(Capabilities.Item.BLOCK, Apoth.Tiles.GEM_CASE, GemCaseTile::getItemHandler);
        e.registerBlockEntity(Capabilities.Item.BLOCK, Apoth.Tiles.ENDER_GEM_CASE, GemCaseTile::getItemHandler);
    }

    @SubscribeEvent
    public void data(GatherDataEvent.Client e) {
        DataProvider.INDENT_WIDTH.set(4);
        DataGenBuilder.create(Apotheosis.MODID)
            .registry(Registries.JUKEBOX_SONG, SongProvider::bootstrap)
            .registry(Registries.PAINTING_VARIANT, ApothPaintingsProvider::bootstrap)
            .registry(Registries.VILLAGER_TRADE, WandererTradesProvider::bootstrap)
            .provider(ApothLootProvider::create)
            .provider(ApothRecipeProvider::new)
            .provider(ApothPaintingTagsProvider::new)
            .provider(RarityProvider::new)
            .provider(RarityOverrideProvider::new)
            .provider(AffixLootEntryProvider::new)
            .provider(AffixProvider::new)
            .provider(GemProvider::new)
            .provider(GLMProvider::new)
            .provider(GearSetProvider::new)
            .provider(GearSetTagsProvider::new)
            .provider(PurityWeightsProvider::new)
            .provider(InvaderProvider::new)
            .provider(EliteProvider::new)
            .provider(ApothAdvancementProvider::create)
            .provider(TierAugmentProvider::new)
            .provider(RogueSpawnerProvider::new)
            // .provider(TwilightAffixLootProvider::new) // Disabled: Twilight Forest has no 26.1 build yet
            // .provider(TwilightGearSetProvider::new) // Disabled: Twilight Forest has no 26.1 build yet
            // .provider(TwilightInvaderProvider::new) // Disabled: Twilight Forest has no 26.1 build yet
            .provider(ApothDataMapProvider::new)
            .provider(AugmentationProvider::new)
            .provider(ApothGateProvider::new)
            .build(e);
    }

    @SubscribeEvent
    public void fieldOrder(RegisterFieldOrderingsEvent e) {
        // TODO: Convert this to use the new APIs properly. Requires figuring out how these should be scoped.
        Object2IntOpenHashMap<String> map = (Object2IntOpenHashMap<String>) DataProvider.FIXED_ORDER_FIELDS;
        // Keep enums in ordinal order
        for (Purity p : Purity.values()) {
            map.put(p.getSerializedName(), p.ordinal() + 1);
        }

        for (WorldTier t : WorldTier.values()) {
            map.put(t.getSerializedName(), t.ordinal() + 1);
        }

        // Place min/max in order
        map.put("min", 1);
        map.put("max", 2);

        // Rarity values
        map.put("apotheosis:common", 1);
        map.put("apotheosis:uncommon", 2);
        map.put("apotheosis:rare", 3);
        map.put("apotheosis:epic", 4);
        map.put("apotheosis:mythic", 5);

        // Force duration above amp/cooldown for mob effect affixes
        map.put("duration", 1);

        // Place gem bonus lists below everything else in the gem file.
        map.put("bonuses", 5);
    }

    public static void loadConfig(boolean firstLoad) {
        Configuration mainConfig = new Configuration(ApothicAttributes.getConfigFile(MODID));
        Configuration nameConfig = new Configuration(ApothicAttributes.getConfigFile("name_generation"));
        AdventureConfig.load(mainConfig);
        NameHelper.load(nameConfig);
        if (firstLoad && mainConfig.hasChanged()) {
            mainConfig.save();
        }
        if (firstLoad && nameConfig.hasChanged()) {
            nameConfig.save();
        }
    }

    /**
     * Constructs a resource location using the {@link Apotheosis#MODID} as the namespace.
     */
    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    /**
     * Constructs a mutable component with a lang key of the form "type.modid.path", using {@link Apotheosis#MODID}.
     *
     * @param type The type of language key, "misc", "info", "title", etc...
     * @param path The path of the language key.
     * @param args Translation arguments passed to the created translatable component.
     */
    public static MutableComponent lang(String type, String path, Object... args) {
        return Component.translatable(langKey(type, path), args);
    }

    public static String langKey(String type, String path) {
        return type + "." + MODID + "." + path;
    }

    public static MutableComponent sysMessageHeader() {
        return Component.translatable("[%s] ", Component.literal("Apoth").withStyle(ChatFormatting.GOLD));
    }

    public static void debugLog(BlockPos pos, String name) {
        if (DEBUG_WORLDGEN) {
            Apotheosis.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
        }
    }

}
