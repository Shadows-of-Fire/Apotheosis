package dev.shadowsoffire.apotheosis;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.advancements.EquippedItemTrigger;
import dev.shadowsoffire.apotheosis.advancements.GemCutTrigger;
import dev.shadowsoffire.apotheosis.advancements.predicates.AffixItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.InvaderPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.MonsterPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.PurityItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.RarityItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.SocketItemPredicate;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingMenu;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingTableBlock;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingTableTile;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingMenu;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingRecipe;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingTableBlock;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingTableTile;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvageItem;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingMenu;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingTableBlock;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingTableTile;
import dev.shadowsoffire.apotheosis.attachments.BonusLootTables;
import dev.shadowsoffire.apotheosis.gen.BlacklistModifier;
import dev.shadowsoffire.apotheosis.gen.BossDungeonFeature;
import dev.shadowsoffire.apotheosis.gen.BossDungeonFeature2;
import dev.shadowsoffire.apotheosis.gen.ItemFrameGemsProcessor;
import dev.shadowsoffire.apotheosis.gen.RogueSpawnerFeature;
import dev.shadowsoffire.apotheosis.item.BossSummonerItem;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import dev.shadowsoffire.apotheosis.item.TooltipItem;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.loot.conditions.KilledByRealPlayerCondition;
import dev.shadowsoffire.apotheosis.loot.conditions.MatchesBlockCondition;
import dev.shadowsoffire.apotheosis.loot.conditions.WorldTierCondition;
import dev.shadowsoffire.apotheosis.loot.entry.AffixLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.entry.GemLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.functions.ReforgeItemFunction;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixConvertLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixHookLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixLootModifier;
import dev.shadowsoffire.apotheosis.loot.modifiers.GemLootModifier;
import dev.shadowsoffire.apotheosis.mobs.BossSpawnerBlock;
import dev.shadowsoffire.apotheosis.mobs.BossSpawnerBlock.BossSpawnerTile;
import dev.shadowsoffire.apotheosis.mobs.InvaderSpawnRules;
import dev.shadowsoffire.apotheosis.particle.RarityParticleData;
import dev.shadowsoffire.apotheosis.recipe.CharmInfusionRecipe;
import dev.shadowsoffire.apotheosis.recipe.MaliceRecipe;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import dev.shadowsoffire.apotheosis.recipe.SupremacyRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.FrozenDropsBonus;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.BasicGemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingBlock;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingMenu;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseBlock;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseMenu;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTile;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTile.BasicGemCaseTile;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTile.EnderGemCaseTile;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.util.AffixItemIngredient;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialState;
import dev.shadowsoffire.apotheosis.util.SingletonRecipeSerializer;
import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType.TickSide;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * Object Holder Class. For the main mod class, see {@link Apotheosis}
 */
public class Apoth {

    public static final DeferredHelper R = DeferredHelper.create(Apotheosis.MODID);

    public static final class BuiltInRegs {

        public static final Registry<LootCategory> LOOT_CATEGORY = R.registry("loot_category", b -> b.defaultKey(Apotheosis.loc("none")).onBake(LootCategory.Inner.rebuildSortedValueList()).sync(true));

        private static void bootstrap() {}
    }

    public static final class Components {

        public static final DataComponentType<ItemAffixes> AFFIXES = R.component("affixes", b -> b.persistent(ItemAffixes.CODEC).networkSynchronized(ItemAffixes.STREAM_CODEC));

        public static final DataComponentType<DynamicHolder<LootRarity>> RARITY = R.component("rarity", b -> b.persistent(RarityRegistry.INSTANCE.holderCodec()).networkSynchronized(RarityRegistry.INSTANCE.holderStreamCodec()));

        public static final DataComponentType<Component> AFFIX_NAME = R.component("affix_name", b -> b.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.TRUSTED_STREAM_CODEC));

        public static final DataComponentType<Integer> SOCKETS = R.component("sockets", b -> b.persistent(Codec.intRange(0, 16)).networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DataComponentType<ItemContainerContents> SOCKETED_GEMS = R.component("socketed_gems", b -> b.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));

        public static final DataComponentType<DynamicHolder<Gem>> GEM = R.component("gem", b -> b.persistent(GemRegistry.INSTANCE.holderCodec()).networkSynchronized(GemRegistry.INSTANCE.holderStreamCodec()));

        public static final DataComponentType<Purity> PURITY = R.component("purity", b -> b.persistent(Purity.CODEC).networkSynchronized(Purity.STREAM_CODEC));

