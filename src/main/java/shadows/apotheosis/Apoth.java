package shadows.apotheosis;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadows.apotheosis.adventure.affix.AffixManager;
import shadows.apotheosis.adventure.affix.effect.DurableAffix;
import shadows.apotheosis.adventure.affix.effect.FestiveAffix;
import shadows.apotheosis.adventure.affix.effect.MagicalArrowAffix;
import shadows.apotheosis.adventure.affix.effect.OmneticAffix;
import shadows.apotheosis.adventure.affix.effect.RadialAffix;
import shadows.apotheosis.adventure.affix.effect.TelepathicAffix;
import shadows.apotheosis.adventure.affix.reforging.ReforgingMenu;
import shadows.apotheosis.adventure.affix.reforging.ReforgingTableBlock;
import shadows.apotheosis.adventure.affix.reforging.ReforgingTableTile;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingMenu;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingRecipe;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingTableBlock;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingTableTile;
import shadows.apotheosis.adventure.affix.socket.SocketAffix;
import shadows.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingBlock;
import shadows.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingMenu;
import shadows.apotheosis.adventure.boss.BossSpawnerBlock;
import shadows.apotheosis.adventure.boss.BossSpawnerBlock.BossSpawnerTile;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.anvil.ObliterationEnchant;
import shadows.apotheosis.ench.anvil.SplittingEnchant;
import shadows.apotheosis.ench.enchantments.ChromaticEnchant;
import shadows.apotheosis.ench.enchantments.InertEnchantment;
import shadows.apotheosis.ench.enchantments.NaturesBlessingEnchant;
import shadows.apotheosis.ench.enchantments.ReflectiveEnchant;
import shadows.apotheosis.ench.enchantments.SpearfishingEnchant;
import shadows.apotheosis.ench.enchantments.StableFootingEnchant;
import shadows.apotheosis.ench.enchantments.TemptingEnchant;
import shadows.apotheosis.ench.enchantments.corrupted.BerserkersFuryEnchant;
import shadows.apotheosis.ench.enchantments.corrupted.LifeMendingEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.ChainsawEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.CrescendoEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.EarthsBoonEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.EndlessQuiverEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.GrowthSerumEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.KnowledgeEnchant;
import shadows.apotheosis.ench.enchantments.masterwork.ScavengerEnchant;
import shadows.apotheosis.ench.enchantments.twisted.ExploitationEnchant;
import shadows.apotheosis.ench.enchantments.twisted.MinersFervorEnchant;
import shadows.apotheosis.ench.library.EnchLibraryBlock;
import shadows.apotheosis.ench.library.EnchLibraryContainer;
import shadows.apotheosis.ench.library.EnchLibraryTile;
import shadows.apotheosis.ench.table.ApothEnchantContainer;
import shadows.apotheosis.ench.table.EnchantingRecipe;
import shadows.apotheosis.garden.EnderLeadItem;
import shadows.apotheosis.potion.LuckyFootItem;
import shadows.apotheosis.potion.PotionCharmItem;
import shadows.apotheosis.spawn.enchantment.CapturingEnchant;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.apotheosis.village.fletching.FletchingContainer;
import shadows.apotheosis.village.fletching.FletchingRecipe;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowEntity;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowItem;
import shadows.apotheosis.village.fletching.arrows.ExplosiveArrowEntity;
import shadows.apotheosis.village.fletching.arrows.ExplosiveArrowItem;
import shadows.apotheosis.village.fletching.arrows.MiningArrowEntity;
import shadows.apotheosis.village.fletching.arrows.MiningArrowItem;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowEntity;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowItem;
import shadows.placebo.json.DynamicRegistryObject;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.RegObjHelper;

/**
 * Object Holder Class.  For the main mod class, see {@link Apotheosis}
 */
public class Apoth {

	private static final RegObjHelper R = new RegObjHelper(Apotheosis.MODID);

