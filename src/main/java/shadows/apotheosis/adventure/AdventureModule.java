package shadows.apotheosis.adventure;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.adventure.affix.AffixManager;
import shadows.apotheosis.adventure.affix.reforging.ReforgingMenu;
import shadows.apotheosis.adventure.affix.reforging.ReforgingTableBlock;
import shadows.apotheosis.adventure.affix.reforging.ReforgingTableTile;
import shadows.apotheosis.adventure.affix.salvaging.SalvageItem;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingMenu;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingRecipe;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingTableBlock;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingTableTile;
import shadows.apotheosis.adventure.affix.socket.AddSocketsRecipe;
import shadows.apotheosis.adventure.affix.socket.ExpulsionRecipe;
import shadows.apotheosis.adventure.affix.socket.ExtractionRecipe;
import shadows.apotheosis.adventure.affix.socket.SocketingRecipe;
import shadows.apotheosis.adventure.affix.socket.UnnamingRecipe;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.affix.socket.gem.GemManager;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import shadows.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingBlock;
import shadows.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingMenu;
import shadows.apotheosis.adventure.boss.BossArmorManager;
import shadows.apotheosis.adventure.boss.BossDungeonFeature;
import shadows.apotheosis.adventure.boss.BossDungeonFeature2;
import shadows.apotheosis.adventure.boss.BossEvents;
import shadows.apotheosis.adventure.boss.BossItemManager;
import shadows.apotheosis.adventure.boss.BossSpawnerBlock;
import shadows.apotheosis.adventure.boss.BossSpawnerBlock.BossSpawnerTile;
import shadows.apotheosis.adventure.boss.BossSummonerItem;
import shadows.apotheosis.adventure.boss.Exclusion;
import shadows.apotheosis.adventure.boss.MinibossManager;
import shadows.apotheosis.adventure.client.AdventureModuleClient;
import shadows.apotheosis.adventure.compat.AdventureTOPPlugin;
import shadows.apotheosis.adventure.compat.AdventureTwilightCompat;
import shadows.apotheosis.adventure.compat.GatewaysCompat;
import shadows.apotheosis.adventure.loot.AffixConvertLootModifier;
import shadows.apotheosis.adventure.loot.AffixHookLootModifier;
import shadows.apotheosis.adventure.loot.AffixLootManager;
import shadows.apotheosis.adventure.loot.AffixLootModifier;
import shadows.apotheosis.adventure.loot.AffixLootPoolEntry;
import shadows.apotheosis.adventure.loot.GemLootModifier;
import shadows.apotheosis.adventure.loot.GemLootPoolEntry;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.adventure.loot.LootRarityManager;
import shadows.apotheosis.adventure.spawner.RandomSpawnerManager;
import shadows.apotheosis.adventure.spawner.RogueSpawnerFeature;
import shadows.apotheosis.ench.objects.GlowyBlockItem.GlowyItem;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.block_entity.TickingBlockEntityType;
import shadows.placebo.config.Configuration;
import shadows.placebo.container.ContainerUtil;
import shadows.placebo.loot.LootSystem;
import shadows.placebo.util.RegistryEvent.Register;

public class AdventureModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Adventure");

	public static final BiMap<LootRarity, Item> RARITY_MATERIALS = HashBiMap.create();

	public static final boolean STAGES_LOADED = ModList.get().isLoaded("gamestages");

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
		AffixManager.INSTANCE.registerToBus();
		GemManager.INSTANCE.registerToBus();
		AffixLootManager.INSTANCE.registerToBus();
		BossArmorManager.INSTANCE.registerToBus();
		BossItemManager.INSTANCE.registerToBus();
		RandomSpawnerManager.INSTANCE.registerToBus();
		LootRarityManager.INSTANCE.registerToBus();
		MinibossManager.INSTANCE.registerToBus();
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
			AdventureGeneration.init();
			Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(Apotheosis.MODID, "random_affix_item"), AffixLootPoolEntry.TYPE);
			Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(Apotheosis.MODID, "random_gem"), GemLootPoolEntry.TYPE);
			Exclusion.initSerializers();
			GemBonus.initCodecs();
		});
		MobEffects.BLINDNESS.addAttributeModifier(Attributes.FOLLOW_RANGE, "f8c3de3d-1fea-4d7c-a8b0-22f63c4c3454", -0.75, Operation.MULTIPLY_TOTAL);
	}

	@SubscribeEvent
	public void register(Register<Feature<?>> e) {
		e.getRegistry().register(BossDungeonFeature.INSTANCE, "boss_dng");
		e.getRegistry().register(BossDungeonFeature2.INSTANCE, "boss_dng_2");
		e.getRegistry().register(RogueSpawnerFeature.INSTANCE, "rogue_spawner");
		//e.getRegistry().register(TroveFeature.INSTANCE, "trove");
		//e.getRegistry().register(TomeTowerFeature.INSTANCE, "tome_tower");
		MinecraftForge.EVENT_BUS.register(AdventureGeneration.class);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().register(new GemItem(new Item.Properties().stacksTo(1)), "gem");
		e.getRegistry().register(new BossSummonerItem(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "boss_summoner");
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "gem_dust");
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "vial_of_extraction");
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "vial_of_expulsion");
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "vial_of_unnaming");
		for (LootRarity r : LootRarity.values()) {
			if (r == LootRarity.ANCIENT) continue;
			Item material = new SalvageItem(r, new Item.Properties().tab(Apotheosis.APOTH_GROUP));
			e.getRegistry().register(material, r.id() + "_material");
			RARITY_MATERIALS.put(r, material);
		}
		e.getRegistry().register(new BlockItem(Apoth.Blocks.SIMPLE_REFORGING_TABLE.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "simple_reforging_table");
		e.getRegistry().register(new BlockItem(Apoth.Blocks.REFORGING_TABLE.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "reforging_table");
		e.getRegistry().register(new BlockItem(Apoth.Blocks.SALVAGING_TABLE.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "salvaging_table");
		e.getRegistry().register(new BlockItem(Apoth.Blocks.GEM_CUTTING_TABLE.get(), new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "gem_cutting_table");
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "sigil_of_socketing");
		e.getRegistry().register(new GlowyItem(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "superior_sigil_of_socketing");
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "sigil_of_enhancement");
		e.getRegistry().register(new GlowyItem(new Item.Properties().tab(Apotheosis.APOTH_GROUP)), "superior_sigil_of_enhancement");
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		e.getRegistry().register(new BossSpawnerBlock(BlockBehaviour.Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).noLootTable()), "boss_spawner");
		e.getRegistry().register(new ReforgingTableBlock(BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(2, 20F), LootRarity.RARE), "simple_reforging_table");
		e.getRegistry().register(new ReforgingTableBlock(BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(4, 1000F), LootRarity.MYTHIC), "reforging_table");
		e.getRegistry().register(new SalvagingTableBlock(BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2.5F)), "salvaging_table");
		e.getRegistry().register(new GemCuttingBlock(BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2.5F)), "gem_cutting_table");
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
		e.getRegistry().register(ContainerUtil.makeType(ReforgingMenu::new), "reforging");
		e.getRegistry().register(ContainerUtil.makeType(SalvagingMenu::new), "salvage");
		e.getRegistry().register(new MenuType<>(GemCuttingMenu::new), "gem_cutting");
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

	public static class ApothUpgradeRecipe extends UpgradeRecipe {

		public ApothUpgradeRecipe(ResourceLocation pId, Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
			super(pId, pBase, pAddition, pResult);
		}
	}

}