package shadows.apotheosis.adventure;

import java.io.File;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.affix.AttributeAffix;
import shadows.apotheosis.adventure.affix.effect.CatalyzingAffix;
import shadows.apotheosis.adventure.affix.effect.CleavingAffix;
import shadows.apotheosis.adventure.affix.effect.DamageReductionAffix;
import shadows.apotheosis.adventure.affix.effect.DamageReductionAffix.DamageType;
import shadows.apotheosis.adventure.affix.effect.DurableAffix;
import shadows.apotheosis.adventure.affix.effect.EnlightenedAffix;
import shadows.apotheosis.adventure.affix.effect.ExecutingAffix;
import shadows.apotheosis.adventure.affix.effect.FestiveAffix;
import shadows.apotheosis.adventure.affix.effect.MagicalArrowAffix;
import shadows.apotheosis.adventure.affix.effect.OmneticAffix;
import shadows.apotheosis.adventure.affix.effect.PotionAffix;
import shadows.apotheosis.adventure.affix.effect.PotionAffix.Target;
import shadows.apotheosis.adventure.affix.effect.PsychicAffix;
import shadows.apotheosis.adventure.affix.effect.RadialAffix;
import shadows.apotheosis.adventure.affix.effect.RetreatingAffix;
import shadows.apotheosis.adventure.affix.effect.SpectralShotAffix;
import shadows.apotheosis.adventure.affix.effect.TelepathicAffix;
import shadows.apotheosis.adventure.affix.effect.ThunderstruckAffix;
import shadows.apotheosis.adventure.affix.reforging.ReforgingMenu;
import shadows.apotheosis.adventure.affix.reforging.ReforgingTableBlock;
import shadows.apotheosis.adventure.affix.reforging.ReforgingTableTile;
import shadows.apotheosis.adventure.affix.salvaging.SalvageItem;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingMenu;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingTableBlock;
import shadows.apotheosis.adventure.affix.socket.ExpulsionRecipe;
import shadows.apotheosis.adventure.affix.socket.ExtractionRecipe;
import shadows.apotheosis.adventure.affix.socket.GemItem;
import shadows.apotheosis.adventure.affix.socket.GemManager;
import shadows.apotheosis.adventure.affix.socket.SocketAffix;
import shadows.apotheosis.adventure.affix.socket.SocketingRecipe;
import shadows.apotheosis.adventure.boss.BossArmorManager;
import shadows.apotheosis.adventure.boss.BossDungeonFeature;
import shadows.apotheosis.adventure.boss.BossDungeonFeature2;
import shadows.apotheosis.adventure.boss.BossEvents;
import shadows.apotheosis.adventure.boss.BossItemManager;
import shadows.apotheosis.adventure.boss.BossSpawnerBlock;
import shadows.apotheosis.adventure.boss.BossSpawnerBlock.BossSpawnerTile;
import shadows.apotheosis.adventure.boss.BossSummonerItem;
import shadows.apotheosis.adventure.client.AdventureModuleClient;
import shadows.apotheosis.adventure.compat.AdventureTOPPlugin;
import shadows.apotheosis.adventure.compat.GatewaysCompat;
import shadows.apotheosis.adventure.loot.AffixConvertLootModifier;
import shadows.apotheosis.adventure.loot.AffixLootManager;
import shadows.apotheosis.adventure.loot.AffixLootModifier;
import shadows.apotheosis.adventure.loot.GemLootModifier;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.adventure.spawner.RandomSpawnerManager;
import shadows.apotheosis.adventure.spawner.RogueSpawnerFeature;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.block_entity.TickingBlockEntityType;
import shadows.placebo.config.Configuration;
import shadows.placebo.container.ContainerUtil;
import shadows.placebo.loot.LootSystem;
import shadows.placebo.util.StepFunction;