	public static final class Blocks {
		public static final RegistryObject<EnchLibraryBlock> LIBRARY = R.block("LIBRARY");
		public static final RegistryObject<Block> HELLSHELF = R.block("HELLSHELF");
		public static final RegistryObject<Block> INFUSED_HELLSHELF = R.block("INFUSED_HELLSHELF");
		public static final RegistryObject<Block> BLAZING_HELLSHELF = R.block("BLAZING_HELLSHELF");
		public static final RegistryObject<Block> GLOWING_HELLSHELF = R.block("GLOWING_HELLSHELF");
		public static final RegistryObject<Block> SEASHELF = R.block("SEASHELF");
		public static final RegistryObject<Block> INFUSED_SEASHELF = R.block("INFUSED_SEASHELF");
		public static final RegistryObject<Block> CRYSTAL_SEASHELF = R.block("CRYSTAL_SEASHELF");
		public static final RegistryObject<Block> HEART_SEASHELF = R.block("HEART_SEASHELF");
		public static final RegistryObject<Block> ENDSHELF = R.block("ENDSHELF");
		public static final RegistryObject<Block> PEARL_ENDSHELF = R.block("PEARL_ENDSHELF");
		public static final RegistryObject<Block> DRACONIC_ENDSHELF = R.block("DRACONIC_ENDSHELF");
		public static final RegistryObject<Block> BEESHELF = R.block("BEESHELF");
		public static final RegistryObject<Block> MELONSHELF = R.block("MELONSHELF");
		public static final RegistryObject<Block> RECTIFIER = R.block("RECTIFIER");
		public static final RegistryObject<Block> RECTIFIER_T2 = R.block("RECTIFIER_T2");
		public static final RegistryObject<Block> RECTIFIER_T3 = R.block("RECTIFIER_T3");
		public static final RegistryObject<Block> SIGHTSHELF = R.block("SIGHTSHELF");
		public static final RegistryObject<Block> SIGHTSHELF_T2 = R.block("SIGHTSHELF_T2");
		public static final RegistryObject<EnchLibraryBlock> ENDER_LIBRARY = R.block("ENDER_LIBRARY");
		public static final RegistryObject<BossSpawnerBlock> BOSS_SPAWNER = R.block("BOSS_SPAWNER");
		public static final RegistryObject<ReforgingTableBlock> SIMPLE_REFORGING_TABLE = R.block("SIMPLE_REFORGING_TABLE");
		public static final RegistryObject<ReforgingTableBlock> REFORGING_TABLE = R.block("REFORGING_TABLE");
		public static final RegistryObject<SalvagingTableBlock> SALVAGING_TABLE = R.block("SALVAGING_TABLE");
		public static final RegistryObject<GemCuttingBlock> GEM_CUTTING_TABLE = R.block("gem_cutting_table");
	}

	public static final class Items {
		public static final RegistryObject<PotionCharmItem> POTION_CHARM = R.item("POTION_CHARM");
		public static final RegistryObject<LuckyFootItem> LUCKY_FOOT = R.item("LUCKY_FOOT");
		public static final RegistryObject<ObsidianArrowItem> OBSIDIAN_ARROW = R.item("OBSIDIAN_ARROW");
		public static final RegistryObject<BroadheadArrowItem> BROADHEAD_ARROW = R.item("BROADHEAD_ARROW");
		public static final RegistryObject<ExplosiveArrowItem> EXPLOSIVE_ARROW = R.item("EXPLOSIVE_ARROW");
		public static final RegistryObject<MiningArrowItem> IRON_MINING_ARROW = R.item("IRON_MINING_ARROW");
		public static final RegistryObject<MiningArrowItem> DIAMOND_MINING_ARROW = R.item("DIAMOND_MINING_ARROW");
		public static final RegistryObject<EnderLeadItem> ENDER_LEAD = R.item("ENDER_LEAD");
		public static final RegistryObject<Item> PRISMATIC_WEB = R.item("PRISMATIC_WEB");
		public static final RegistryObject<Item> SCRAP_TOME = R.item("SCRAP_TOME");
		public static final RegistryObject<Item> OTHER_TOME = R.item("OTHER_TOME");
		public static final RegistryObject<Item> HELMET_TOME = R.item("HELMET_TOME");
		public static final RegistryObject<Item> CHESTPLATE_TOME = R.item("CHESTPLATE_TOME");
		public static final RegistryObject<Item> LEGGINGS_TOME = R.item("LEGGINGS_TOME");
		public static final RegistryObject<Item> BOOTS_TOME = R.item("BOOTS_TOME");
		public static final RegistryObject<Item> WEAPON_TOME = R.item("WEAPON_TOME");
		public static final RegistryObject<Item> PICKAXE_TOME = R.item("PICKAXE_TOME");
		public static final RegistryObject<Item> FISHING_TOME = R.item("FISHING_TOME");
		public static final RegistryObject<Item> BOW_TOME = R.item("BOW_TOME");
		public static final RegistryObject<Item> GEM = R.item("GEM");
		public static final RegistryObject<Item> GEM_DUST = R.item("GEM_DUST");
		public static final RegistryObject<Item> VIAL_OF_EXPULSION = R.item("VIAL_OF_EXPULSION");
		public static final RegistryObject<Item> VIAL_OF_EXTRACTION = R.item("VIAL_OF_EXTRACTION");
		public static final RegistryObject<Item> VIAL_OF_UNNAMING = R.item("VIAL_OF_UNNAMING");

