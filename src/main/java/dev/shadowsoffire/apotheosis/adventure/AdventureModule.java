package dev.shadowsoffire.apotheosis.adventure;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisConstruction;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisReloadEvent;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingMenu;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingTableBlock;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingTableTile;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvageItem;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingMenu;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingTableBlock;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingTableTile;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.ExpulsionRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.ExtractionRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingBlock;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingMenu;
import dev.shadowsoffire.apotheosis.adventure.boss.BossEvents;
import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.adventure.boss.BossSpawnerBlock;
import dev.shadowsoffire.apotheosis.adventure.boss.BossSpawnerBlock.BossSpawnerTile;
import dev.shadowsoffire.apotheosis.adventure.boss.BossSummonerItem;
import dev.shadowsoffire.apotheosis.adventure.boss.Exclusion;
import dev.shadowsoffire.apotheosis.adventure.boss.GearSetRegistry;
import dev.shadowsoffire.apotheosis.adventure.boss.MinibossRegistry;
import dev.shadowsoffire.apotheosis.adventure.client.AdventureModuleClient;
import dev.shadowsoffire.apotheosis.adventure.compat.AdventureTOPPlugin;
import dev.shadowsoffire.apotheosis.adventure.compat.AdventureTwilightCompat;
import dev.shadowsoffire.apotheosis.adventure.compat.GatewaysCompat;
import dev.shadowsoffire.apotheosis.adventure.gen.BossDungeonFeature;
import dev.shadowsoffire.apotheosis.adventure.gen.BossDungeonFeature2;
import dev.shadowsoffire.apotheosis.adventure.gen.ItemFrameGemsProcessor;
import dev.shadowsoffire.apotheosis.adventure.gen.RogueSpawnerFeature;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixConvertLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixHookLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootPoolEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.GemLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.GemLootPoolEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.spawner.RogueSpawnerRegistry;
import dev.shadowsoffire.apotheosis.ench.objects.GlowyBlockItem.GlowyItem;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.loot.LootSystem;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.registry.RegistryEvent.Register;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public class AdventureModule {

    public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Adventure");
    public static final boolean STAGES_LOADED = ModList.get().isLoaded("gamestages");

    public static final StructureProcessorType<ItemFrameGemsProcessor> ITEM_FRAME_LOOT = () -> ItemFrameGemsProcessor.CODEC;

    @SubscribeEvent
    public void preInit(ApotheosisConstruction e) {
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR, 200D, "f_22308_");
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR_TOUGHNESS, 100D, "f_22308_");
    }

    @SubscribeEvent
    public void init(FMLCommonSetupEvent e) {
        this.reload(null);
        MinecraftForge.EVENT_BUS.register(new AdventureEvents());
        MinecraftForge.EVENT_BUS.register(new BossEvents());
        MinecraftForge.EVENT_BUS.addListener(this::reload);
        RarityRegistry.INSTANCE.registerToBus();
        AffixRegistry.INSTANCE.registerToBus();
        GemRegistry.INSTANCE.registerToBus();
        GearSetRegistry.INSTANCE.registerToBus();
        AffixLootRegistry.INSTANCE.registerToBus();
        BossRegistry.INSTANCE.registerToBus();
        RogueSpawnerRegistry.INSTANCE.registerToBus();
        MinibossRegistry.INSTANCE.registerToBus();
        Apotheosis.HELPER.registerProvider(f -> {
            f.addRecipe(new SocketingRecipe());
            f.addRecipe(new ExpulsionRecipe());
            f.addRecipe(new ExtractionRecipe());
            f.addRecipe(new UnnamingRecipe());
        });
        e.enqueueWork(() -> {
            if (ModList.get().isLoaded("gateways")) GatewaysCompat.register();
            if (ModList.get().isLoaded("theoneprobe")) AdventureTOPPlugin.register();
            if (ModList.get().isLoaded("twilightforest")) AdventureTwilightCompat.register();
            LootSystem.defaultBlockTable(Apoth.Blocks.SIMPLE_REFORGING_TABLE.get());
            LootSystem.defaultBlockTable(Apoth.Blocks.REFORGING_TABLE.get());
            LootSystem.defaultBlockTable(Apoth.Blocks.SALVAGING_TABLE.get());
            LootSystem.defaultBlockTable(Apoth.Blocks.GEM_CUTTING_TABLE.get());
            Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(Apotheosis.MODID, "random_affix_item"), AffixLootPoolEntry.TYPE);
            Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(Apotheosis.MODID, "random_gem"), GemLootPoolEntry.TYPE);
            Exclusion.initSerializers();
            GemBonus.initCodecs();
        });
    }

    @SubscribeEvent
    public void register(Register<Feature<?>> e) {
        e.getRegistry().register(BossDungeonFeature.INSTANCE, "boss_dungeon");
        e.getRegistry().register(BossDungeonFeature2.INSTANCE, "boss_dungeon_2");
        e.getRegistry().register(RogueSpawnerFeature.INSTANCE, "rogue_spawner");
        // e.getRegistry().register(TroveFeature.INSTANCE, "trove");
        // e.getRegistry().register(TomeTowerFeature.INSTANCE, "tome_tower");
        MinecraftForge.EVENT_BUS.register(AdventureGeneration.class);
        Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, "apotheosis:item_frame_gems", ITEM_FRAME_LOOT);
    }

    @SubscribeEvent
    public void items(Register<Item> e) {
        e.getRegistry().register(new GemItem(new Item.Properties()), "gem");
        e.getRegistry().register(new BossSummonerItem(new Item.Properties()), "boss_summoner");
        e.getRegistry().register(new Item(new Item.Properties()), "gem_dust");
        e.getRegistry().register(new Item(new Item.Properties()), "vial_of_extraction");
        e.getRegistry().register(new Item(new Item.Properties()), "vial_of_expulsion");
        e.getRegistry().register(new Item(new Item.Properties()), "vial_of_unnaming");
        String[] names = { "common", "uncommon", "rare", "epic", "mythic", "ancient" };
        for (String s : names) {
            Item material = new SalvageItem(RarityRegistry.INSTANCE.holder(Apotheosis.loc(s)), new Item.Properties());
            e.getRegistry().register(material, s + "_material");
        }
        e.getRegistry().register(new BlockItem(Apoth.Blocks.SIMPLE_REFORGING_TABLE.get(), new Item.Properties()), "simple_reforging_table");
        e.getRegistry().register(new BlockItem(Apoth.Blocks.REFORGING_TABLE.get(), new Item.Properties()), "reforging_table");
        e.getRegistry().register(new BlockItem(Apoth.Blocks.SALVAGING_TABLE.get(), new Item.Properties()), "salvaging_table");
        e.getRegistry().register(new BlockItem(Apoth.Blocks.GEM_CUTTING_TABLE.get(), new Item.Properties()), "gem_cutting_table");
        e.getRegistry().register(new Item(new Item.Properties()), "sigil_of_socketing");
        e.getRegistry().register(new GlowyItem(new Item.Properties()), "superior_sigil_of_socketing");
        e.getRegistry().register(new Item(new Item.Properties()), "sigil_of_enhancement");
        e.getRegistry().register(new GlowyItem(new Item.Properties()), "superior_sigil_of_enhancement");
    }

    @SubscribeEvent
    public void blocks(Register<Block> e) {
        e.getRegistry().register(new BossSpawnerBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()), "boss_spawner");
        e.getRegistry().register(new ReforgingTableBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(2, 20F), 2), "simple_reforging_table");
        e.getRegistry().register(new ReforgingTableBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(4, 1000F), 4), "reforging_table");
        e.getRegistry().register(new SalvagingTableBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(2.5F)), "salvaging_table");
        e.getRegistry().register(new GemCuttingBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(2.5F)), "gem_cutting_table");
    }

    @SubscribeEvent
    public void tiles(Register<BlockEntityType<?>> e) {
        e.getRegistry().register(new TickingBlockEntityType<>(BossSpawnerTile::new, ImmutableSet.of(Apoth.Blocks.BOSS_SPAWNER.get()), false, true), "boss_spawner");
        e.getRegistry().register(new TickingBlockEntityType<>(ReforgingTableTile::new, ImmutableSet.of(Apoth.Blocks.SIMPLE_REFORGING_TABLE.get(), Apoth.Blocks.REFORGING_TABLE.get()), true, false), "reforging_table");
        e.getRegistry().register(new BlockEntityType<>(SalvagingTableTile::new, ImmutableSet.of(Apoth.Blocks.SALVAGING_TABLE.get()), null), "salvaging_table");
    }

    @SubscribeEvent
    public void serializers(Register<RecipeSerializer<?>> e) {
        e.getRegistry().register(SocketingRecipe.Serializer.INSTANCE, "socketing");
        e.getRegistry().register(ExpulsionRecipe.Serializer.INSTANCE, "expulsion");
        e.getRegistry().register(ExtractionRecipe.Serializer.INSTANCE, "extraction");
        e.getRegistry().register(UnnamingRecipe.Serializer.INSTANCE, "unnaming");
        e.getRegistry().register(AddSocketsRecipe.Serializer.INSTANCE, "add_sockets");
        e.getRegistry().register(SalvagingRecipe.Serializer.INSTANCE, "salvaging");
    }

    @SubscribeEvent
    public void miscRegistration(RegisterEvent e) {
        if (e.getForgeRegistry() == (Object) ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get()) {
            e.getForgeRegistry().register("gems", GemLootModifier.CODEC);
            e.getForgeRegistry().register("affix_loot", AffixLootModifier.CODEC);
            e.getForgeRegistry().register("affix_conversion", AffixConvertLootModifier.CODEC);
            e.getForgeRegistry().register("affix_hook", AffixHookLootModifier.CODEC);
        }
        if (e.getForgeRegistry() == (Object) ForgeRegistries.BIOME_MODIFIER_SERIALIZERS.get()) {
            e.getForgeRegistry().register("blacklist", AdventureGeneration.BlacklistModifier.CODEC);
        }
    }

    @SubscribeEvent
    public void containers(Register<MenuType<?>> e) {
        e.getRegistry().register(MenuUtil.posType(ReforgingMenu::new), "reforging");
        e.getRegistry().register(MenuUtil.posType(SalvagingMenu::new), "salvage");
        e.getRegistry().register(MenuUtil.type((MenuSupplier<GemCuttingMenu>) GemCuttingMenu::new), "gem_cutting");
    }

    @SubscribeEvent
    public void client(FMLClientSetupEvent e) {
        e.enqueueWork(AdventureModuleClient::init);
        FMLJavaModLoadingContext.get().getModEventBus().register(new AdventureModuleClient());
    }

    /**
     * Loads all configurable data for the deadly module.
     */
    public void reload(ApotheosisReloadEvent e) {
        Configuration mainConfig = new Configuration(new File(Apotheosis.configDir, "adventure.cfg"));
        Configuration nameConfig = new Configuration(new File(Apotheosis.configDir, "names.cfg"));
        AdventureConfig.load(mainConfig);
        NameHelper.load(nameConfig);
        if (e == null && mainConfig.hasChanged()) mainConfig.save();
        if (e == null && nameConfig.hasChanged()) nameConfig.save();
    }

    public static final boolean DEBUG = false;

    public static void debugLog(BlockPos pos, String name) {
        if (DEBUG) AdventureModule.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
    }

    public static class ApothSmithingRecipe extends SmithingTransformRecipe {

        public ApothSmithingRecipe(ResourceLocation pId, Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
            super(pId, Ingredient.EMPTY, pBase, pAddition, pResult);
        }
    }

}