        public static final DataComponentType<Float> DURABILITY_BONUS = R.component("durability_bonus", b -> b.persistent(Codec.floatRange(0, 1)).networkSynchronized(ByteBufCodecs.FLOAT));

        public static final DataComponentType<Boolean> FROM_CHEST = R.component("from_chest", b -> b.persistent(Codec.BOOL));

        public static final DataComponentType<Boolean> FROM_TRADER = R.component("from_trader", b -> b.persistent(Codec.BOOL));

        public static final DataComponentType<Boolean> FROM_BOSS = R.component("from_boss", b -> b.persistent(Codec.BOOL));

        public static final DataComponentType<Boolean> FROM_MOB = R.component("from_mob", b -> b.persistent(Codec.BOOL));

        public static final DataComponentType<Boolean> CHARM_ENABLED = R.component("charm_enabled", b -> b.persistent(Codec.BOOL));

        public static final DataComponentType<Block> STONEFORMING_TARGET = R.component("stoneforming_target", b -> b.persistent(BuiltInRegistries.BLOCK.byNameCodec()).networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK)));

        public static final DataComponentType<Boolean> MALICE_MARKER = R.component("malice_marker", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

        public static final DataComponentType<Boolean> TOUCHED_BY_MALICE = R.component("touched_by_malice", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

        private static void bootstrap() {}

    }

    public static final class Attachments {

        /**
         * Holds additional loot tables that will be dropped by the entity when it is killed.
         */
        public static final AttachmentType<BonusLootTables> BONUS_LOOT_TABLES = R.attachment("bonus_loot_tables", () -> BonusLootTables.EMPTY, b -> b.serialize(BonusLootTables.CODEC, blt -> !blt.tables().isEmpty()));

        /**
         * The player's current {@link WorldTier}.
         */
        public static final AttachmentType<WorldTier> WORLD_TIER = R.attachment("world_tier", () -> WorldTier.HAVEN, b -> b.serialize(WorldTier.CODEC).copyOnDeath().copyHandler((t, holder, prov) -> t));

        /**
         * Records if the {@link TierAugment}s for the current world tier have been applied to the attached entity or not.
         * <p>
         * If this is not set, they will be applied the next time the entity joins the level.
         */
        public static final AttachmentType<Boolean> TIER_AUGMENTS_APPLIED = R.attachment("tier_augments_applied", () -> false, b -> b.serialize(Codec.BOOL));

        /**
         * Supports the {@link FrozenDropsBonus} by recording the amount of post-mitigation cold damage an entity has taken.
         */
        public static final AttachmentType<Float> COLD_DAMAGE_TAKEN = R.attachment("cold_damage_taken", () -> 0F, b -> b.serialize(Codec.FLOAT));

        /**
         * Client-only attachment to record if the affix effect render has started.
         * <p>
         * Rendering starts when an item touches the ground, and stops if it leaves the ground.
         */
        public static final AttachmentType<Boolean> AFFIX_EFFECT_RENDER_STARTED = R.attachment("affix_effect_render_started", () -> false, UnaryOperator.identity());

        /**
         * Client-only attachment to record the tick count of an entity when the effect renderer starts.
         * <p>
         * This is used to interpolate progress-based effects that ease in.
         */
        public static final AttachmentType<Integer> AFFIX_EFFECT_START_TIME = R.attachment("affix_effect_start_time", () -> 0, UnaryOperator.identity());

        /**
         * Client-only attachment to record the time (in ticks, relative to the entity tick count) at which the next affix effect particle spawns.
         */
        public static final AttachmentType<Integer> AFFIX_EFFECT_NEXT_PARTICLE_TIME = R.attachment("affix_effect_next_particle_time", () -> 0, UnaryOperator.identity());

        /**
         * Client-only attachment to record the time (in ticks, relative to the entity tick count) at which the next affix effect particle spawns.
         */
        public static final AttachmentType<RadialState> RADIAL_MINING_MODE = R.attachment("radial_mining_mode", () -> RadialState.REQUIRE_NOT_SNEAKING, b -> b.serialize(RadialState.CODEC).copyOnDeath());

        private static void bootstrap() {}
    }

    public static final class Blocks {

        public static final Holder<Block> BOSS_SPAWNER = R.block("boss_spawner", BossSpawnerBlock::new,
            p -> p.requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable());

        public static final Holder<Block> SIMPLE_REFORGING_TABLE = R.block("simple_reforging_table", ReforgingTableBlock::new, p -> p.requiresCorrectToolForDrops().strength(2, 20F));

        public static final Holder<Block> REFORGING_TABLE = R.block("reforging_table", ReforgingTableBlock::new, p -> p.requiresCorrectToolForDrops().strength(4, 1000F));

        public static final Holder<Block> SALVAGING_TABLE = R.block("salvaging_table", SalvagingTableBlock::new,
            p -> p.sound(SoundType.WOOD).strength(2.5F));

        public static final Holder<Block> GEM_CUTTING_TABLE = R.block("gem_cutting_table", GemCuttingBlock::new,
            p -> p.sound(SoundType.WOOD).strength(2.5F));

        public static final Holder<Block> AUGMENTING_TABLE = R.block("augmenting_table", AugmentingTableBlock::new,
            p -> p.requiresCorrectToolForDrops().strength(4, 1000F));

        public static final Holder<Block> GEM_CASE = R.block("gem_case", p -> new GemCaseBlock(BasicGemCaseTile::new, p, Short.MAX_VALUE),
            p -> p.requiresCorrectToolForDrops().strength(5, 1200F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 2));

        public static final Holder<Block> ENDER_GEM_CASE = R.block("ender_gem_case", p -> new GemCaseBlock(EnderGemCaseTile::new, p, Integer.MAX_VALUE),
            p -> p.requiresCorrectToolForDrops().strength(5, 1200F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 2));

        private static void bootstrap() {}
    }

    public static final class Items extends net.minecraft.world.item.Items {

        public static final Holder<Item> COMMON_MATERIAL = rarityMat("common");

        public static final Holder<Item> UNCOMMON_MATERIAL = rarityMat("uncommon");

        public static final Holder<Item> RARE_MATERIAL = rarityMat("rare");

        public static final Holder<Item> EPIC_MATERIAL = rarityMat("epic");

        public static final Holder<Item> MYTHIC_MATERIAL = rarityMat("mythic");

        public static final Holder<Item> GEM_DUST = R.item("gem_dust", Item::new);

        public static final Holder<Item> GEM_FUSED_SLATE = R.item("gem_fused_slate", Item::new);

        public static final Holder<Item> SIGIL_OF_SOCKETING = R.item("sigil_of_socketing", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));

        public static final Holder<Item> SIGIL_OF_WITHDRAWAL = R.item("sigil_of_withdrawal", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));

        public static final Holder<Item> SIGIL_OF_REBIRTH = R.item("sigil_of_rebirth", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));

        public static final Holder<Item> SIGIL_OF_ENHANCEMENT = R.item("sigil_of_enhancement", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));

        public static final Holder<Item> SIGIL_OF_UNNAMING = R.item("sigil_of_unnaming", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));

        public static final Holder<Item> SIGIL_OF_MALICE = R.item("sigil_of_malice", TooltipItem::new, p -> p
            .component(DataComponents.ITEM_NAME, Apotheosis.lang("item", "sigil_of_malice").withStyle(ChatFormatting.RED)));

        public static final Holder<Item> SIGIL_OF_SUPREMACY = R.item("sigil_of_supremacy", TooltipItem::new, p -> p
            .component(DataComponents.ITEM_NAME, Apotheosis.lang("item", "sigil_of_supremacy").withStyle(ChatFormatting.GOLD)));

        public static final Holder<Item> BOSS_SUMMONER = R.item("boss_summoner", BossSummonerItem::new);

        public static final Holder<Item> SIMPLE_REFORGING_TABLE = R.blockItem("simple_reforging_table", Blocks.SIMPLE_REFORGING_TABLE);

        public static final Holder<Item> REFORGING_TABLE = R.blockItem("reforging_table", Blocks.REFORGING_TABLE, p -> p.rarity(Rarity.EPIC));

        public static final Holder<Item> SALVAGING_TABLE = R.blockItem("salvaging_table", Blocks.SALVAGING_TABLE);

        public static final Holder<Item> GEM_CUTTING_TABLE = R.blockItem("gem_cutting_table", Blocks.GEM_CUTTING_TABLE);

        public static final Holder<Item> AUGMENTING_TABLE = R.blockItem("augmenting_table", Blocks.AUGMENTING_TABLE, p -> p.rarity(Rarity.UNCOMMON));

        public static final Holder<Item> GEM_CASE = R.blockItem("gem_case", Blocks.GEM_CASE);

        public static final Holder<Item> ENDER_GEM_CASE = R.blockItem("ender_gem_case", Blocks.ENDER_GEM_CASE);

        public static final Holder<Item> GEM = R.item("gem", GemItem::new);

        public static final Holder<Item> POTION_CHARM = R.item("potion_charm", PotionCharmItem::new);

        public static final Holder<Item> IRON_UPGRADE_SMITHING_TEMPLATE = R.item("iron_upgrade_smithing_template", () -> createVanillaUpgradeTemplate("iron"));

        public static final Holder<Item> GOLD_UPGRADE_SMITHING_TEMPLATE = R.item("gold_upgrade_smithing_template", () -> createVanillaUpgradeTemplate("gold"));

        public static final Holder<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE = R.item("diamond_upgrade_smithing_template", () -> createVanillaUpgradeTemplate("diamond"));

        public static final Holder<Item> MUSIC_DISC_FLASH = R.item("music_disc_flash", Item::new, p -> p.rarity(Rarity.RARE).stacksTo(1).jukeboxPlayable(Songs.FLASH));

        public static final Holder<Item> MUSIC_DISC_GLIMMER = R.item("music_disc_glimmer", Item::new, p -> p.rarity(Rarity.RARE).stacksTo(1).jukeboxPlayable(Songs.GLIMMER));

        public static final Holder<Item> MUSIC_DISC_SHIMMER = R.item("music_disc_shimmer", Item::new, p -> p.rarity(Rarity.RARE).stacksTo(1).jukeboxPlayable(Songs.SHIMMER));

        private static Holder<Item> rarityMat(String id) {
            return R.item(id + "_material", () -> new SalvageItem(RarityRegistry.INSTANCE.holder(Apotheosis.loc(id)), new Item.Properties()));
        }

        private static SmithingTemplateItem createVanillaUpgradeTemplate(String type) {
            String path = type + "_upgrade_smithing_template";
            return new SmithingTemplateItem(
                Apotheosis.lang("item", path + ".applies_to").withStyle(ChatFormatting.BLUE),
                Apotheosis.lang("item", path + ".ingredients").withStyle(ChatFormatting.BLUE),
                Apotheosis.lang("upgrade", type).withStyle(ChatFormatting.GRAY),
                Apotheosis.lang("item", path + ".base_slot_description"),
                Apotheosis.lang("item", path + ".additions_slot_description"),
                SmithingTemplateItem.createNetheriteUpgradeIconList(),
                SmithingTemplateItem.createNetheriteUpgradeMaterialList());
        }

        private static void bootstrap() {}
    }

    public static final class Tiles {
        public static final BlockEntityType<BossSpawnerTile> BOSS_SPAWNER = R.tickingBlockEntity("boss_spawner", BossSpawnerTile::new, TickSide.SERVER, Blocks.BOSS_SPAWNER);
        public static final BlockEntityType<ReforgingTableTile> REFORGING_TABLE = R.tickingBlockEntity("reforging_table", ReforgingTableTile::new, TickSide.CLIENT, Blocks.REFORGING_TABLE, Blocks.SIMPLE_REFORGING_TABLE);
        public static final BlockEntityType<SalvagingTableTile> SALVAGING_TABLE = R.blockEntity("salvaging_table", SalvagingTableTile::new, Blocks.SALVAGING_TABLE);
        public static final BlockEntityType<AugmentingTableTile> AUGMENTING_TABLE = R.tickingBlockEntity("augmenting_table", AugmentingTableTile::new, TickSide.CLIENT, Blocks.AUGMENTING_TABLE);

        public static final BlockEntityType<GemCaseTile> GEM_CASE = R.tickingBlockEntity("gem_case", BasicGemCaseTile::new, TickSide.CLIENT, Blocks.GEM_CASE);
        public static final BlockEntityType<GemCaseTile> ENDER_GEM_CASE = R.tickingBlockEntity("ender_gem_case", EnderGemCaseTile::new, TickSide.CLIENT, Blocks.ENDER_GEM_CASE);

        private static void bootstrap() {}
    }

    public static final class Menus {
        public static final MenuType<ReforgingMenu> REFORGING = R.menuWithPos("reforging", ReforgingMenu::new);
        public static final MenuType<SalvagingMenu> SALVAGE = R.menuWithPos("salvage", SalvagingMenu::new);
        public static final MenuType<GemCuttingMenu> GEM_CUTTING = R.menu("gem_cutting", GemCuttingMenu::new);
        public static final MenuType<AugmentingMenu> AUGMENTING = R.menuWithPos("augmenting", AugmentingMenu::new);
        public static final MenuType<GemCaseMenu> GEM_CASE = R.menuWithPos("gem_case", GemCaseMenu::new);

        private static void bootstrap() {}
    }

    public static class Features {
        public static final Holder<Feature<?>> BOSS_DUNGEON = R.feature("boss_dungeon", BossDungeonFeature::new);
        public static final Holder<Feature<?>> BOSS_DUNGEON_2 = R.feature("boss_dungeon_2", BossDungeonFeature2::new);
        public static final Holder<Feature<?>> ROGUE_SPAWNER = R.feature("rogue_spawner", RogueSpawnerFeature::new);
        public static final StructureProcessorType<ItemFrameGemsProcessor> ITEM_FRAME_GEMS = R.structureProcessor("item_frame_gems", ItemFrameGemsProcessor.CODEC);

        private static void bootstrap() {}

    }

    public static class Tabs {
        public static final Holder<CreativeModeTab> ADVENTURE = R.creativeTab("adventure",
            b -> b.title(Component.translatable("itemGroup.apotheosis.adventure")).icon(() -> Items.GEM.value().getDefaultInstance()));

        private static void bootstrap() {}
    }

    public static class Sounds {
        public static final Holder<SoundEvent> REFORGE = R.sound("reforge");

        public static final Holder<SoundEvent> MALICE = R.sound("malice");

        public static final Holder<SoundEvent> MUSIC_DISC_FLASH = R.sound("music_disc_flash");
        public static final Holder<SoundEvent> MUSIC_DISC_GLIMMER = R.sound("music_disc_glimmer");
        public static final Holder<SoundEvent> MUSIC_DISC_SHIMMER = R.sound("music_disc_shimmer");

        public static final Holder<SoundEvent> INVADER_UNCOMMON = R.sound("invader_uncommon");
        public static final Holder<SoundEvent> INVADER_RARE = R.sound("invader_rare");
        public static final Holder<SoundEvent> INVADER_EPIC = R.sound("invader_epic");
        public static final Holder<SoundEvent> INVADER_MYTHIC = R.sound("invader_mythic");

        private static void bootstrap() {}
    }

    public static final class Songs {
        public static final ResourceKey<JukeboxSong> FLASH = key("flash");
        public static final ResourceKey<JukeboxSong> GLIMMER = key("glimmer");
        public static final ResourceKey<JukeboxSong> SHIMMER = key("shimmer");

        private static ResourceKey<JukeboxSong> key(String name) {
            return ResourceKey.create(Registries.JUKEBOX_SONG, Apotheosis.loc(name));
        }
    }

    public static final class RecipeTypes {
        public static final RecipeType<SalvagingRecipe> SALVAGING = R.recipe("salvaging");
        public static final RecipeType<ReforgingRecipe> REFORGING = R.recipe("reforging");
        public static final RecipeType<GemCuttingRecipe> GEM_CUTTING = R.recipe("gem_cutting");

        private static void bootstrap() {}
    }

    public static final class RecipeSerializers {
        public static final Holder<RecipeSerializer<?>> WITHDRAWAL = R.recipeSerializer("withdrawal", () -> new SingletonRecipeSerializer<>(WithdrawalRecipe::new));
        public static final Holder<RecipeSerializer<?>> SOCKETING = R.recipeSerializer("socketing", () -> new SingletonRecipeSerializer<>(SocketingRecipe::new));
        public static final Holder<RecipeSerializer<?>> SUPREMACY = R.recipeSerializer("supremacy", () -> new SingletonRecipeSerializer<>(SupremacyRecipe::new));
        public static final Holder<RecipeSerializer<?>> UNNAMING = R.recipeSerializer("unnaming", () -> new SingletonRecipeSerializer<>(UnnamingRecipe::new));
        public static final Holder<RecipeSerializer<?>> MALICE = R.recipeSerializer("malice", () -> new SingletonRecipeSerializer<>(MaliceRecipe::new));
        public static final Holder<RecipeSerializer<?>> ADD_SOCKETS = R.recipeSerializer("add_sockets", () -> AddSocketsRecipe.Serializer.INSTANCE);
        public static final Holder<RecipeSerializer<?>> SALVAGING = R.recipeSerializer("salvaging", () -> SalvagingRecipe.Serializer.INSTANCE);
        public static final Holder<RecipeSerializer<?>> REFORGING = R.recipeSerializer("reforging", () -> ReforgingRecipe.Serializer.INSTANCE);
        public static final Holder<RecipeSerializer<?>> PURITY_UPGRADE = R.recipeSerializer("purity_upgrade", () -> PurityUpgradeRecipe.Serializer.INSTANCE);
        public static final Holder<RecipeSerializer<?>> BASIC_GEM_CUTTING = R.recipeSerializer("basic_gem_cutting", () -> BasicGemCuttingRecipe.Serializer.INSTANCE);
        public static final Holder<RecipeSerializer<?>> POTION_CHARM_CRAFTING = R.recipeSerializer("potion_charm_crafting", () -> PotionCharmRecipe.Serializer.INSTANCE);
        public static final Holder<RecipeSerializer<?>> POTION_CHARM_INFUSION = R.recipeSerializer("potion_charm_infusion", () -> CharmInfusionRecipe.Serializer.INSTANCE);
        public static final Holder<RecipeSerializer<?>> SIZED_UPGRADE_RECIPE = R.recipeSerializer("sized_upgrade_recipe", () -> SizedUpgradeRecipe.Serializer.INSTANCE);

        private static void bootstrap() {}
    }

    public static final class Ingredients {
        public static final IngredientType<AffixItemIngredient> AFFIX = R.ingredient("affix", AffixItemIngredient.TYPE);
        public static final IngredientType<GemIngredient> GEM = R.ingredient("gem", GemIngredient.TYPE);

        private static void bootstrap() {}
    }

    public static final class LootPoolEntries {
        public static final LootPoolEntryType RANDOM_AFFIX_ITEM = R.lootPoolEntry("random_affix_item", AffixLootPoolEntry.TYPE);
        public static final LootPoolEntryType RANDOM_GEM = R.lootPoolEntry("random_gem", GemLootPoolEntry.TYPE);

        private static void bootstrap() {}
    }

    public static final class LootModifiers {
        public static final MapCodec<GemLootModifier> GEMS = R.lootModifier("gems", GemLootModifier.CODEC);
        public static final MapCodec<AffixLootModifier> AFFIX_LOOT = R.lootModifier("affix_loot", AffixLootModifier.CODEC);
        public static final MapCodec<AffixConvertLootModifier> AFFIX_CONVERSION = R.lootModifier("affix_conversion", AffixConvertLootModifier.CODEC);
        public static final MapCodec<AffixHookLootModifier> CODE_HOOK = R.lootModifier("code_hook", AffixHookLootModifier.CODEC);

        private static void bootstrap() {}
    }

    public static final class LootConditions {
        public static final LootItemConditionType MATCHES_BLOCK = R.lootCondition("matches_block", MatchesBlockCondition.CODEC);

        public static final LootItemConditionType KILLED_BY_REAL_PLAYER = R.lootCondition("killed_by_real_player", KilledByRealPlayerCondition.CODEC);

        public static final LootItemConditionType HAS_WORLD_TIER = R.lootCondition("has_world_tier", WorldTierCondition.CODEC);

        public static final LootItemConditionType LOOT_TABLE_PATTERN_MATCHER = R.lootCondition("loot_table_pattern_matcher", LootPatternMatcher.CODEC);

        private static void bootstrap() {}
    }

    public static final class LootFunctions {
        public static final LootItemFunctionType<ReforgeItemFunction> REFORGE_ITEM = R.custom("reforge_item", Registries.LOOT_FUNCTION_TYPE, ReforgeItemFunction.TYPE);

        private static void bootstrap() {}
    }

    public static final class Triggers {
        public static final GemCutTrigger GEM_CUTTING = R.criteriaTrigger("gem_cutting", new GemCutTrigger());
        public static final EquippedItemTrigger EQUIPPED_ITEM = R.criteriaTrigger("equipped_item", new EquippedItemTrigger());

        private static void bootstrap() {}
    }

    public static final class EntitySubPredicates {
        public static final MapCodec<MonsterPredicate> IS_MONSTER = R.custom("is_monster", Registries.ENTITY_SUB_PREDICATE_TYPE, MonsterPredicate.CODEC);
        public static final MapCodec<InvaderPredicate> IS_INVADER = R.custom("is_invader", Registries.ENTITY_SUB_PREDICATE_TYPE, InvaderPredicate.CODEC);

        private static void bootstrap() {}
    }

    public static final class ItemSubPredicates {

        public static final ItemSubPredicate.Type<AffixItemPredicate> AFFIXED_ITEM = R.itemSubPredicate("affixed_item", AffixItemPredicate.CODEC);
        public static final ItemSubPredicate.Type<PurityItemPredicate> ITEM_WITH_PURITY = R.itemSubPredicate("item_with_purity", PurityItemPredicate.CODEC);
        public static final ItemSubPredicate.Type<RarityItemPredicate> ITEM_WITH_RARITY = R.itemSubPredicate("item_with_rarity", RarityItemPredicate.CODEC);
        public static final ItemSubPredicate.Type<SocketItemPredicate> SOCKETED_ITEM = R.itemSubPredicate("socketed_item", SocketItemPredicate.CODEC);

        private static void bootstrap() {}
    }

    public static final class LootTables {
        public static final ResourceKey<LootTable> CHEST_VALUABLE = key("chests/chest_valuable");
        public static final ResourceKey<LootTable> SPAWNER_BRUTAL = key("chests/spawner_brutal");
        public static final ResourceKey<LootTable> SPAWNER_SWARM = key("chests/spawner_swarm");
        public static final ResourceKey<LootTable> TOME_TOWER = key("chests/tome_tower");
        public static final ResourceKey<LootTable> BONUS_BOSS_DROPS = key("entity/boss_drops");
        public static final ResourceKey<LootTable> BONUS_RARE_BOSS_DROPS = key("entity/rare_boss_drops");
        public static final ResourceKey<LootTable> TREASURE_GOBLIN = key("entity/treasure_goblin");

        private static ResourceKey<LootTable> key(String path) {
            return ResourceKey.create(Registries.LOOT_TABLE, Apotheosis.loc(path));
        }
    }

    public static final class Tags {
        public static final TagKey<Block> ROGUE_SPAWNER_COVERS = BlockTags.create(Apotheosis.loc("rogue_spawner_covers"));
        public static final TagKey<Block> STONEFORMING_CANDIDATES = BlockTags.create(Apotheosis.loc("stoneforming_candidates"));
        public static final TagKey<Block> SANDFORMING_CANDIDATES = BlockTags.create(Apotheosis.loc("sandforming_candidates"));
        public static final TagKey<Block> LEAFFORMING_CANDIDATES = BlockTags.create(Apotheosis.loc("leafforming_candidates"));
        public static final TagKey<Block> GARDENING_CANDIDATES = BlockTags.create(Apotheosis.loc("gardening_candidates"));
        public static final TagKey<Item> BOSS_MUSIC_DISCS = ItemTags.create(Apotheosis.loc("boss_music_discs"));

        /**
         * List of {@link Potion}s that cannot be converted into Potion Charms using {@link PotionCharmRecipe}.
         */
        public static final TagKey<Potion> POTION_CHARM_BLACKLIST = TagKey.create(Registries.POTION, Apotheosis.loc("potion_charm_blacklist"));

        /**
         * List of {@link MobEffect}s that will have their duration extended when using a {@link PotionCharmItem}.
         * <p>
         * This is necessary for certain effects (i.e. night vision) that have different effects at low duration levels.
         */
        public static final TagKey<MobEffect> EXTENDED_CHARM_DURATION = TagKey.create(Registries.MOB_EFFECT, Apotheosis.loc("extended_charm_duration"));
    }

    public static final class DamageTypes {
        public static final ResourceKey<DamageType> EXECUTE = ResourceKey.create(Registries.DAMAGE_TYPE, Apotheosis.loc("execute"));
        public static final ResourceKey<DamageType> PSYCHIC = ResourceKey.create(Registries.DAMAGE_TYPE, Apotheosis.loc("psychic"));

    }

    public static final class Advancements {
        public static final ResourceLocation WORLD_TIER_HAVEN = Apotheosis.loc("progression/haven");
        public static final ResourceLocation WORLD_TIER_FRONTIER = Apotheosis.loc("progression/frontier");
        public static final ResourceLocation WORLD_TIER_ASCENT = Apotheosis.loc("progression/ascent");
        public static final ResourceLocation WORLD_TIER_SUMMIT = Apotheosis.loc("progression/summit");
        public static final ResourceLocation WORLD_TIER_PINNACLE = Apotheosis.loc("progression/pinnacle");
    }

    public static final class Particles {
        public static final ParticleType<RarityParticleData> RARITY_GLOW = R.particle("rarity_glow", false, type -> RarityParticleData.CODEC, type -> RarityParticleData.STREAM_CODEC);

        private static void bootstrap() {}
    }

    public static final class Stats {
        public static final ResourceLocation WORLD_TIERS_ACTIVATED = R.customStat("world_tiers_activated", StatFormatter.DEFAULT);

        private static void bootstrap() {}
    }

    public static final class LootCategories {

        public static final LootCategory BOW = register("bow", s -> s.getItem() instanceof BowItem || s.getItem() instanceof CrossbowItem, ALObjects.EquipmentSlotGroups.HAND);
        public static final LootCategory BREAKER = register("breaker", s -> s.canPerformAction(ItemAbilities.PICKAXE_DIG) || s.canPerformAction(ItemAbilities.SHOVEL_DIG), ALObjects.EquipmentSlotGroups.MAINHAND);
        public static final LootCategory HELMET = register("helmet", armorSlot(EquipmentSlot.HEAD), ALObjects.EquipmentSlotGroups.HEAD);
        public static final LootCategory CHESTPLATE = register("chestplate", armorSlot(EquipmentSlot.CHEST), ALObjects.EquipmentSlotGroups.CHEST);
        public static final LootCategory LEGGINGS = register("leggings", armorSlot(EquipmentSlot.LEGS), ALObjects.EquipmentSlotGroups.LEGS);
        public static final LootCategory BOOTS = register("boots", armorSlot(EquipmentSlot.FEET), ALObjects.EquipmentSlotGroups.FEET);
        public static final LootCategory SHIELD = register("shield", s -> s.canPerformAction(ItemAbilities.SHIELD_BLOCK), ALObjects.EquipmentSlotGroups.HAND);
        public static final LootCategory TRIDENT = register("trident", s -> s.getItem() instanceof TridentItem, ALObjects.EquipmentSlotGroups.MAINHAND);
        public static final LootCategory MELEE_WEAPON = register("melee_weapon", s -> s.canPerformAction(ItemAbilities.SWORD_DIG) || getDefaultModifiers(s).compute(1, EquipmentSlot.MAINHAND) > 1,
            ALObjects.EquipmentSlotGroups.MAINHAND, 2000);
        public static final LootCategory SHEARS = register("shears", s -> s.canPerformAction(ItemAbilities.SHEARS_DIG), ALObjects.EquipmentSlotGroups.MAINHAND, 2500);
        public static final LootCategory NONE = register("none", Predicates.alwaysFalse(), ALObjects.EquipmentSlotGroups.ANY, Integer.MAX_VALUE);

        private static LootCategory register(String path, Predicate<ItemStack> filter, EntitySlotGroup slots, int priority) {
            return R.custom(path, BuiltInRegs.LOOT_CATEGORY.key(), new LootCategory(filter, slots, priority));
        }

        private static LootCategory register(String path, Predicate<ItemStack> filter, EntitySlotGroup slots) {
            return register(path, filter, slots, 1000);
        }

        private static Predicate<ItemStack> armorSlot(EquipmentSlot slot) {
            return stack -> {
                if (stack.is(net.minecraft.world.item.Items.CARVED_PUMPKIN) || stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof AbstractSkullBlock) {
                    return false;
                }

                EquipmentSlot itemSlot = stack.getEquipmentSlot();
                if (itemSlot == null) {
                    Equipable equipable = Equipable.get(stack);
                    if (equipable != null) {
                        itemSlot = equipable.getEquipmentSlot();
                    }
                }

                return itemSlot == slot;
            };
        }

        private static ItemAttributeModifiers getDefaultModifiers(ItemStack stack) {
            return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, stack.getItem().getDefaultAttributeModifiers(stack));
        }

        private static void bootstrap() {}

    }

    public static final class DataMaps {

        /**
         * Holds per-dimension spawn rules for Apothic Invaders.
         */
        public static final DataMapType<DimensionType, InvaderSpawnRules> INVADER_SPAWN_RULES = R.dataMap("invader_spawn_rules", Registries.DIMENSION_TYPE, InvaderSpawnRules.CODEC, UnaryOperator.identity());

        /**
         * Holds per-item loot category overrides.
         */
        public static final DataMapType<Item, LootCategory> LOOT_CATEGORY_OVERRIDES = R.dataMap("loot_category_overrides", Registries.ITEM, LootCategory.OPTIONAL_CODEC, c -> c.synced(LootCategory.OPTIONAL_CODEC, true));

        private static void bootstrap() {}
    }

    public static void bootstrap(IEventBus bus) {
        bus.register(R);

        BuiltInRegs.bootstrap();
        Attachments.bootstrap();
        Components.bootstrap();
        Sounds.bootstrap();
        Blocks.bootstrap();
        Items.bootstrap();
        Tiles.bootstrap();
        Menus.bootstrap();
        Tabs.bootstrap();
        Triggers.bootstrap();
        Features.bootstrap();
        Ingredients.bootstrap();
        RecipeTypes.bootstrap();
        LootModifiers.bootstrap();
        LootFunctions.bootstrap();
        LootConditions.bootstrap();
        LootPoolEntries.bootstrap();
        RecipeSerializers.bootstrap();
        ItemSubPredicates.bootstrap();
        EntitySubPredicates.bootstrap();
        Stats.bootstrap();
        Particles.bootstrap();
        LootCategories.bootstrap();
        DataMaps.bootstrap();

        R.custom("blacklist", NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, BlacklistModifier.CODEC);
    }

}