		public static final RegistryObject<Item> SKULL_FRAGMENT = RegistryObject.create(new ResourceLocation("wstweaks", "fragment"), ForgeRegistries.ITEMS);
	}

	public static final class Enchantments {
		public static final RegistryObject<EndlessQuiverEnchant> ENDLESS_QUIVER = R.enchant("ENDLESS_QUIVER");
		public static final RegistryObject<CapturingEnchant> CAPTURING = R.enchant("CAPTURING");
		public static final RegistryObject<BerserkersFuryEnchant> BERSERKERS_FURY = R.enchant("BERSERKERS_FURY");
		public static final RegistryObject<CrescendoEnchant> CRESCENDO = R.enchant("CRESCENDO");
		public static final RegistryObject<KnowledgeEnchant> KNOWLEDGE = R.enchant("KNOWLEDGE");
		public static final RegistryObject<LifeMendingEnchant> LIFE_MENDING = R.enchant("LIFE_MENDING");
		public static final RegistryObject<MinersFervorEnchant> MINERS_FERVOR = R.enchant("MINERS_FERVOR");
		public static final RegistryObject<NaturesBlessingEnchant> NATURES_BLESSING = R.enchant("NATURES_BLESSING");
		public static final RegistryObject<ReflectiveEnchant> REFLECTIVE = R.enchant("REFLECTIVE");
		public static final RegistryObject<ScavengerEnchant> SCAVENGER = R.enchant("SCAVENGER");
		public static final RegistryObject<StableFootingEnchant> STABLE_FOOTING = R.enchant("STABLE_FOOTING");
		public static final RegistryObject<TemptingEnchant> TEMPTING = R.enchant("TEMPTING");
		public static final RegistryObject<ObliterationEnchant> OBLITERATION = R.enchant("OBLITERATION");
		public static final RegistryObject<SplittingEnchant> SPLITTING = R.enchant("SPLITTING");
		public static final RegistryObject<InertEnchantment> INFUSION = R.enchant("INFUSION");
		public static final RegistryObject<ChromaticEnchant> CHROMATIC = R.enchant("CHROMATIC");
		public static final RegistryObject<ExploitationEnchant> EXPLOITATION = R.enchant("EXPLOITATION");
		public static final RegistryObject<GrowthSerumEnchant> GROWTH_SERUM = R.enchant("GROWTH_SERUM");
		public static final RegistryObject<EarthsBoonEnchant> EARTHS_BOON = R.enchant("EARTHS_BOON");
		public static final RegistryObject<ChainsawEnchant> CHAINSAW = R.enchant("CHAINSAW");
		public static final RegistryObject<SpearfishingEnchant> SPEARFISHING = R.enchant("SPEARFISHING");
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
	}

	public static final class Entities {
		public static final RegistryObject<EntityType<ObsidianArrowEntity>> OBSIDIAN_ARROW = R.entity("OBSIDIAN_ARROW");
		public static final RegistryObject<EntityType<BroadheadArrowEntity>> BROADHEAD_ARROW = R.entity("BROADHEAD_ARROW");
		public static final RegistryObject<EntityType<ExplosiveArrowEntity>> EXPLOSIVE_ARROW = R.entity("EXPLOSIVE_ARROW");
		public static final RegistryObject<EntityType<MiningArrowEntity>> MINING_ARROW = R.entity("MINING_ARROW");
	}

