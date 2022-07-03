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
import net.minecraftforge.registries.ObjectHolder;
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
import shadows.apotheosis.potion.potions.KnowledgeEffect;
import shadows.apotheosis.potion.potions.SunderingEffect;
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
import shadows.placebo.util.PlaceboUtil;

/**
 * Object Holder Class.  For the main mod class, see {@link Apotheosis}
 */
public class Apoth {

	@ObjectHolder(Apotheosis.MODID)
	public static final class Blocks {
		public static final EnchLibraryBlock LIBRARY = null;
		public static final Block HELLSHELF = null;
		public static final Block INFUSED_HELLSHELF = null;
		public static final Block BLAZING_HELLSHELF = null;
		public static final Block GLOWING_HELLSHELF = null;
		public static final Block SEASHELF = null;
		public static final Block INFUSED_SEASHELF = null;
		public static final Block CRYSTAL_SEASHELF = null;
		public static final Block HEART_SEASHELF = null;
		public static final Block ENDSHELF = null;
		public static final Block PEARL_ENDSHELF = null;
		public static final Block DRACONIC_ENDSHELF = null;
		public static final Block BEESHELF = null;
		public static final Block MELONSHELF = null;
		public static final Block RECTIFIER = null;
		public static final Block RECTIFIER_T2 = null;
		public static final Block RECTIFIER_T3 = null;
		public static final Block SIGHTSHELF = null;
		public static final Block SIGHTSHELF_T2 = null;
		public static final EnchLibraryBlock ENDER_LIBRARY = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Items {
		public static final PotionCharmItem POTION_CHARM = null;
		public static final LuckyFootItem LUCKY_FOOT = null;
		public static final ObsidianArrowItem OBSIDIAN_ARROW = null;
		public static final BroadheadArrowItem BROADHEAD_ARROW = null;
		public static final ExplosiveArrowItem EXPLOSIVE_ARROW = null;
		public static final MiningArrowItem IRON_MINING_ARROW = null;
		public static final MiningArrowItem DIAMOND_MINING_ARROW = null;
		public static final EnderLeadItem ENDER_LEAD = null;
		public static final Item PRISMATIC_WEB = null;
		public static final Item SCRAP_TOME = null;
		public static final Item OTHER_TOME = null;
		public static final Item HELMET_TOME = null;
		public static final Item CHESTPLATE_TOME = null;
		public static final Item LEGGINGS_TOME = null;
		public static final Item BOOTS_TOME = null;
		public static final Item WEAPON_TOME = null;
		public static final Item PICKAXE_TOME = null;
		public static final Item FISHING_TOME = null;
		public static final Item BOW_TOME = null;

		@ObjectHolder("wstweaks:fragment")
		public static final Item SKULL_FRAGMENT = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Enchantments {
		public static final EndlessQuiverEnchant ENDLESS_QUIVER = null;
		public static final CapturingEnchant CAPTURING = null;
		public static final BerserkersFuryEnchant BERSERKERS_FURY = null;
		public static final CrescendoEnchant CRESCENDO = null;
		public static final KnowledgeEnchant KNOWLEDGE = null;
		public static final LifeMendingEnchant LIFE_MENDING = null;
		public static final MinersFervorEnchant MINERS_FERVOR = null;
		public static final NaturesBlessingEnchant NATURES_BLESSING = null;
		public static final ReflectiveEnchant REFLECTIVE = null;
		public static final ScavengerEnchant SCAVENGER = null;
		public static final StableFootingEnchant STABLE_FOOTING = null;
		public static final TemptingEnchant TEMPTING = null;
		public static final ObliterationEnchant OBLITERATION = null;
		public static final SplittingEnchant SPLITTING = null;
		public static final InertEnchantment INFUSION = null;
		public static final ChromaticEnchant CHROMATIC = null;
		public static final ExploitationEnchant EXPLOITATION = null;
		public static final GrowthSerumEnchant GROWTH_SERUM = null;
		public static final EarthsBoonEnchant EARTHS_BOON = null;
		public static final ChainsawEnchant CHAINSAW = null;
		public static final SpearfishingEnchant SPEARFISHING = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Potions {
		public static final Potion RESISTANCE = null;
		public static final Potion LONG_RESISTANCE = null;
		public static final Potion STRONG_RESISTANCE = null;
		public static final Potion ABSORPTION = null;
		public static final Potion LONG_ABSORPTION = null;
		public static final Potion STRONG_ABSORPTION = null;
		public static final Potion HASTE = null;
		public static final Potion LONG_HASTE = null;
		public static final Potion STRONG_HASTE = null;
		public static final Potion FATIGUE = null;
		public static final Potion LONG_FATIGUE = null;
		public static final Potion STRONG_FATIGUE = null;
		public static final Potion SUNDERING = null;
		public static final Potion LONG_SUNDERING = null;
		public static final Potion STRONG_SUNDERING = null;
		public static final Potion KNOWLEDGE = null;
		public static final Potion LONG_KNOWLEDGE = null;
		public static final Potion STRONG_KNOWLEDGE = null;
		public static final Potion WITHER = null;
		public static final Potion LONG_WITHER = null;
		public static final Potion STRONG_WITHER = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Entities {
		public static final EntityType<ObsidianArrowEntity> OBSIDIAN_ARROW = null;
		public static final EntityType<BroadheadArrowEntity> BROADHEAD_ARROW = null;
		public static final EntityType<ExplosiveArrowEntity> EXPLOSIVE_ARROW = null;
		public static final EntityType<MiningArrowEntity> MINING_ARROW = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Effects {
		public static final SunderingEffect SUNDERING = null;
		public static final KnowledgeEffect KNOWLEDGE = null;
		public static final MobEffect BLEEDING = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Menus {
		public static final MenuType<FletchingContainer> FLETCHING = null;
		public static final MenuType<EnchLibraryContainer> LIBRARY = null;
		public static final MenuType<ApothEnchantContainer> ENCHANTING_TABLE = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Tiles {
		public static final BlockEntityType<EnchLibraryTile> LIBRARY = null;
		public static final BlockEntityType<AnvilTile> ANVIL = null;
		public static final BlockEntityType<EnchLibraryTile> ENDER_LIBRARY = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Attributes {
		/**
		 * Bonus to how fast a ranged weapon is charged. Base Value = (1.0) = 100%
		 */
		public static final Attribute DRAW_SPEED = null;
		/**
		 * Chance that a non-jump-attack will critically strike.  Base value = (1.0) = 0%
		 */
		public static final Attribute CRIT_CHANCE = null;
		/**
		 * Amount of damage caused by critical strikes. Base value = (1.0) = 100%
		 */
		public static final Attribute CRIT_DAMAGE = null;
		/**
		 * Bonus magic damage that slows enemies hit. Base value = (0.0) = 0 damage
		 */
		public static final Attribute COLD_DAMAGE = null;
		/**
		 * Bonus magic damage that burns enemies hit. Base value = (0.0) = 0 damage
		 */
		public static final Attribute FIRE_DAMAGE = null;
		/**
		 * Percent of physical damage converted to health. Base value = (1.0) = 0%
		 */
		public static final Attribute LIFE_STEAL = null;
		/**
		 * Percent of physical damage that bypasses armor. Base value = (1.0) = 0%
		 */
		public static final Attribute PIERCING = null;
		/**
		 * Bonus physical damage dealt equal to enemy's current health. Base value = (1.0) = 0%
		 */
		public static final Attribute CURRENT_HP_DAMAGE = null;
		/**
		 * Percent of physical damage converted to absorption hearts. Base value = (1.0) = 0%
		 */
		public static final Attribute OVERHEAL = null;
		/**
		 * Extra health that regenerates when not taking damage. Base value = (0.0) = 0 damage
		 */
		public static final Attribute GHOST_HEALTH = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Affixes {

	}

	public static final class Tags {
		public static final TagKey<Item> BOON_DROPS = ItemTags.create(new ResourceLocation(Apotheosis.MODID, "boon_drops"));
		public static final TagKey<Item> SPEARFISHING_DROPS = ItemTags.create(new ResourceLocation(Apotheosis.MODID, "spearfishing_drops"));
	}

	public static final class RecipeTypes {
		public static final RecipeType<FletchingRecipe> FLETCHING = PlaceboUtil.makeRecipeType(Apotheosis.MODID + ":fletching");
		public static final RecipeType<EnchantingRecipe> INFUSION = PlaceboUtil.makeRecipeType("apotheosis:enchanting");
		public static final RecipeType<SpawnerModifier> MODIFIER = PlaceboUtil.makeRecipeType("apotheosis:spawner_modifier");
	}

}