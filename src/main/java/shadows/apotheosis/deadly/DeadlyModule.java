package shadows.apotheosis.deadly;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixEvents;
import shadows.apotheosis.deadly.affix.Affixes;
import shadows.apotheosis.deadly.affix.attributes.CustomAttributes;
import shadows.apotheosis.deadly.affix.recipe.AffixShardingRecipe;
import shadows.apotheosis.deadly.affix.recipe.SoulfireCookingRecipe;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.gen.DeadlyFeatures;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.deadly.objects.AffixTomeItem;
import shadows.apotheosis.deadly.objects.RarityShardItem;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.deadly.reload.BossArmorManager;
import shadows.apotheosis.deadly.reload.BossItemManager;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.config.Configuration;
import shadows.placebo.recipe.RecipeHelper;

import java.io.File;
import java.util.EnumMap;
import java.util.Locale;

public class DeadlyModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Deadly");

	public static final EnumMap<LootRarity, RarityShardItem> RARITY_SHARDS = new EnumMap<>(LootRarity.class);
	public static final EnumMap<LootRarity, AffixTomeItem> RARITY_TOMES = new EnumMap<>(LootRarity.class);

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		MinecraftForge.EVENT_BUS.register(new AffixEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reloads);

		DeadlyFeatures.init();
	}

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		this.reload(null);
		MinecraftForge.EVENT_BUS.register(new DeadlyModuleEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reload);
		DeadlyLoot.init();

		LootRarity[] vals = LootRarity.values();
		for (int i = 0; i < vals.length - 1; i++) {
			RecipeHelper.addRecipe(new AffixShardingRecipe(new ResourceLocation(Apotheosis.MODID, "affix_sharding_" + vals[i].name().toLowerCase(Locale.ROOT)), vals[i]));
			Apotheosis.HELPER.addShapeless(new ItemStack(RARITY_SHARDS.get(vals[i]), 2), new ItemStack(RARITY_SHARDS.get(vals[i + 1])));
		}
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		for (LootRarity r : LootRarity.values()) {
			RarityShardItem shard = new RarityShardItem(r, new Item.Properties().tab(Apotheosis.APOTH_GROUP));
			shard.setRegistryName(r.name().toLowerCase(Locale.ROOT) + "_shard");
			e.getRegistry().register(shard);
			RARITY_SHARDS.put(r, shard);
		}
		for (LootRarity r : LootRarity.values()) {
			AffixTomeItem tome = new AffixTomeItem(r, new Item.Properties().tab(Apotheosis.APOTH_GROUP));
			tome.setRegistryName(r.name().toLowerCase(Locale.ROOT) + "_tome");
			e.getRegistry().register(tome);
			RARITY_TOMES.put(r, tome);
		}
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		//TODO: pc3k
//		e.getRegistry().register(new BossSpawnerBlock(BlockBehaviour.Properties.of(Material.STONE).strength(-1).noDrops().noOcclusion()).setRegistryName("boss_spawner"));
	}

	@SubscribeEvent
	public void tiles(Register<BlockEntityType<?>> e) {
		//TODO: pc3k
//		e.getRegistry().register(new BlockEntityType<>(BossSpawnerTile::new, ImmutableSet.of(ApotheosisObjects.BOSS_SPAWNER), null).setRegistryName("boss_spawn_tile"));
	}

	@SubscribeEvent
	public void serializers(Register<RecipeSerializer<?>> e) {
		e.getRegistry().register(AffixShardingRecipe.SERIALIZER.setRegistryName(new ResourceLocation("affix_sharding")));
		e.getRegistry().register(SoulfireCookingRecipe.SERIALIZER.setRegistryName(new ResourceLocation("soulfire_cooking")));
	}

	@SubscribeEvent
	public void registry(NewRegistryEvent e) {
		RegistryBuilder<Affix> build = new RegistryBuilder<>();
		build.setName(new ResourceLocation(Apotheosis.MODID, "affixes"));
		build.setType(Affix.class);
		e.create(build, r -> Affix.REGISTRY = (ForgeRegistry<Affix>) r);
	}

	@SubscribeEvent
	public void attribs(Register<Attribute> e) { CustomAttributes.register(e); }

	@SubscribeEvent
	public void affixes(Register<Affix> e) { Affixes.register(e); }

	@SubscribeEvent
	public void client(FMLClientSetupEvent e) {
		e.enqueueWork(DeadlyModuleClient::init);
	}


	public void reloads(AddReloadListenerEvent e) {
		e.addListener(BossArmorManager.INSTANCE);
		e.addListener(BossItemManager.INSTANCE);
		e.addListener(AffixLootManager.INSTANCE);
		//TODO: pc3k
//		e.addListener(RandomSpawnerManager.INSTANCE);
	}

	/**
	 * Loads all configurable data for the deadly module.
	 */
	public void reload(ApotheosisReloadEvent e) {
		Configuration mainConfig = new Configuration(new File(Apotheosis.configDir, "deadly.cfg"));
		Configuration nameConfig = new Configuration(new File(Apotheosis.configDir, "names.cfg"));
		DeadlyConfig.load(mainConfig);
		NameHelper.load(nameConfig);
		if (e == null && mainConfig.hasChanged()) mainConfig.save();
		if (e == null && nameConfig.hasChanged()) nameConfig.save();
	}

	public static final boolean DEBUG = false;

	public static void debugLog(BlockPos pos, String name) {
		if (DEBUG) DeadlyModule.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
	}

}