package dev.shadowsoffire.apotheosis.ench;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Particles;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apotheosis.ModularDeferredHelper;
import dev.shadowsoffire.apotheosis.ench.anvil.ObliterationEnchant;
import dev.shadowsoffire.apotheosis.ench.anvil.SplittingEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.ChromaticEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.IcyThornsEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.InertEnchantment;
import dev.shadowsoffire.apotheosis.ench.enchantments.NaturesBlessingEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.ReboundingEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.ReflectiveEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.ShieldBashEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.SpearfishingEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.StableFootingEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.TemptingEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.corrupted.BerserkersFuryEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.corrupted.LifeMendingEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.ChainsawEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.CrescendoEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.EarthsBoonEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.EndlessQuiverEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.GrowthSerumEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.KnowledgeEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.ScavengerEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.twisted.ExploitationEnchant;
import dev.shadowsoffire.apotheosis.ench.enchantments.twisted.MinersFervorEnchant;
import dev.shadowsoffire.apotheosis.ench.library.EnchLibraryBlock;
import dev.shadowsoffire.apotheosis.ench.library.EnchLibraryTile.BasicLibraryTile;
import dev.shadowsoffire.apotheosis.ench.library.EnchLibraryTile.EnderLibraryTile;
import dev.shadowsoffire.apotheosis.ench.objects.ExtractionTomeItem;
import dev.shadowsoffire.apotheosis.ench.objects.FilteringShelfBlock;
import dev.shadowsoffire.apotheosis.ench.objects.FilteringShelfBlock.FilteringShelfTile;
import dev.shadowsoffire.apotheosis.ench.objects.GlowyBlockItem;
import dev.shadowsoffire.apotheosis.ench.objects.ImprovedScrappingTomeItem;
import dev.shadowsoffire.apotheosis.ench.objects.ScrappingTomeItem;
import dev.shadowsoffire.apotheosis.ench.objects.TomeItem;
import dev.shadowsoffire.apotheosis.ench.objects.TreasureShelfBlock;
import dev.shadowsoffire.apotheosis.ench.objects.TypedShelfBlock;
import dev.shadowsoffire.apotheosis.ench.objects.TypedShelfBlock.SculkShelfBlock;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

public class Ench {

    public static final class Blocks {

        public static final RegistryObject<Block> BEESHELF = woodShelf("beeshelf", MapColor.COLOR_YELLOW, 0.75F, () -> ParticleTypes.ENCHANT);

        public static final RegistryObject<Block> BLAZING_HELLSHELF = stoneShelf("blazing_hellshelf", MapColor.COLOR_BLACK, 1.5F, Particles.ENCHANT_FIRE);

        public static final RegistryObject<Block> CRYSTAL_SEASHELF = stoneShelf("crystal_seashelf", MapColor.COLOR_CYAN, 1.5F, Particles.ENCHANT_WATER);

        public static final RegistryObject<Block> DEEPSHELF = stoneShelf("deepshelf", MapColor.COLOR_BLACK, 2.5F, Particles.ENCHANT_SCULK);

        public static final RegistryObject<Block> DORMANT_DEEPSHELF = stoneShelf("dormant_deepshelf", MapColor.COLOR_BLACK, 2.5F, Particles.ENCHANT_SCULK);

        public static final RegistryObject<Block> DRACONIC_ENDSHELF = stoneShelf("draconic_endshelf", MapColor.SAND, 5F, Particles.ENCHANT_END);

        public static final RegistryObject<Block> ECHOING_DEEPSHELF = stoneShelf("echoing_deepshelf", MapColor.COLOR_BLACK, 2.5F, Particles.ENCHANT_SCULK);

        public static final RegistryObject<Block> ECHOING_SCULKSHELF = sculkShelf("echoing_sculkshelf");

        public static final RegistryObject<EnchLibraryBlock> ENDER_LIBRARY = R.block("ender_library", () -> new EnchLibraryBlock(EnderLibraryTile::new, 31));

        public static final RegistryObject<Block> ENDSHELF = stoneShelf("endshelf", MapColor.SAND, 4.5F, Particles.ENCHANT_END);

        public static final RegistryObject<Block> GLOWING_HELLSHELF = stoneShelf("glowing_hellshelf", MapColor.COLOR_BLACK, 1.5F, Particles.ENCHANT_FIRE);