public class AdventureModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Adventure");

	public static final BiMap<LootRarity, IRegistryDelegate<Item>> RARITY_MATERIALS = HashBiMap.create();

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR, 40D, "f_22308_");
		ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR_TOUGHNESS, 30D, "f_22308_");
	}

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		this.reload(null);
		MinecraftForge.EVENT_BUS.register(new AdventureEvents());
		MinecraftForge.EVENT_BUS.register(new BossEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reload);
		GemManager.INSTANCE.registerToBus();
		AffixLootManager.INSTANCE.registerToBus();
		BossArmorManager.INSTANCE.registerToBus();
		BossItemManager.INSTANCE.registerToBus();
		RandomSpawnerManager.INSTANCE.registerToBus();
		Apotheosis.HELPER.registerProvider(f -> {
			f.addRecipe(new SocketingRecipe());
			f.addRecipe(new ExpulsionRecipe());
			f.addRecipe(new ExtractionRecipe());
			Item g = Apoth.Items.GEM_DUST;
			f.addShaped(Apoth.Items.VIAL_OF_EXPULSION, 3, 3, g, Items.MAGMA_CREAM, g, Items.BLAZE_ROD, Apotheosis.potionIngredient(Potions.THICK), Items.BLAZE_ROD, g, Items.LAVA_BUCKET, g);
			f.addShaped(Apoth.Items.VIAL_OF_EXTRACTION, 3, 3, g, Items.AMETHYST_SHARD, g, Items.ENDER_PEARL, Apotheosis.potionIngredient(Potions.THICK), Items.ENDER_PEARL, g, Items.WATER_BUCKET, g);
		});
		e.enqueueWork(() -> {
			if (ModList.get().isLoaded("gateways")) GatewaysCompat.register();
			if (ModList.get().isLoaded("theoneprobe")) AdventureTOPPlugin.register();
			LootSystem.defaultBlockTable(Apoth.Blocks.REFORGING_TABLE);
			LootSystem.defaultBlockTable(Apoth.Blocks.SALVAGING_TABLE);
		});
	}

	@SubscribeEvent
	public void register(Register<Feature<?>> e) {
		e.getRegistry().register(BossDungeonFeature.INSTANCE.setRegistryName("boss_dng"));
		e.getRegistry().register(BossDungeonFeature2.INSTANCE.setRegistryName("boss_dng_2"));
		e.getRegistry().register(RogueSpawnerFeature.INSTANCE.setRegistryName("rogue_spawner"));
		//e.getRegistry().register(TroveFeature.INSTANCE.setRegistryName("trove"));
		//e.getRegistry().register(TomeTowerFeature.INSTANCE.setRegistryName("tome_tower"));
		MinecraftForge.EVENT_BUS.register(AdventureGeneration.class);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().register(new GemItem(new Item.Properties().stacksTo(1)).setRegistryName("gem"));
		e.getRegistry().register(new BossSummonerItem(new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("boss_summoner"));
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("gem_dust"));
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("vial_of_extraction"));
		e.getRegistry().register(new Item(new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("vial_of_expulsion"));
		for (LootRarity r : LootRarity.values()) {
			if (r == LootRarity.ANCIENT) continue;
			Item material = new SalvageItem(r, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName(r.id() + "_material");
			e.getRegistry().register(material);
			RARITY_MATERIALS.put(r, material.delegate);
		}
		e.getRegistry().register(new BlockItem(Apoth.Blocks.REFORGING_TABLE, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("reforging_table"));
		e.getRegistry().register(new BlockItem(Apoth.Blocks.SALVAGING_TABLE, new Item.Properties().tab(Apotheosis.APOTH_GROUP)).setRegistryName("salvaging_table"));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		e.getRegistry().register(new BossSpawnerBlock(BlockBehaviour.Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).noDrops()).setRegistryName("boss_spawner"));
		e.getRegistry().register(new ReforgingTableBlock(BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(5, 1000F)).setRegistryName("reforging_table"));
		e.getRegistry().register(new SalvagingTableBlock(BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2.5F)).setRegistryName("salvaging_table"));
	}

	@SubscribeEvent
	public void tiles(Register<BlockEntityType<?>> e) {
		e.getRegistry().register(new TickingBlockEntityType<>(BossSpawnerTile::new, ImmutableSet.of(Apoth.Blocks.BOSS_SPAWNER), false, true).setRegistryName("boss_spawner"));
		e.getRegistry().register(new TickingBlockEntityType<>(ReforgingTableTile::new, ImmutableSet.of(Apoth.Blocks.REFORGING_TABLE), true, false).setRegistryName("reforging_table"));
	}

	@SubscribeEvent
	public void serializers(Register<RecipeSerializer<?>> e) {
		e.getRegistry().register(SocketingRecipe.Serializer.INSTANCE.setRegistryName("socketing"));
		e.getRegistry().register(ExpulsionRecipe.Serializer.INSTANCE.setRegistryName("expulsion"));
		e.getRegistry().register(ExtractionRecipe.Serializer.INSTANCE.setRegistryName("extraction"));
	}

	@SubscribeEvent
	public void lootSerializers(Register<GlobalLootModifierSerializer<?>> e) {
		e.getRegistry().register(new GemLootModifier.Serializer().setRegistryName("gems"));
		e.getRegistry().register(new AffixLootModifier.Serializer().setRegistryName("affix_loot"));
		e.getRegistry().register(new AffixConvertLootModifier.Serializer().setRegistryName("affix_conversion"));
	}

	@SubscribeEvent
	public void containers(Register<MenuType<?>> e) {
		e.getRegistry().register(ContainerUtil.makeType(ReforgingMenu::new).setRegistryName("reforging"));
		e.getRegistry().register(new MenuType<>(SalvagingMenu::new).setRegistryName("salvage"));
	}

	@SubscribeEvent
	public void attribs(Register<Attribute> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new RangedAttribute("apotheosis:draw_speed", 1.0D, 1.0D, 4.0D).setSyncable(true).setRegistryName("draw_speed"),
				new RangedAttribute("apotheosis:crit_chance", 1.0D, 1.0D, 5.0D).setSyncable(true).setRegistryName("crit_chance"),
				new RangedAttribute("apotheosis:crit_damage", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("crit_damage"),
				new RangedAttribute("apotheosis:cold_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("cold_damage"),
				new RangedAttribute("apotheosis:fire_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("fire_damage"),
				new RangedAttribute("apotheosis:life_steal", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("life_steal"),
				new RangedAttribute("apotheosis:piercing", 1.0D, 1.0D, 2.0D).setSyncable(true).setRegistryName("piercing"),
				new RangedAttribute("apotheosis:current_hp_damage", 1.0D, 1.0D, 2.0D).setSyncable(true).setRegistryName("current_hp_damage"),
				new RangedAttribute("apotheosis:overheal", 1.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("overheal"),
				new RangedAttribute("apotheosis:ghost_health", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("ghost_health"),
				new RangedAttribute("apotheosis:mining_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("mining_speed"),
				new RangedAttribute("apotheosis:arrow_damage", 1.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("arrow_damage"),
				new RangedAttribute("apotheosis:arrow_velocity", 1.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("arrow_velocity")
		);
		//Formatter::on
	}

	@SubscribeEvent
	public void applyAttribs(EntityAttributeModificationEvent e) {
		e.getTypes().forEach(type -> {
			//Formatter::off
			addAll(type, e::add,
					Apoth.Attributes.DRAW_SPEED,
					Apoth.Attributes.CRIT_CHANCE,
					Apoth.Attributes.CRIT_DAMAGE,
					Apoth.Attributes.COLD_DAMAGE,
					Apoth.Attributes.FIRE_DAMAGE,
					Apoth.Attributes.LIFE_STEAL,
					Apoth.Attributes.PIERCING,
					Apoth.Attributes.CURRENT_HP_DAMAGE,
					Apoth.Attributes.OVERHEAL,
					Apoth.Attributes.GHOST_HEALTH,
					Apoth.Attributes.MINING_SPEED,
					Apoth.Attributes.ARROW_DAMAGE,
					Apoth.Attributes.ARROW_VELOCITY);
			//Formatter::on
		});
	}

	private static void addAll(EntityType<? extends LivingEntity> type, BiConsumer<EntityType<? extends LivingEntity>, Attribute> add, Attribute... attribs) {
		for (Attribute a : attribs)
			add.accept(type, a);
	}

	@SubscribeEvent
	public void affixes(Register<Affix> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				// Defensive Affixes
				new AttributeAffix.Builder(() -> Attributes.MAX_HEALTH, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.5F, 3, 0.5F))
				.with(LootRarity.UNCOMMON, step(1.5F, 5, 0.5F))
				.with(LootRarity.RARE, step(3F, 8, 0.5F))
				.with(LootRarity.EPIC, step(5F, 10, 0.5F))
				.with(LootRarity.MYTHIC, step(8F, 14, 0.5F))
				.with(LootRarity.ANCIENT, step(12F, 20, 0.5F))
				.types(LootCategory::isDefensive).build("blessed"),

				new AttributeAffix.Builder(() -> Attributes.ARMOR, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.25F, 3, 0.25F))
				.with(LootRarity.UNCOMMON, step(1F, 8, 0.25F))
				.with(LootRarity.RARE, step(3F, 10, 0.25F))
				.with(LootRarity.EPIC, step(5F, 16, 0.25F))
				.with(LootRarity.MYTHIC, step(7F, 24, 0.25F))
				.with(LootRarity.ANCIENT, step(9F, 30, 0.25F))
				.types(LootCategory::isDefensive).build("ironforged"),

				new AttributeAffix.Builder(ForgeMod.SWIM_SPEED, Operation.MULTIPLY_TOTAL)
				.with(LootRarity.COMMON, step(0.05F, 10, 0.02F))
				.with(LootRarity.UNCOMMON, step(0.08F, 10, 0.02F))
				.with(LootRarity.RARE, step(0.12F, 10, 0.02F))
				.with(LootRarity.EPIC, step(0.15F, 12, 0.02F))
				.with(LootRarity.MYTHIC, step(0.18F, 14, 0.02F))
				.with(LootRarity.ANCIENT, step(0.25F, 10, 0.02F))
				.types(l -> l == LootCategory.ARMOR).items(s -> ((ArmorItem) s.getItem()).getSlot() == EquipmentSlot.FEET).build("aquatic"),

				new AttributeAffix.Builder(ForgeMod.ENTITY_GRAVITY, Operation.MULTIPLY_TOTAL)
				.with(LootRarity.COMMON, step(-0.05F, 2, -0.02F))
				.with(LootRarity.UNCOMMON, step(-0.05F, 4, -0.02F))
				.with(LootRarity.RARE, step(-0.05F, 6, -0.02F))
				.with(LootRarity.EPIC, step(-0.05F, 8, -0.02F))
				.with(LootRarity.MYTHIC, step(-0.05F, 10, -0.02F))
				.with(LootRarity.ANCIENT, step(-0.05F, 10, -0.02F))
				.types(l -> l == LootCategory.ARMOR).items(s -> ((ArmorItem) s.getItem()).getSlot() == EquipmentSlot.CHEST).build("gravitational"),

				new AttributeAffix.Builder(() -> Attributes.ARMOR_TOUGHNESS, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(0.25F, 2, 0.25F))
				.with(LootRarity.RARE, step(1F, 4, 0.25F))
				.with(LootRarity.EPIC, step(2F, 7, 0.25F))
				.with(LootRarity.MYTHIC, step(4F, 12, 0.25F))
				.with(LootRarity.ANCIENT, step(6F, 18, 0.25F))
				.types(LootCategory::isDefensive).build("steel_touched"),

				new AttributeAffix.Builder(() -> Attributes.KNOCKBACK_RESISTANCE, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(0.04F, 5, 0.02F))
				.with(LootRarity.RARE, step(0.06F, 6, 0.02F))
				.with(LootRarity.EPIC, step(0.1F, 10, 0.02F))
				.with(LootRarity.MYTHIC, step(0.2F, 14, 0.02F))
				.with(LootRarity.ANCIENT, step(0.4F, 16, 0.02F))
				.types(LootCategory::isDefensive).build("stalwart"),

				new PotionAffix.Builder(() -> MobEffects.DAMAGE_RESISTANCE)
				.with(LootRarity.RARE, step(20, 30, 2), step(0, 1, 0))
				.with(LootRarity.EPIC, step(40, 50, 2), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(60, 70, 2), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(100, 100, 4), step(1, 2, 1))
				.types(l -> l == LootCategory.ARMOR)
				.build(AffixType.EFFECT, Target.HURT_SELF, "bolstering"),

				new PotionAffix.Builder(() -> MobEffects.HEAL)
				.with(LootRarity.EPIC, step(1, 1, 0), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(1, 1, 0), step(0, 2, 1))
				.with(LootRarity.ANCIENT, step(1, 1, 0), step(1, 3, 1))
				.types(l -> l == LootCategory.ARMOR)
				.build(AffixType.EFFECT, Target.HURT_SELF, "revitalizing"),

				new AttributeAffix.Builder(() -> Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL)
				.with(LootRarity.COMMON, step(0.05F, 10, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.08F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.12F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.15F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.18F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.25F, 10, 0.01F))
				.types(l -> l.isRanged() || l == LootCategory.ARMOR).build("windswept"),

				new AttributeAffix.Builder(ForgeMod.STEP_HEIGHT_ADDITION, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(0.25F, 1, 0.25F))
				.with(LootRarity.RARE, step(0.5F, 3, 0.25F))
				.with(LootRarity.EPIC, step(0.5F, 4, 0.25F))
				.with(LootRarity.MYTHIC, step(1F, 6, 0.25F))
				.with(LootRarity.ANCIENT, step(1.5F, 7, 0.25F))
				.types(l -> l == LootCategory.ARMOR).items(s -> ((ArmorItem) s.getItem()).getSlot() == EquipmentSlot.FEET).build("elastic"),

				new AttributeAffix.Builder(() -> Attributes.LUCK, Operation.ADDITION)
				.with(LootRarity.RARE, step(1.5F, 8, 0.25F))
				.with(LootRarity.EPIC, step(2.5F, 10, 0.25F))
				.with(LootRarity.MYTHIC, step(4.5F, 12, 0.25F))
				.with(LootRarity.ANCIENT, step(8F, 16, 0.25F))
				.types(l -> l == LootCategory.ARMOR).build("fortunate"),

				// Light Weapon Affixes
				new AttributeAffix.Builder(() -> Attributes.ATTACK_DAMAGE, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.5F, 4, 0.25F))
				.with(LootRarity.UNCOMMON, step(1F, 10, 0.25F))
				.with(LootRarity.RARE, step(2F, 16, 0.25F))
				.with(LootRarity.EPIC, step(6F, 18, 0.25F))
				.with(LootRarity.MYTHIC, step(8F, 24, 0.25F))
				.with(LootRarity.ANCIENT, step(12F, 30, 0.25F))
				.types(LootCategory::isLightWeapon).build("violent"),

				new AttributeAffix.Builder(() -> Attributes.ATTACK_SPEED, Operation.MULTIPLY_TOTAL)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.20F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.35F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.50F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.60F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.80F, 10, 0.01F))
				.types(LootCategory::isLightWeapon).build("graceful"),

				new AttributeAffix.Builder(ForgeMod.ATTACK_RANGE, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.5F, 4, 0.25F))
				.with(LootRarity.UNCOMMON, step(0.75F, 5, 0.25F))
				.with(LootRarity.RARE, step(1F, 6, 0.25F))
				.with(LootRarity.EPIC, step(1.5F, 8, 0.25F))
				.with(LootRarity.MYTHIC, step(2F, 10, 0.25F))
				.with(LootRarity.ANCIENT, step(2.5F, 16, 0.25F))
				.types(LootCategory::isLightWeapon).build("elongated"),

				// Heavy Weapon Affixes
				new AttributeAffix.Builder(() -> Attributes.ATTACK_DAMAGE, Operation.ADDITION)
				.with(LootRarity.COMMON, step(2F, 8, 0.25F))
				.with(LootRarity.UNCOMMON, step(4F, 12, 0.25F))
				.with(LootRarity.RARE, step(5.5F, 20, 0.25F))
				.with(LootRarity.EPIC, step(7F, 24, 0.25F))
				.with(LootRarity.MYTHIC, step(10F, 30, 0.25F))
				.with(LootRarity.ANCIENT, step(16F, 36, 0.25F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("murderous"),

				new AttributeAffix.Builder(() -> Attributes.ATTACK_KNOCKBACK, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.25F, 10, 0.025F))
				.with(LootRarity.UNCOMMON, step(0.75F, 20, 0.025F))
				.with(LootRarity.RARE, step(1.5F, 30, 0.025F))
				.with(LootRarity.EPIC, step(2F, 30, 0.025F))
				.with(LootRarity.MYTHIC, step(3.5F, 30, 0.025F))
				.with(LootRarity.ANCIENT, l -> 5F)
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("forceful"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.PIERCING, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.25F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.40F, 7, 0.01F))
				.with(LootRarity.RARE, step(0.50F, 8, 0.01F))
				.with(LootRarity.EPIC, step(0.70F, 10, 0.01F))
				.with(LootRarity.MYTHIC, step(0.85F, 15, 0.01F))
				.with(LootRarity.ANCIENT, step(0.90F, 20, 0.01F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("shredding"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.CURRENT_HP_DAMAGE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.01F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.04F, 6, 0.01F))
				.with(LootRarity.RARE, step(0.10F, 8, 0.01F))
				.with(LootRarity.EPIC, step(0.14F, 10, 0.01F))
				.with(LootRarity.MYTHIC, step(0.18F, 16, 0.01F))
				.with(LootRarity.ANCIENT, step(0.25F, 20, 0.01F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("giant_slaying"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.OVERHEAL, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.15F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.25F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.40F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.50F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.65F, 20, 0.01F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("berserking"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.CRIT_CHANCE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 15, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.20F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.35F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.40F, 10, 0.01F))
				.with(LootRarity.MYTHIC, step(0.65F, 15, 0.01F))
				.with(LootRarity.ANCIENT, step(0.85F, 20, 0.01F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("annihilating"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.CRIT_DAMAGE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 8, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.15F, 15, 0.01F))
				.with(LootRarity.RARE, step(0.25F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.40F, 10, 0.01F))
				.with(LootRarity.MYTHIC, step(0.55F, 10, 0.01F))
				.with(LootRarity.ANCIENT, step(0.75F, 15, 0.01F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("decimating"),

				new AttributeAffix.Builder(ForgeMod.REACH_DISTANCE, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.5F, 6, 0.25F))
				.with(LootRarity.UNCOMMON, step(0.75F, 8, 0.25F))
				.with(LootRarity.RARE, step(1F, 10, 0.25F))
				.with(LootRarity.EPIC, step(1.5F, 12, 0.25F))
				.with(LootRarity.MYTHIC, step(2F, 14, 0.25F))
				.with(LootRarity.ANCIENT, step(2.5F, 20, 0.25F))
				.types(l -> l == LootCategory.BREAKER).build("lengthy"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.DRAW_SPEED, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, l -> l > 0.5F ? 0.2F : 0.1F)
				.with(LootRarity.UNCOMMON, l -> l > 0.5F ? 0.33F : 0.25F)
				.with(LootRarity.RARE, l -> l > 0.5F ? 1F : 0.5F)
				.with(LootRarity.EPIC, l -> l > 0.5F ? 1.2F : 1.1F)
				.with(LootRarity.MYTHIC, l -> l > 0.5F ? 1.5F : 1.33F)
				.with(LootRarity.ANCIENT, l -> l > 0.5F ? 2.5F : 2F)
				.types(LootCategory::isRanged).build("agile"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.CRIT_CHANCE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.01F, 10, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.1F, 8, 0.01F))
				.with(LootRarity.RARE, step(0.2F, 7, 0.01F))
				.with(LootRarity.EPIC, step(0.3F, 5, 0.01F))
				.with(LootRarity.MYTHIC, step(0.45F, 10, 0.01F))
				.with(LootRarity.ANCIENT, step(0.65F, 20, 0.01F))
				.types(LootCategory::isLightWeapon).build("intricate"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.CRIT_DAMAGE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.01F, 9, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.05F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.1F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.2F, 10, 0.01F))
				.with(LootRarity.MYTHIC, step(0.3F, 10, 0.01F))
				.with(LootRarity.ANCIENT, step(0.5F, 15, 0.01F))
				.types(LootCategory::isLightWeapon).build("lacerating"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.COLD_DAMAGE, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(1F, 6, 0.25F))
				.with(LootRarity.RARE, step(2F, 10, 0.25F))
				.with(LootRarity.EPIC, step(4F, 14, 0.25F))
				.with(LootRarity.MYTHIC, step(5F, 17, 0.25F))
				.with(LootRarity.ANCIENT, step(7F, 20, 0.25F))
				.types(LootCategory::isLightWeapon).build("glacial"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.FIRE_DAMAGE, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(1F, 6, 0.25F))
				.with(LootRarity.RARE, step(2F, 10, 0.25F))
				.with(LootRarity.EPIC, step(4F, 14, 0.25F))
				.with(LootRarity.MYTHIC, step(5F, 17, 0.25F))
				.with(LootRarity.ANCIENT, step(7F, 20, 0.25F))
				.types(LootCategory::isLightWeapon).build("infernal"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.LIFE_STEAL, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(LootCategory::isLightWeapon).build("vampiric"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.MINING_SPEED, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.20F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.35F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.50F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.65F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.90F, 10, 0.01F))
				.types(l -> l == LootCategory.BREAKER).build("destructive"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.ARROW_DAMAGE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.20F, 15, 0.01F))
				.with(LootRarity.RARE, step(0.30F, 20, 0.01F))
				.with(LootRarity.EPIC, step(0.45F, 30, 0.01F))
				.with(LootRarity.MYTHIC, step(0.60F, 40, 0.01F))
				.with(LootRarity.ANCIENT, step(0.75F, 50, 0.01F))
				.types(LootCategory::isRanged).build("elven"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.ARROW_VELOCITY, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(LootCategory::isRanged).build("streamlined"),

				new SocketAffix().setRegistryName("socket"),
				new DurableAffix().setRegistryName("durable"),

				new PotionAffix.Builder(() -> MobEffects.MOVEMENT_SPEED)
				.with(LootRarity.RARE, step(100, 5, 20), step(0, 1, 0))
				.with(LootRarity.EPIC, step(140, 8, 20), step(0, 1, 0))
				.with(LootRarity.MYTHIC, step(180, 10, 20), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(240, 10, 40), step(2, 1, 0))
				.types(LootCategory::isWeapon)
				.build(AffixType.EFFECT, Target.ATTACK_SELF, "elusive"),

				new PotionAffix.Builder(() -> MobEffects.DIG_SPEED)
				.with(LootRarity.RARE, step(100, 5, 20), step(0, 1, 0))
				.with(LootRarity.EPIC, step(140, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(180, 10, 20), step(0, 1, 2))
				.with(LootRarity.ANCIENT, step(240, 10, 40), step(2, 1, 0))
				.types(l -> l == LootCategory.BREAKER)
				.build(AffixType.EFFECT, Target.BREAK_SELF, "swift"),

				new PotionAffix.Builder(() -> MobEffects.MOVEMENT_SLOWDOWN)
				.with(LootRarity.RARE, step(20, 2, 20), step(0, 1, 0))
				.with(LootRarity.EPIC, step(60, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(80, 10, 20), step(0, 1, 2))
				.with(LootRarity.ANCIENT, step(120, 10, 40), step(2, 1, 0))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "ensnaring"),

				new PotionAffix.Builder(() -> MobEffects.POISON)
				.with(LootRarity.RARE, step(60, 5, 20), step(0, 1, 0))
				.with(LootRarity.EPIC, step(100, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(120, 10, 20), step(0, 1, 2))
				.with(LootRarity.ANCIENT, step(160, 10, 40), step(2, 1, 0))
				.types(l -> l == LootCategory.SHIELD)
				.build(AffixType.EFFECT, Target.BLOCK_ATTACKER, "venomous"),

				new PotionAffix.Builder(() -> MobEffects.MOVEMENT_SPEED)
				.with(LootRarity.RARE, step(60, 5, 20), step(0, 1, 0))
				.with(LootRarity.EPIC, step(80, 8, 20), step(0, 1, 0))
				.with(LootRarity.MYTHIC, step(100, 10, 20), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(120, 10, 40), step(2, 1, 0))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_SELF, "fleeting"),

				new PotionAffix.Builder(() -> MobEffects.WEAKNESS)
				.with(LootRarity.RARE, step(40, 5, 20), step(0, 1, 0))
				.with(LootRarity.EPIC, step(60, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(80, 10, 20), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(120, 10, 40), step(2, 1, 0))
				.types(LootCategory::isWeapon)
				.build(AffixType.EFFECT, Target.ATTACK_TARGET, "weakening"),

				new PotionAffix.Builder(() -> MobEffects.LEVITATION)
				.with(LootRarity.EPIC, step(10, 3, 10), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(10, 4, 10), step(0, 2, 1))
				.with(LootRarity.ANCIENT, step(20, 5, 10), step(1, 3, 1))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "shulkers"),

				new PotionAffix.Builder(() -> MobEffects.WITHER)
				.with(LootRarity.EPIC, step(40, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(60, 10, 20), step(0, 1, 2))
				.with(LootRarity.ANCIENT, step(100, 10, 40), step(2, 1, 0))
				.types(l -> l == LootCategory.SHIELD)
				.build(AffixType.EFFECT, Target.BLOCK_ATTACKER, "withering"),

				new PotionAffix.Builder(() -> MobEffects.POISON)
				.with(LootRarity.EPIC, step(20, 7, 10), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(20, 9, 10), step(0, 2, 1))
				.with(LootRarity.ANCIENT, step(40, 13, 10), step(1, 3, 1))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "ivy_laced"),

				new PotionAffix.Builder(() -> MobEffects.WITHER)
				.with(LootRarity.MYTHIC, step(140, 3, 20), step(1, 3, 1))
				.with(LootRarity.ANCIENT, step(180, 5, 20), step(1, 3, 1))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "satanic"),

				new DamageReductionAffix.Builder(DamageType.PHYSICAL)
				.with(LootRarity.EPIC, step(0.05F, 5, 0.01F))
				.with(LootRarity.MYTHIC, step(0.05F, 10, 0.01F))
				.with(LootRarity.ANCIENT, step(0.05F, 20, 0.01F))
				.build("blockading"),

				new DamageReductionAffix.Builder(DamageType.MAGIC)
				.with(LootRarity.EPIC, step(0.05F, 5, 0.01F))
				.with(LootRarity.MYTHIC, step(0.05F, 10, 0.01F))
				.with(LootRarity.ANCIENT, step(0.05F, 20, 0.01F))
				.build("runed"),

				new DamageReductionAffix.Builder(DamageType.EXPLOSION)
				.with(LootRarity.EPIC, step(0.15F, 5, 0.01F))
				.with(LootRarity.MYTHIC, step(0.35F, 10, 0.01F))
				.with(LootRarity.ANCIENT, step(0.55F, 20, 0.01F))
				.build("blast_forged"),

				new DamageReductionAffix.Builder(DamageType.FIRE)
				.with(LootRarity.EPIC, step(0.25F, 5, 0.01F))
				.with(LootRarity.MYTHIC, step(0.40F, 10, 0.01F))
				.with(LootRarity.ANCIENT, step(0.60F, 20, 0.01F))
				.build("dwarven"),

				new DamageReductionAffix.Builder(DamageType.FALL)
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.40F, 15, 0.01F))
				.with(LootRarity.MYTHIC, step(0.55F, 25, 0.01F))
				.with(LootRarity.ANCIENT, step(1, 1, 0))
				.build("feathery"),

				new SpectralShotAffix().setRegistryName("spectral"),
				new MagicalArrowAffix().setRegistryName("magical"),
				new FestiveAffix().setRegistryName("festive"),
				new ThunderstruckAffix().setRegistryName("thunderstruck"),
				new RetreatingAffix().setRegistryName("retreating"),
				new TelepathicAffix().setRegistryName("telepathic"),
				new ExecutingAffix().setRegistryName("executing"),
				new CleavingAffix().setRegistryName("cleaving"),
				new OmneticAffix().setRegistryName("omnetic"),
				new RadialAffix().setRegistryName("radial"),
				new EnlightenedAffix().setRegistryName("enlightened"),
				new PsychicAffix().setRegistryName("psychic"),
				new CatalyzingAffix().setRegistryName("catalyzing")
		);

		if(Apotheosis.enablePotion) {
			e.getRegistry().registerAll(
				new PotionAffix.Builder(() -> Apoth.Effects.SUNDERING)
				.with(LootRarity.MYTHIC, step(60, 10, 5), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(100, 20, 5), step(1, 1, 1))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "acidic"),

				new PotionAffix.Builder(() -> Apoth.Effects.SUNDERING)
				.with(LootRarity.RARE, step(30, 15, 5), step(0, 1, 0))
				.with(LootRarity.EPIC, step(40, 15, 5), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(60, 20, 5), step(1, 1, 1))
				.with(LootRarity.ANCIENT, step(100, 20, 5), step(2, 1, 1))
				.types(l -> l == LootCategory.HEAVY_WEAPON)
				.build(AffixType.EFFECT, Target.ATTACK_TARGET, "caustic"),

				new PotionAffix.Builder(() -> Apoth.Effects.KNOWLEDGE)
				.with(LootRarity.RARE, step(30, 15, 5), step(0, 1, 0))
				.with(LootRarity.EPIC, step(40, 15, 5), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(60, 20, 5), step(1, 1, 1))
				.with(LootRarity.ANCIENT, step(100, 20, 5), step(2, 1, 1))
				.types(LootCategory::isLightWeapon)
				.build(AffixType.EFFECT, Target.ATTACK_SELF, "sophisticated"),

				new PotionAffix.Builder(() -> Apoth.Effects.BLEEDING)
				.with(LootRarity.RARE, step(60, 15, 5), step(0, 1, 0))
				.with(LootRarity.EPIC, step(90, 15, 5), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(120, 20, 5), step(1, 1, 1))
				.with(LootRarity.ANCIENT, step(200, 20, 5), step(2, 1, 1))
				.types(l -> l == LootCategory.SHIELD)
				.build(AffixType.EFFECT, Target.BLOCK_ATTACKER, "devilish")
			);
		}
		//Formatter::on
	}

	/**
	 * Level Function that allows for only returning "nice" stepped numbers.
	 * @param min The min value
	 * @param steps The max number of steps
	 * @param step The value per step
	 * @return A level function according to these rules
	 */
	private static StepFunction step(float min, int steps, float step) {
		return AffixHelper.step(min, steps, step);
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

}