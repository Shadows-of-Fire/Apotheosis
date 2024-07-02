package dev.shadowsoffire.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingTableTile;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.FestiveAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.MagicalArrowAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.OmneticAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.RadialAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.TelepathicAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingTableTile;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingTableTile;
import dev.shadowsoffire.apotheosis.adventure.boss.BossSpawnerBlock.BossSpawnerTile;
import dev.shadowsoffire.apotheosis.ench.anvil.AnvilTile;
import dev.shadowsoffire.apotheosis.ench.library.EnchLibraryContainer;
import dev.shadowsoffire.apotheosis.ench.library.EnchLibraryTile;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingRecipe;
import dev.shadowsoffire.apotheosis.garden.EnderLeadItem;
import dev.shadowsoffire.apotheosis.potion.PotionCharmItem;
import dev.shadowsoffire.apotheosis.spawn.enchantment.CapturingEnchant;
import dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerModifier;
import dev.shadowsoffire.apotheosis.village.fletching.FletchingContainer;
import dev.shadowsoffire.apotheosis.village.fletching.FletchingRecipe;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.BroadheadArrowEntity;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.BroadheadArrowItem;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.ExplosiveArrowEntity;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.ExplosiveArrowItem;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.MiningArrowEntity;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.MiningArrowItem;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.ObsidianArrowEntity;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.ObsidianArrowItem;
import dev.shadowsoffire.placebo.registry.RegObjHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.PlaceboUtil;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Object Holder Class. For the main mod class, see {@link Apotheosis}
 */
public class Apoth {

    public static final RegObjHelper R = new RegObjHelper(Apotheosis.MODID);

    public static final class Items {
        public static final RegistryObject<PotionCharmItem> POTION_CHARM = R.item("POTION_CHARM");
        public static final RegistryObject<Item> LUCKY_FOOT = R.item("LUCKY_FOOT");
        public static final RegistryObject<ObsidianArrowItem> OBSIDIAN_ARROW = R.item("OBSIDIAN_ARROW");
        public static final RegistryObject<BroadheadArrowItem> BROADHEAD_ARROW = R.item("BROADHEAD_ARROW");
        public static final RegistryObject<ExplosiveArrowItem> EXPLOSIVE_ARROW = R.item("EXPLOSIVE_ARROW");
        public static final RegistryObject<MiningArrowItem> IRON_MINING_ARROW = R.item("IRON_MINING_ARROW");
        public static final RegistryObject<MiningArrowItem> DIAMOND_MINING_ARROW = R.item("DIAMOND_MINING_ARROW");
        public static final RegistryObject<EnderLeadItem> ENDER_LEAD = R.item("ENDER_LEAD");
        public static final RegistryObject<Item> SKULL_FRAGMENT = RegistryObject.create(new ResourceLocation("wstweaks", "fragment"), ForgeRegistries.ITEMS);
    }

    public static final class Enchantments {

        public static final RegistryObject<CapturingEnchant> CAPTURING = Apoth.R.enchant("CAPTURING");
    }