        public static final RegistryObject<Block> HEART_SEASHELF = stoneShelf("heart_seashelf", MapColor.COLOR_CYAN, 1.5F, Particles.ENCHANT_WATER);

        public static final RegistryObject<Block> HELLSHELF = stoneShelf("hellshelf", MapColor.COLOR_BLACK, 1.5F, Particles.ENCHANT_FIRE);

        public static final RegistryObject<Block> INFUSED_HELLSHELF = stoneShelf("infused_hellshelf", MapColor.COLOR_BLACK, 1.5F, Particles.ENCHANT_FIRE);

        public static final RegistryObject<Block> INFUSED_SEASHELF = stoneShelf("infused_seashelf", MapColor.COLOR_CYAN, 1.5F, Particles.ENCHANT_WATER);

        public static final RegistryObject<EnchLibraryBlock> LIBRARY = R.block("library", () -> new EnchLibraryBlock(BasicLibraryTile::new, 16));

        public static final RegistryObject<Block> MELONSHELF = woodShelf("melonshelf", MapColor.COLOR_GREEN, 0.75F, () -> ParticleTypes.ENCHANT);

        public static final RegistryObject<Block> PEARL_ENDSHELF = stoneShelf("pearl_endshelf", MapColor.SAND, 4.5F, Particles.ENCHANT_END);

        public static final RegistryObject<Block> RECTIFIER = stoneShelf("rectifier", MapColor.COLOR_CYAN, 1.5F, Particles.ENCHANT_WATER);

        public static final RegistryObject<Block> RECTIFIER_T2 = stoneShelf("rectifier_t2", MapColor.COLOR_BLACK, 1.5F, Particles.ENCHANT_FIRE);

        public static final RegistryObject<Block> RECTIFIER_T3 = stoneShelf("rectifier_t3", MapColor.SAND, 1.5F, Particles.ENCHANT_END);

        public static final RegistryObject<Block> SEASHELF = stoneShelf("seashelf", MapColor.COLOR_CYAN, 1.5F, Particles.ENCHANT_WATER);

        public static final RegistryObject<Block> SIGHTSHELF = stoneShelf("sightshelf", MapColor.COLOR_BLACK, 1.5F, Particles.ENCHANT_FIRE);

        public static final RegistryObject<Block> SIGHTSHELF_T2 = stoneShelf("sightshelf_t2", MapColor.COLOR_BLACK, 1.5F, Particles.ENCHANT_FIRE);

        public static final RegistryObject<Block> SOUL_TOUCHED_DEEPSHELF = stoneShelf("soul_touched_deepshelf", MapColor.COLOR_BLACK, 2.5F, Particles.ENCHANT_SCULK);

        public static final RegistryObject<Block> SOUL_TOUCHED_SCULKSHELF = sculkShelf("soul_touched_sculkshelf");

        public static final RegistryObject<Block> STONESHELF = stoneShelf("stoneshelf", MapColor.STONE, 1.75F, () -> ParticleTypes.ENCHANT);

        public static final RegistryObject<Block> FILTERING_SHELF = R.block("filtering_shelf",
            () -> new FilteringShelfBlock(Block.Properties.of().mapColor(MapColor.COLOR_CYAN).sound(SoundType.STONE).strength(1.75F).requiresCorrectToolForDrops()));

        public static final RegistryObject<Block> TREASURE_SHELF = R.block("treasure_shelf",
            () -> new TreasureShelfBlock(Block.Properties.of().mapColor(MapColor.COLOR_BLACK).sound(SoundType.STONE).strength(1.75F).requiresCorrectToolForDrops()));

        private static void bootstrap() {}