	public static final class Effects {
		public static final RegistryObject<MobEffect> SUNDERING = R.effect("sundering");
		public static final RegistryObject<MobEffect> KNOWLEDGE = R.effect("knowledge");
		public static final RegistryObject<MobEffect> VITALITY = R.effect("vitality");
		public static final RegistryObject<MobEffect> GRIEVOUS = R.effect("grievous");

		public static final RegistryObject<MobEffect> BLEEDING = R.effect("bleeding");
	}

	public static final class Menus {
		public static final RegistryObject<MenuType<FletchingContainer>> FLETCHING = R.menu("fletching");
		public static final RegistryObject<MenuType<EnchLibraryContainer>> LIBRARY = R.menu("library");
		public static final RegistryObject<MenuType<ApothEnchantContainer>> ENCHANTING_TABLE = R.menu("enchanting_table");
		public static final RegistryObject<MenuType<ReforgingMenu>> REFORGING = R.menu("reforging");
		public static final RegistryObject<MenuType<SalvagingMenu>> SALVAGE = R.menu("salvage");
		public static final RegistryObject<MenuType<GemCuttingMenu>> GEM_CUTTING = R.menu("gem_cutting");
	}

	public static final class Tiles {
		public static final RegistryObject<BlockEntityType<EnchLibraryTile>> LIBRARY = R.blockEntity("LIBRARY");
		public static final RegistryObject<BlockEntityType<AnvilTile>> ANVIL = R.blockEntity("ANVIL");
		public static final RegistryObject<BlockEntityType<EnchLibraryTile>> ENDER_LIBRARY = R.blockEntity("ENDER_LIBRARY");
		public static final RegistryObject<BlockEntityType<BossSpawnerTile>> BOSS_SPAWNER = R.blockEntity("BOSS_SPAWNER");
		public static final RegistryObject<BlockEntityType<ReforgingTableTile>> REFORGING_TABLE = R.blockEntity("REFORGING_TABLE");
		public static final RegistryObject<BlockEntityType<SalvagingTableTile>> SALVAGING_TABLE = R.blockEntity("SALVAGING_TABLE");
	}

	public static final class Attributes {
		/**
		 * Bonus to how fast a ranged weapon is charged. Base Value = (1.0) = 100%
		 */
		public static final RegistryObject<Attribute> DRAW_SPEED = R.attribute("DRAW_SPEED");
		/**
		 * Chance that a non-jump-attack will critically strike.  Base value = (1.0) = 0%
		 */
		public static final RegistryObject<Attribute> CRIT_CHANCE = R.attribute("CRIT_CHANCE");
		/**
		 * Amount of damage caused by critical strikes. Base value = (1.0) = 100%
		 * Not related to vanilla critical strikes.
		 */
		public static final RegistryObject<Attribute> CRIT_DAMAGE = R.attribute("CRIT_DAMAGE");
		/**
		 * Bonus magic damage that slows enemies hit. Base value = (0.0) = 0 damage
		 */
		public static final RegistryObject<Attribute> COLD_DAMAGE = R.attribute("COLD_DAMAGE");
		/**
		 * Bonus magic damage that burns enemies hit. Base value = (0.0) = 0 damage
		 */
		public static final RegistryObject<Attribute> FIRE_DAMAGE = R.attribute("FIRE_DAMAGE");
		/**
		 * Percent of physical damage converted to health. Base value = (1.0) = 0%
		 */
		public static final RegistryObject<Attribute> LIFE_STEAL = R.attribute("LIFE_STEAL");
		/**
		 * Percent of physical damage that bypasses armor. Base value = (1.0) = 0%
		 */
		public static final RegistryObject<Attribute> PIERCING = R.attribute("PIERCING");
		/**
		 * Bonus physical damage dealt equal to enemy's current health. Base value = (1.0) = 0%
		 */
		public static final RegistryObject<Attribute> CURRENT_HP_DAMAGE = R.attribute("CURRENT_HP_DAMAGE");
		/**
		 * Percent of physical damage converted to absorption hearts. Base value = (1.0) = 0%
		 */
		public static final RegistryObject<Attribute> OVERHEAL = R.attribute("OVERHEAL");
		/**
		 * Extra health that regenerates when not taking damage. Base value = (0.0) = 0 damage
		 */
		public static final RegistryObject<Attribute> GHOST_HEALTH = R.attribute("GHOST_HEALTH");
		/**
		 * Mining Speed. Base value = (1.0) = 100% default break speed
		 */
		public static final RegistryObject<Attribute> MINING_SPEED = R.attribute("MINING_SPEED");
		/**
		 * Arrow Damage. Base value = (1.0) = 100% default arrow damage
		 */
		public static final RegistryObject<Attribute> ARROW_DAMAGE = R.attribute("ARROW_DAMAGE");
		/**
		 * Arrow Velocity. Base value = (1.0) = 100% default arrow velocity
		 */
		public static final RegistryObject<Attribute> ARROW_VELOCITY = R.attribute("ARROW_VELOCITY");
		/**
		 * Experience mulitplier, from killing mobs or breaking ores. Base value = (1.0) = 100% xp gained.
		 */
		public static final RegistryObject<Attribute> EXPERIENCE_GAINED = R.attribute("experience_gained");
	}