    public static final class Potions {
        public static final RegistryObject<Potion> RESISTANCE = R.potion("RESISTANCE");
        public static final RegistryObject<Potion> LONG_RESISTANCE = R.potion("LONG_RESISTANCE");
        public static final RegistryObject<Potion> STRONG_RESISTANCE = R.potion("STRONG_RESISTANCE");
        public static final RegistryObject<Potion> ABSORPTION = R.potion("ABSORPTION");
        public static final RegistryObject<Potion> LONG_ABSORPTION = R.potion("LONG_ABSORPTION");
        public static final RegistryObject<Potion> STRONG_ABSORPTION = R.potion("STRONG_ABSORPTION");
        public static final RegistryObject<Potion> HASTE = R.potion("HASTE");
        public static final RegistryObject<Potion> LONG_HASTE = R.potion("LONG_HASTE");
        public static final RegistryObject<Potion> STRONG_HASTE = R.potion("STRONG_HASTE");
        public static final RegistryObject<Potion> FATIGUE = R.potion("FATIGUE");
        public static final RegistryObject<Potion> LONG_FATIGUE = R.potion("LONG_FATIGUE");
        public static final RegistryObject<Potion> STRONG_FATIGUE = R.potion("STRONG_FATIGUE");
        public static final RegistryObject<Potion> SUNDERING = R.potion("SUNDERING");
        public static final RegistryObject<Potion> LONG_SUNDERING = R.potion("LONG_SUNDERING");
        public static final RegistryObject<Potion> STRONG_SUNDERING = R.potion("STRONG_SUNDERING");
        public static final RegistryObject<Potion> KNOWLEDGE = R.potion("KNOWLEDGE");
        public static final RegistryObject<Potion> LONG_KNOWLEDGE = R.potion("LONG_KNOWLEDGE");
        public static final RegistryObject<Potion> STRONG_KNOWLEDGE = R.potion("STRONG_KNOWLEDGE");
        public static final RegistryObject<Potion> WITHER = R.potion("WITHER");
        public static final RegistryObject<Potion> LONG_WITHER = R.potion("LONG_WITHER");
        public static final RegistryObject<Potion> STRONG_WITHER = R.potion("STRONG_WITHER");
        public static final RegistryObject<Potion> VITALITY = R.potion("VITALITY");
        public static final RegistryObject<Potion> LONG_VITALITY = R.potion("LONG_VITALITY");
        public static final RegistryObject<Potion> STRONG_VITALITY = R.potion("STRONG_VITALITY");
        public static final RegistryObject<Potion> GRIEVOUS = R.potion("GRIEVOUS");
        public static final RegistryObject<Potion> LONG_GRIEVOUS = R.potion("LONG_GRIEVOUS");
        public static final RegistryObject<Potion> STRONG_GRIEVOUS = R.potion("STRONG_GRIEVOUS");
        public static final RegistryObject<Potion> LEVITATION = R.potion("LEVITATION");
        public static final RegistryObject<Potion> FLYING = R.potion("FLYING");
        public static final RegistryObject<Potion> LONG_FLYING = R.potion("LONG_FLYING");
        public static final RegistryObject<Potion> EXTRA_LONG_FLYING = R.potion("EXTRA_LONG_FLYING");
    }

    public static final class Entities {
        public static final RegistryObject<EntityType<ObsidianArrowEntity>> OBSIDIAN_ARROW = R.entity("OBSIDIAN_ARROW");
        public static final RegistryObject<EntityType<BroadheadArrowEntity>> BROADHEAD_ARROW = R.entity("BROADHEAD_ARROW");
        public static final RegistryObject<EntityType<ExplosiveArrowEntity>> EXPLOSIVE_ARROW = R.entity("EXPLOSIVE_ARROW");
        public static final RegistryObject<EntityType<MiningArrowEntity>> MINING_ARROW = R.entity("MINING_ARROW");
    }

    public static final class Menus {
        public static final RegistryObject<MenuType<FletchingContainer>> FLETCHING = R.menu("fletching");
        public static final RegistryObject<MenuType<EnchLibraryContainer>> LIBRARY = R.menu("library");
        public static final RegistryObject<MenuType<ApothEnchantmentMenu>> ENCHANTING_TABLE = R.menu("enchanting_table");
    }

    public static final class Tiles {
        public static final RegistryObject<BlockEntityType<EnchLibraryTile>> LIBRARY = R.blockEntity("LIBRARY");
        public static final RegistryObject<BlockEntityType<AnvilTile>> ANVIL = R.blockEntity("ANVIL");
        public static final RegistryObject<BlockEntityType<EnchLibraryTile>> ENDER_LIBRARY = R.blockEntity("ENDER_LIBRARY");
        public static final RegistryObject<BlockEntityType<BossSpawnerTile>> BOSS_SPAWNER = R.blockEntity("BOSS_SPAWNER");
        public static final RegistryObject<BlockEntityType<ReforgingTableTile>> REFORGING_TABLE = R.blockEntity("REFORGING_TABLE");
        public static final RegistryObject<BlockEntityType<SalvagingTableTile>> SALVAGING_TABLE = R.blockEntity("SALVAGING_TABLE");
        public static final RegistryObject<BlockEntityType<AugmentingTableTile>> AUGMENTING_TABLE = R.blockEntity("augmenting_table");
    }