        private static RegistryObject<Block> sculkShelf(String id) {
            return R.block(id, () -> new SculkShelfBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).sound(SoundType.STONE).randomTicks().requiresCorrectToolForDrops().strength(3.5F), Apoth.Particles.ENCHANT_SCULK));
        }

        private static RegistryObject<Block> stoneShelf(String id, MapColor color, float strength, Supplier<? extends ParticleOptions> particle) {
            return R.block(id, () -> new TypedShelfBlock(Block.Properties.of().requiresCorrectToolForDrops().sound(SoundType.STONE).mapColor(color).strength(strength), particle));
        }

        private static RegistryObject<Block> woodShelf(String id, MapColor color, float strength, Supplier<? extends ParticleOptions> particle) {
            return R.block(id, () -> new TypedShelfBlock(Block.Properties.of().sound(SoundType.WOOD).mapColor(color).strength(strength), particle));
        }

    }

    public static class Items {

        public static final RegistryObject<BlockItem> BEESHELF = R.item("beeshelf", () -> new BlockItem(Ench.Blocks.BEESHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> BLAZING_HELLSHELF = R.item("blazing_hellshelf", () -> new BlockItem(Ench.Blocks.BLAZING_HELLSHELF.get(), new Item.Properties()));

        public static final RegistryObject<TomeItem> BOOTS_TOME = R.item("boots_tome", () -> new TomeItem(net.minecraft.world.item.Items.DIAMOND_BOOTS, EnchantmentCategory.ARMOR_FEET));

        public static final RegistryObject<TomeItem> BOW_TOME = R.item("bow_tome", () -> new TomeItem(net.minecraft.world.item.Items.BOW, EnchantmentCategory.BOW));

        public static final RegistryObject<TomeItem> CHESTPLATE_TOME = R.item("chestplate_tome", () -> new TomeItem(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE, EnchantmentCategory.ARMOR_CHEST));

        public static final RegistryObject<BlockItem> CRYSTAL_SEASHELF = R.item("crystal_seashelf", () -> new BlockItem(Ench.Blocks.CRYSTAL_SEASHELF.get(), new Item.Properties()));

        public static final RegistryObject<GlowyBlockItem> DEEPSHELF = R.item("deepshelf", () -> new GlowyBlockItem(Ench.Blocks.DEEPSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> DORMANT_DEEPSHELF = R.item("dormant_deepshelf", () -> new BlockItem(Ench.Blocks.DORMANT_DEEPSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> DRACONIC_ENDSHELF = R.item("draconic_endshelf", () -> new BlockItem(Ench.Blocks.DRACONIC_ENDSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> ECHOING_DEEPSHELF = R.item("echoing_deepshelf", () -> new BlockItem(Ench.Blocks.ECHOING_DEEPSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> ECHOING_SCULKSHELF = R.item("echoing_sculkshelf", () -> new BlockItem(Ench.Blocks.ECHOING_SCULKSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> ENDER_LIBRARY = R.item("ender_library", () -> new BlockItem(Ench.Blocks.ENDER_LIBRARY.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> ENDSHELF = R.item("endshelf", () -> new BlockItem(Ench.Blocks.ENDSHELF.get(), new Item.Properties()));

        public static final RegistryObject<ExtractionTomeItem> EXTRACTION_TOME = R.item("extraction_tome", ExtractionTomeItem::new);

        public static final RegistryObject<TomeItem> FISHING_TOME = R.item("fishing_tome", () -> new TomeItem(net.minecraft.world.item.Items.FISHING_ROD, EnchantmentCategory.FISHING_ROD));

        public static final RegistryObject<BlockItem> GLOWING_HELLSHELF = R.item("glowing_hellshelf", () -> new BlockItem(Ench.Blocks.GLOWING_HELLSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> HEART_SEASHELF = R.item("heart_seashelf", () -> new BlockItem(Ench.Blocks.HEART_SEASHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> HELLSHELF = R.item("hellshelf", () -> new BlockItem(Ench.Blocks.HELLSHELF.get(), new Item.Properties()));

        public static final RegistryObject<TomeItem> HELMET_TOME = R.item("helmet_tome", () -> new TomeItem(net.minecraft.world.item.Items.DIAMOND_HELMET, EnchantmentCategory.ARMOR_HEAD));

        public static final RegistryObject<ImprovedScrappingTomeItem> IMPROVED_SCRAP_TOME = R.item("improved_scrap_tome", ImprovedScrappingTomeItem::new);

        public static final RegistryObject<Item> INERT_TRIDENT = R.item("inert_trident", () -> new Item(new Item.Properties().stacksTo(1)));

        public static final RegistryObject<Item> INFUSED_BREATH = R.item("infused_breath", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

        public static final RegistryObject<GlowyBlockItem> INFUSED_HELLSHELF = R.item("infused_hellshelf", () -> new GlowyBlockItem(Ench.Blocks.INFUSED_HELLSHELF.get(), new Item.Properties()));

        public static final RegistryObject<GlowyBlockItem> INFUSED_SEASHELF = R.item("infused_seashelf", () -> new GlowyBlockItem(Ench.Blocks.INFUSED_SEASHELF.get(), new Item.Properties()));

        public static final RegistryObject<TomeItem> LEGGINGS_TOME = R.item("leggings_tome", () -> new TomeItem(net.minecraft.world.item.Items.DIAMOND_LEGGINGS, EnchantmentCategory.ARMOR_LEGS));

        public static final RegistryObject<BlockItem> LIBRARY = R.item("library", () -> new BlockItem(Ench.Blocks.LIBRARY.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> MELONSHELF = R.item("melonshelf", () -> new BlockItem(Ench.Blocks.MELONSHELF.get(), new Item.Properties()));

        public static final RegistryObject<TomeItem> OTHER_TOME = R.item("other_tome", () -> new TomeItem(net.minecraft.world.item.Items.AIR, null));

        public static final RegistryObject<BlockItem> PEARL_ENDSHELF = R.item("pearl_endshelf", () -> new BlockItem(Ench.Blocks.PEARL_ENDSHELF.get(), new Item.Properties()));

        public static final RegistryObject<TomeItem> PICKAXE_TOME = R.item("pickaxe_tome", () -> new TomeItem(net.minecraft.world.item.Items.DIAMOND_PICKAXE, EnchantmentCategory.DIGGER));

        public static final RegistryObject<Item> PRISMATIC_WEB = R.item("prismatic_web", () -> new Item(new Item.Properties()));

        public static final RegistryObject<BlockItem> RECTIFIER = R.item("rectifier", () -> new BlockItem(Ench.Blocks.RECTIFIER.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> RECTIFIER_T2 = R.item("rectifier_t2", () -> new BlockItem(Ench.Blocks.RECTIFIER_T2.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> RECTIFIER_T3 = R.item("rectifier_t3", () -> new BlockItem(Ench.Blocks.RECTIFIER_T3.get(), new Item.Properties()));

        public static final RegistryObject<ScrappingTomeItem> SCRAP_TOME = R.item("scrap_tome", ScrappingTomeItem::new);

        public static final RegistryObject<BlockItem> SEASHELF = R.item("seashelf", () -> new BlockItem(Ench.Blocks.SEASHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> SIGHTSHELF = R.item("sightshelf", () -> new BlockItem(Ench.Blocks.SIGHTSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> SIGHTSHELF_T2 = R.item("sightshelf_t2", () -> new BlockItem(Ench.Blocks.SIGHTSHELF_T2.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> SOUL_TOUCHED_DEEPSHELF = R.item("soul_touched_deepshelf", () -> new BlockItem(Ench.Blocks.SOUL_TOUCHED_DEEPSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> SOUL_TOUCHED_SCULKSHELF = R.item("soul_touched_sculkshelf", () -> new BlockItem(Ench.Blocks.SOUL_TOUCHED_SCULKSHELF.get(), new Item.Properties()));

        public static final RegistryObject<BlockItem> STONESHELF = R.item("stoneshelf", () -> new BlockItem(Ench.Blocks.STONESHELF.get(), new Item.Properties()));

        public static final RegistryObject<Item> WARDEN_TENDRIL = R.item("warden_tendril", () -> new Item(new Item.Properties()));

        public static final RegistryObject<TomeItem> WEAPON_TOME = R.item("weapon_tome", () -> new TomeItem(net.minecraft.world.item.Items.DIAMOND_SWORD, EnchantmentCategory.WEAPON));

        public static final RegistryObject<BlockItem> FILTERING_SHELF = R.item("filtering_shelf", () -> new BlockItem(Ench.Blocks.FILTERING_SHELF.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

        public static final RegistryObject<BlockItem> TREASURE_SHELF = R.item("treasure_shelf", () -> new BlockItem(Ench.Blocks.TREASURE_SHELF.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

        private static void bootstrap() {}

    }

    public static final class Enchantments {

        public static final RegistryObject<BerserkersFuryEnchant> BERSERKERS_FURY = R.enchant("berserkers_fury", BerserkersFuryEnchant::new);

        public static final RegistryObject<ChainsawEnchant> CHAINSAW = R.enchant("chainsaw", ChainsawEnchant::new);

        public static final RegistryObject<ChromaticEnchant> CHROMATIC = R.enchant("chromatic", ChromaticEnchant::new);

        public static final RegistryObject<CrescendoEnchant> CRESCENDO = R.enchant("crescendo", CrescendoEnchant::new);

        public static final RegistryObject<EarthsBoonEnchant> EARTHS_BOON = R.enchant("earths_boon", EarthsBoonEnchant::new);

        public static final RegistryObject<EndlessQuiverEnchant> ENDLESS_QUIVER = R.enchant("endless_quiver", EndlessQuiverEnchant::new);

        public static final RegistryObject<ExploitationEnchant> EXPLOITATION = R.enchant("exploitation", ExploitationEnchant::new);

        public static final RegistryObject<GrowthSerumEnchant> GROWTH_SERUM = R.enchant("growth_serum", GrowthSerumEnchant::new);

        public static final RegistryObject<IcyThornsEnchant> ICY_THORNS = R.enchant("icy_thorns", IcyThornsEnchant::new);

        public static final RegistryObject<InertEnchantment> INFUSION = R.enchant("infusion", InertEnchantment::new);

        public static final RegistryObject<KnowledgeEnchant> KNOWLEDGE = R.enchant("knowledge", KnowledgeEnchant::new);

        public static final RegistryObject<LifeMendingEnchant> LIFE_MENDING = R.enchant("life_mending", LifeMendingEnchant::new);

        public static final RegistryObject<MinersFervorEnchant> MINERS_FERVOR = R.enchant("miners_fervor", MinersFervorEnchant::new);

        public static final RegistryObject<NaturesBlessingEnchant> NATURES_BLESSING = R.enchant("natures_blessing", NaturesBlessingEnchant::new);

        public static final RegistryObject<ObliterationEnchant> OBLITERATION = R.enchant("obliteration", ObliterationEnchant::new);

        public static final RegistryObject<ReboundingEnchant> REBOUNDING = R.enchant("rebounding", ReboundingEnchant::new);

        public static final RegistryObject<ReflectiveEnchant> REFLECTIVE = R.enchant("reflective", ReflectiveEnchant::new);

        public static final RegistryObject<ScavengerEnchant> SCAVENGER = R.enchant("scavenger", ScavengerEnchant::new);

        public static final RegistryObject<ShieldBashEnchant> SHIELD_BASH = R.enchant("shield_bash", ShieldBashEnchant::new);

        public static final RegistryObject<SpearfishingEnchant> SPEARFISHING = R.enchant("spearfishing", SpearfishingEnchant::new);

        public static final RegistryObject<SplittingEnchant> SPLITTING = R.enchant("splitting", SplittingEnchant::new);

        public static final RegistryObject<StableFootingEnchant> STABLE_FOOTING = R.enchant("stable_footing", StableFootingEnchant::new);

        public static final RegistryObject<TemptingEnchant> TEMPTING = R.enchant("tempting", TemptingEnchant::new);

        private static void bootstrap() {}

    }

    public static class Tabs {

        public static final RegistryObject<CreativeModeTab> ENCH = R.tab("ench",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.apotheosis.ench")).icon(() -> Items.HELLSHELF.get().getDefaultInstance()).withTabsBefore(Apotheosis.loc("adventure")).build());

        private static void bootstrap() {}

    }

    public static class Tiles {

        public static final RegistryObject<BlockEntityType<FilteringShelfTile>> FILTERING_SHELF = R.blockEntity("filtering_shelf",
            () -> new BlockEntityType<>(FilteringShelfTile::new, ImmutableSet.of(Blocks.FILTERING_SHELF.get()), null));

        private static void bootstrap() {}

    }

    public static class Colors {
        private static int[] _LIGHT_BLUE_FLASH = { 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff,
            0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff,
            0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff,
            0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x00b3ff, 0x0bb5ff,
            0x17b8ff, 0x22bbff, 0x2dbdff, 0x39c0ff, 0x44c3ff, 0x4fc6ff, 0x5bc9ff, 0x66ccff };

        public static GradientColor LIGHT_BLUE_FLASH = new GradientColor(ApothMiscUtil.doubleUpGradient(_LIGHT_BLUE_FLASH), "light_blue_flash");
    }

    private static final DeferredHelper R = ModularDeferredHelper.create(() -> Apotheosis.enableEnch);

    public static void bootstrap() {
        Blocks.bootstrap();
        Items.bootstrap();
        Enchantments.bootstrap();
        Tabs.bootstrap();
        Tiles.bootstrap();
    }

}