	public static final class Affixes {
		// Implicit affixes
		public static final DynamicRegistryObject<SocketAffix> SOCKET = AffixManager.INSTANCE.makeObj(Apotheosis.loc("socket"));
		public static final DynamicRegistryObject<DurableAffix> DURABLE = AffixManager.INSTANCE.makeObj(Apotheosis.loc("durable"));
		// Real affixes
		public static final DynamicRegistryObject<MagicalArrowAffix> MAGICAL = AffixManager.INSTANCE.makeObj(Apotheosis.loc("ranged/special/magical"));
		public static final DynamicRegistryObject<FestiveAffix> FESTIVE = AffixManager.INSTANCE.makeObj(Apotheosis.loc("sword/special/festive"));
		public static final DynamicRegistryObject<TelepathicAffix> TELEPATHIC = AffixManager.INSTANCE.makeObj(Apotheosis.loc("telepathic"));
		public static final DynamicRegistryObject<OmneticAffix> OMNETIC = AffixManager.INSTANCE.makeObj(Apotheosis.loc("breaker/special/omnetic"));
		public static final DynamicRegistryObject<RadialAffix> RADIAL = AffixManager.INSTANCE.makeObj(Apotheosis.loc("breaker/special/radial"));
	}

	public static final class Tags {
		public static final TagKey<Item> BOON_DROPS = ItemTags.create(new ResourceLocation(Apotheosis.MODID, "boon_drops"));
		public static final TagKey<Item> SPEARFISHING_DROPS = ItemTags.create(new ResourceLocation(Apotheosis.MODID, "spearfishing_drops"));
	}

	public static final class RecipeTypes {
		public static final RecipeType<FletchingRecipe> FLETCHING = PlaceboUtil.makeRecipeType("apotheosis:fletching");
		public static final RecipeType<EnchantingRecipe> INFUSION = PlaceboUtil.makeRecipeType("apotheosis:enchanting");
		public static final RecipeType<SpawnerModifier> MODIFIER = PlaceboUtil.makeRecipeType("apotheosis:spawner_modifier");
		public static final RecipeType<SalvagingRecipe> SALVAGING = PlaceboUtil.makeRecipeType("apotheosis:salvaging");
	}

	public static final class LootTables {
		public static final ResourceLocation CHEST_VALUABLE = Apotheosis.loc("chests/chest_valuable");
		public static final ResourceLocation SPAWNER_BRUTAL_ROTATE = Apotheosis.loc("chests/spawner_brutal_rotate");
		public static final ResourceLocation SPAWNER_BRUTAL = Apotheosis.loc("chests/spawner_brutal");
		public static final ResourceLocation SPAWNER_SWARM = Apotheosis.loc("chests/spawner_swarm");
		public static final ResourceLocation TOME_TOWER = Apotheosis.loc("chests/tome_tower");
	}

}