    public static final class Affixes {
        // Implicit affixes
        public static final DynamicHolder<DurableAffix> DURABLE = AffixRegistry.INSTANCE.holder(Apotheosis.loc("durable"));
        // Real affixes
        public static final DynamicHolder<MagicalArrowAffix> MAGICAL = AffixRegistry.INSTANCE.holder(Apotheosis.loc("ranged/special/magical"));
        public static final DynamicHolder<FestiveAffix> FESTIVE = AffixRegistry.INSTANCE.holder(Apotheosis.loc("sword/special/festive"));
        public static final DynamicHolder<TelepathicAffix> TELEPATHIC = AffixRegistry.INSTANCE.holder(Apotheosis.loc("telepathic"));
        public static final DynamicHolder<OmneticAffix> OMNETIC = AffixRegistry.INSTANCE.holder(Apotheosis.loc("breaker/special/omnetic"));
        public static final DynamicHolder<RadialAffix> RADIAL = AffixRegistry.INSTANCE.holder(Apotheosis.loc("breaker/special/radial"));
    }

    public static final class Tags {
        public static final TagKey<Item> BOON_DROPS = ItemTags.create(new ResourceLocation(Apotheosis.MODID, "boon_drops"));
        public static final TagKey<Item> SPEARFISHING_DROPS = ItemTags.create(new ResourceLocation(Apotheosis.MODID, "spearfishing_drops"));
        public static final TagKey<Block> ROGUE_SPAWNER_COVERS = BlockTags.create(new ResourceLocation(Apotheosis.MODID, "rogue_spawner_covers"));
    }

    public static final class RecipeTypes {
        public static final RecipeType<FletchingRecipe> FLETCHING = PlaceboUtil.makeRecipeType("apotheosis:fletching");
        public static final RecipeType<EnchantingRecipe> INFUSION = PlaceboUtil.makeRecipeType("apotheosis:enchanting");
        public static final RecipeType<SpawnerModifier> MODIFIER = PlaceboUtil.makeRecipeType("apotheosis:spawner_modifier");
        public static final RecipeType<SalvagingRecipe> SALVAGING = PlaceboUtil.makeRecipeType("apotheosis:salvaging");
        public static final RecipeType<ReforgingRecipe> REFORGING = PlaceboUtil.makeRecipeType("apotheosis:reforging");
    }

    public static final class LootTables {
        public static final ResourceLocation CHEST_VALUABLE = Apotheosis.loc("chests/chest_valuable");
        public static final ResourceLocation SPAWNER_BRUTAL_ROTATE = Apotheosis.loc("chests/spawner_brutal_rotate");
        public static final ResourceLocation SPAWNER_BRUTAL = Apotheosis.loc("chests/spawner_brutal");
        public static final ResourceLocation SPAWNER_SWARM = Apotheosis.loc("chests/spawner_swarm");
        public static final ResourceLocation TOME_TOWER = Apotheosis.loc("chests/tome_tower");
    }

    public static final class Particles {
        public static final RegistryObject<SimpleParticleType> ENCHANT_FIRE = R.particle("enchant_fire");
        public static final RegistryObject<SimpleParticleType> ENCHANT_WATER = R.particle("enchant_water");
        public static final RegistryObject<SimpleParticleType> ENCHANT_SCULK = R.particle("enchant_sculk");
        public static final RegistryObject<SimpleParticleType> ENCHANT_END = R.particle("enchant_end");
    }

    public static final class DamageTypes {
        public static final ResourceKey<DamageType> EXECUTE = ResourceKey.create(Registries.DAMAGE_TYPE, Apotheosis.loc("execute"));
        public static final ResourceKey<DamageType> PSYCHIC = ResourceKey.create(Registries.DAMAGE_TYPE, Apotheosis.loc("psychic"));
        public static final ResourceKey<DamageType> CORRUPTED = ResourceKey.create(Registries.DAMAGE_TYPE, Apotheosis.loc("corrupted"));
    }

}
