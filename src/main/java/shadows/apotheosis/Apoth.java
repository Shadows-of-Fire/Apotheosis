package shadows.apotheosis;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.anvil.ObliterationEnchant;
import shadows.apotheosis.ench.anvil.SplittingEnchant;
import shadows.apotheosis.ench.enchantments.BerserkersFuryEnchant;
import shadows.apotheosis.ench.enchantments.CrescendoEnchant;
import shadows.apotheosis.ench.enchantments.InertEnchantment;
import shadows.apotheosis.ench.enchantments.KnowledgeEnchant;
import shadows.apotheosis.ench.enchantments.LifeMendingEnchant;
import shadows.apotheosis.ench.enchantments.MinersFervorEnchant;
import shadows.apotheosis.ench.enchantments.NaturesBlessingEnchant;
import shadows.apotheosis.ench.enchantments.ReflectiveEnchant;
import shadows.apotheosis.ench.enchantments.ScavengerEnchant;
import shadows.apotheosis.ench.enchantments.StableFootingEnchant;
import shadows.apotheosis.ench.enchantments.TemptingEnchant;
import shadows.apotheosis.ench.library.EnchLibraryBlock;
import shadows.apotheosis.ench.library.EnchLibraryContainer;
import shadows.apotheosis.ench.library.EnchLibraryTile;
import shadows.apotheosis.ench.table.ApothEnchantContainer;
import shadows.apotheosis.garden.EnderLeadItem;
import shadows.apotheosis.potion.LuckyFootItem;
import shadows.apotheosis.potion.PotionCharmItem;
import shadows.apotheosis.potion.TrueInfinityEnchant;
import shadows.apotheosis.potion.potions.KnowledgeEffect;
import shadows.apotheosis.potion.potions.SunderingEffect;
import shadows.apotheosis.spawn.enchantment.CapturingEnchant;
import shadows.apotheosis.village.fletching.FletchingContainer;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowEntity;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowItem;
import shadows.apotheosis.village.fletching.arrows.ExplosiveArrowEntity;
import shadows.apotheosis.village.fletching.arrows.ExplosiveArrowItem;
import shadows.apotheosis.village.fletching.arrows.MiningArrowEntity;
import shadows.apotheosis.village.fletching.arrows.MiningArrowItem;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowEntity;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowItem;

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
		public static final Block WEAK_RECTIFIER = null;
		public static final Block RECTIFIER = null;
		public static final Block STRONG_RECTIFIER = null;
		public static final Block REVEALER = null;
		public static final Block STRONG_REVEALER = null;
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

		@ObjectHolder("witherskelefix:fragment")
		public static final Item SKULL_FRAGMENT = null;
	}

	@ObjectHolder(Apotheosis.MODID)
	public static final class Enchantments {
		public static final TrueInfinityEnchant TRUE_INFINITY = null;
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
	}

	/*	public static final HellshelfBlock HELLSHELF = null;
	//	public static final Item PRISMATIC_WEB = null;
	//	public static final HellInfusionEnchantment HELL_INFUSION = null;
	//	public static final MinersFervorEnchant DEPTH_MINER = null;
	//	public static final StableFootingEnchant STABLE_FOOTING = null;
	//	public static final ScavengerEnchant SCAVENGER = null;
	//	public static final LifeMendingEnchant LIFE_MENDING = null;
	//	public static final IcyThornsEnchant ICY_THORNS = null;
	//	public static final TemptingEnchant TEMPTING = null;
	//	public static final ShieldBashEnchant SHIELD_BASH = null;
	//	public static final ReflectiveEnchant REFLECTIVE = null;
	//	public static final BerserkersFuryEnchant BERSERK = null;
	//	public static final CapturingEnchant CAPTURING = null;
	
	//	public static final KnowledgeEnchant KNOWLEDGE = null;
	//	public static final SplittingEnchant SPLITTING = null;
	//	public static final NaturesBlessingEnchant NATURES_BLESSING = null;
	//	public static final ReboundingEnchant REBOUNDING = null;
	//	public static final TomeItem NULL_BOOK = null;
	//	public static final TomeItem ARMOR_HEAD_BOOK = null;
	//	public static final TomeItem ARMOR_CHEST_BOOK = null;
	//	public static final TomeItem ARMOR_LEGS_BOOK = null;
	//	public static final TomeItem ARMOR_FEET_BOOK = null;
	//	public static final TomeItem WEAPON_BOOK = null;
	//	public static final TomeItem DIGGER_BOOK = null;
	//	public static final TomeItem FISHING_ROD_BOOK = null;
	//	public static final TomeItem BOW_BOOK = null;
	//	public static final SeaAltarBlock PRISMATIC_ALTAR = null;
	public static final SoundEvent ALTAR_SOUND = null;
	//	public static final MagicProtEnchant MAGIC_PROTECTION = null;
	//	public static final ScrappingTomeItem SCRAP_TOME = null;
	//	public static final BlockEntityType<AnvilTile> ANVIL = null;
	//	@ObjectHolder("apotheosis:prismatic_altar")
	//	public static final BlockEntityType<SeaAltarTile> ALTAR_TYPE = null;
	public static final ObsidianArrowItem OBSIDIAN_ARROW = null;
	public static final EntityType<ObsidianArrowEntity> OB_ARROW_ENTITY = null;
	public static final MobEffect BLEEDING = null;
	public static final EntityType<BroadheadArrowEntity> BH_ARROW_ENTITY = null;
	public static final BroadheadArrowItem BROADHEAD_ARROW = null;
	
	//	@ObjectHolder("minecraft:enchanting_table")
	//	public static final BlockEntityType<ApothEnchantTile> ENCHANTING_TABLE = null;
	//	public static final MenuType<ApothEnchantContainer> ENCHANTING = null;
	//	public static final SeaInfusionEnchantment SEA_INFUSION = null;
	//	public static final SeashelfBlock SEASHELF = null;
	public static final Block BLAZING_HELLSHELF = null;
	public static final Block GLOWING_HELLSHELF = null;
	public static final Block CRYSTAL_SEASHELF = null;
	public static final Block HEART_SEASHELF = null;
	public static final Block ENDSHELF = null;
	public static final Block PEARL_ENDSHELF = null;
	public static final Block DRACONIC_ENDSHELF = null;
	public static final Block BEESHELF = null;
	public static final Block MELONSHELF = null;
	public static final Enchantment OBLITERATION = null;
	public static final Enchantment CRESCENDO = null;
	public static final ObsidianArrowItem OBSIDIAN_ARROW = null;
	public static final EntityType<ObsidianArrowEntity> OB_ARROW_ENTITY = null;
	public static final MobEffect BLEEDING = null;
	public static final EntityType<BroadheadArrowEntity> BH_ARROW_ENTITY = null;
	public static final BroadheadArrowItem BROADHEAD_ARROW = null;
	public static final EntityType<ExplosiveArrowEntity> EX_ARROW_ENTITY = null;
	public static final ExplosiveArrowItem EXPLOSIVE_ARROW = null;
	public static final EntityType<MiningArrowEntity> MN_ARROW_ENTITY = null;
	public static final MiningArrowItem IRON_MINING_ARROW = null;
	public static final MiningArrowItem DIAMOND_MINING_ARROW = null;
	//	public static final BossSpawnerBlock BOSS_SPAWNER = null;
	//public static final BlockEntityType<BossSpawnerTile> BOSS_SPAWN_TILE = null;
	public static final Block ENCHANTMENT_LIBRARY = null;
	//public static final BlockEntityType<EnchLibraryTile> ENCH_LIB_TILE = null;
	//public static final MenuType<EnchLibraryContainer> ENCH_LIB_CON = null;
	*/
}