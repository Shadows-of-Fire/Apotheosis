package dev.shadowsoffire.apotheosis.adventure;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apotheosis.ModularDeferredHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingMenu;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingTableBlock;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvageItem;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingMenu;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingTableBlock;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingBlock;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingMenu;
import dev.shadowsoffire.apotheosis.adventure.boss.BossSpawnerBlock;
import dev.shadowsoffire.apotheosis.adventure.boss.BossSummonerItem;
import dev.shadowsoffire.apotheosis.adventure.gen.BossDungeonFeature;
import dev.shadowsoffire.apotheosis.adventure.gen.BossDungeonFeature2;
import dev.shadowsoffire.apotheosis.adventure.gen.ItemFrameGemsProcessor;
import dev.shadowsoffire.apotheosis.adventure.gen.RogueSpawnerFeature;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.ench.objects.GlowyBlockItem.GlowyItem;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.RegistryObject;

public class Adventure {

    private static final DeferredHelper R = ModularDeferredHelper.create(() -> Apotheosis.enableAdventure);

    public static class Blocks {

        public static final RegistryObject<BossSpawnerBlock> BOSS_SPAWNER = R.block("boss_spawner",
            () -> new BossSpawnerBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));

        public static final RegistryObject<ReforgingTableBlock> SIMPLE_REFORGING_TABLE = R.block("simple_reforging_table",
            () -> new ReforgingTableBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(2, 20F), 2));

        public static final RegistryObject<ReforgingTableBlock> REFORGING_TABLE = R.block("reforging_table",
            () -> new ReforgingTableBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(4, 1000F), 4));

        public static final RegistryObject<SalvagingTableBlock> SALVAGING_TABLE = R.block("salvaging_table",
            () -> new SalvagingTableBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(2.5F)));

        public static final RegistryObject<GemCuttingBlock> GEM_CUTTING_TABLE = R.block("gem_cutting_table",
            () -> new GemCuttingBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(2.5F)));

        private static void bootstrap() {}

    }

    public static class Items {

        public static final RegistryObject<Item> COMMON_MATERIAL = rarityMat("common");

        public static final RegistryObject<Item> UNCOMMON_MATERIAL = rarityMat("uncommon");

        public static final RegistryObject<Item> RARE_MATERIAL = rarityMat("rare");

        public static final RegistryObject<Item> EPIC_MATERIAL = rarityMat("epic");

        public static final RegistryObject<Item> MYTHIC_MATERIAL = rarityMat("mythic");

        public static final RegistryObject<Item> ANCIENT_MATERIAL = rarityMat("ancient");

        public static final RegistryObject<Item> GEM_DUST = R.item("gem_dust", () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> VIAL_OF_EXPULSION = R.item("vial_of_expulsion", () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> VIAL_OF_EXTRACTION = R.item("vial_of_extraction", () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> VIAL_OF_UNNAMING = R.item("vial_of_unnaming", () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SIGIL_OF_SOCKETING = R.item("sigil_of_socketing", () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SUPERIOR_SIGIL_OF_SOCKETING = R.item("superior_sigil_of_socketing", () -> new GlowyItem(new Item.Properties()));

        public static final RegistryObject<Item> SIGIL_OF_ENHANCEMENT = R.item("sigil_of_enhancement", () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> SUPERIOR_SIGIL_OF_ENHANCEMENT = R.item("superior_sigil_of_enhancement", () -> new GlowyItem(new Item.Properties()));

        public static final RegistryObject<Item> BOSS_SUMMONER = R.item("boss_summoner", () -> new BossSummonerItem(new Item.Properties()));

        public static final RegistryObject<Item> SIMPLE_REFORGING_TABLE = R.item("simple_reforging_table", () -> new BlockItem(Blocks.SIMPLE_REFORGING_TABLE.get(), new Item.Properties()));

        public static final RegistryObject<Item> REFORGING_TABLE = R.item("reforging_table", () -> new BlockItem(Blocks.REFORGING_TABLE.get(), new Item.Properties()));

        public static final RegistryObject<Item> SALVAGING_TABLE = R.item("salvaging_table", () -> new BlockItem(Blocks.SALVAGING_TABLE.get(), new Item.Properties()));

        public static final RegistryObject<Item> GEM_CUTTING_TABLE = R.item("gem_cutting_table", () -> new BlockItem(Blocks.GEM_CUTTING_TABLE.get(), new Item.Properties()));

        public static final RegistryObject<Item> GEM = R.item("gem", () -> new GemItem(new Item.Properties()));

        private static RegistryObject<Item> rarityMat(String id) {
            return R.item(id + "_material", () -> new SalvageItem(RarityRegistry.INSTANCE.holder(Apotheosis.loc(id)), new Item.Properties()));
        }

        private static void bootstrap() {}

    }

    public static class Features {

        public static final RegistryObject<BossDungeonFeature> BOSS_DUNGEON = R.feature("boss_dungeon", BossDungeonFeature::new);

        public static final RegistryObject<BossDungeonFeature2> BOSS_DUNGEON_2 = R.feature("boss_dungeon_2", BossDungeonFeature2::new);

        public static final RegistryObject<RogueSpawnerFeature> ROGUE_SPAWNER = R.feature("rogue_spawner", RogueSpawnerFeature::new);

        public static final RegistryObject<StructureProcessorType<ItemFrameGemsProcessor>> ITEM_FRAME_GEMS = R.custom("item_frame_gems", Registries.STRUCTURE_PROCESSOR, () -> () -> ItemFrameGemsProcessor.CODEC);

        private static void bootstrap() {}

    }

    public static class Menus {

        public static final RegistryObject<MenuType<ReforgingMenu>> REFORGING = R.menu("reforging", () -> MenuUtil.posType(ReforgingMenu::new));

        public static final RegistryObject<MenuType<SalvagingMenu>> SALVAGE = R.menu("salvage", () -> MenuUtil.posType(SalvagingMenu::new));

        public static final RegistryObject<MenuType<GemCuttingMenu>> GEM_CUTTING = R.menu("gem_cutting", () -> MenuUtil.type(GemCuttingMenu::new));

        private static void bootstrap() {}
    }

    public static class Tabs {

        public static final RegistryObject<CreativeModeTab> ADVENTURE = R.tab("adventure",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.apotheosis.adventure")).icon(() -> Items.GEM.get().getDefaultInstance()).build());

        private static void bootstrap() {}
    }

    public static void bootstrap() {
        Blocks.bootstrap();
        Items.bootstrap();
        Features.bootstrap();
        Menus.bootstrap();
        Tabs.bootstrap();
    }

}
