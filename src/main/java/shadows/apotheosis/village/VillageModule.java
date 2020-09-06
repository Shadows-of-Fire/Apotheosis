package shadows.apotheosis.village;

import java.io.File;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.potion.Effect;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisClientSetup;
import shadows.apotheosis.Apotheosis.ApotheosisSetup;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.village.fletching.ApothFletchingBlock;
import shadows.apotheosis.village.fletching.FletchingContainer;
import shadows.apotheosis.village.fletching.FletchingRecipe;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowEntity;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowItem;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowEntity;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowItem;
import shadows.apotheosis.village.fletching.effects.BleedingEffect;
import shadows.apotheosis.village.wanderer.WandererReplacements;
import shadows.apotheosis.village.wanderer.WandererSpawnerExt;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.ReflectionHelper;

public class VillageModule {

	public static final IRecipeType<FletchingRecipe> FLETCHING = IRecipeType.register(Apotheosis.MODID + ":fletching");
	public static final IRecipeSerializer<FletchingRecipe> FLETCHING_SERIALIZER = new FletchingRecipe.Serializer();

	public static Configuration config;

	public static boolean enableWandererSpawner = true;
	public static boolean enableNewTrades = true;

	@SubscribeEvent
	public void setup(ApotheosisSetup e) {
		MinecraftForge.EVENT_BUS.addListener(WandererReplacements::replaceWandererArrays);
		MinecraftForge.EVENT_BUS.addGenericListener(World.class, this::starting);
		MinecraftForge.EVENT_BUS.addListener(ApotheosisObjects.OBSIDIAN_ARROW::handleArrowJoin);
		Map<BlockState, PointOfInterestType> types = ReflectionHelper.getPrivateValue(PointOfInterestType.class, null, "field_221073_u");
		types.put(Blocks.FLETCHING_TABLE.getDefaultState(), PointOfInterestType.FLETCHER);
		config = new Configuration(new File(Apotheosis.configDir, "village.cfg"));
		WandererSpawnerExt.defaultChance = config.getInt("Spawn Chance", "Wanderer", 10, 1, 100, "The 1/100 chance the wandering trader has to spawn every attempt.  This chance is increased by it's own value each failure until successful.");
		WandererSpawnerExt.defaultDelay = config.getInt("Spawn Delay", "Wanderer", 24000, 1, Integer.MAX_VALUE, "The number of ticks that must elapse before a wanderer spawn attempt happens.");
		enableWandererSpawner = config.getBoolean("Enable Wanderer Spawner", "Wanderer", true, "If the Apotheosis Wanderer Spawner is enabled, instead of the default.");
		enableNewTrades = config.getBoolean("Enable New Trades", "Wanderer", true, "If new trades are added to the wandering merchant.");
		if (config.hasChanged()) config.save();
	}

	@SubscribeEvent
	public void setup(ApotheosisClientSetup e) {
		VillageModuleClient.init();
	}

	@SubscribeEvent
	public void serializers(Register<IRecipeSerializer<?>> e) {
		e.getRegistry().register(FLETCHING_SERIALIZER.setRegistryName(FletchingRecipe.Serializer.NAME));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		PlaceboUtil.registerOverrideBlock(new ApothFletchingBlock(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().registerAll(new ObsidianArrowItem().setRegistryName("obsidian_arrow"), new BroadheadArrowItem().setRegistryName("broadhead_arrow"));
	}

	@SubscribeEvent
	public void entities(Register<EntityType<?>> e) {
		//Formatter::off
		e.getRegistry().register(EntityType.Builder
				.<ObsidianArrowEntity>create(ObsidianArrowEntity::new, EntityClassification.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(4)
				.setUpdateInterval(20)
				.size(0.5F, 0.5F)
				.setCustomClientFactory((se, w) -> new ObsidianArrowEntity(w))
				.build("ob_arrow")
				.setRegistryName("ob_arrow_entity"));
		e.getRegistry().register(EntityType.Builder
				.<BroadheadArrowEntity>create(BroadheadArrowEntity::new, EntityClassification.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(4)
				.setUpdateInterval(20)
				.size(0.5F, 0.5F)
				.setCustomClientFactory((se, w) -> new BroadheadArrowEntity(w))
				.build("bh_arrow")
				.setRegistryName("bh_arrow_entity"));
		//Formatter::on
	}

	@SubscribeEvent
	public void containers(Register<ContainerType<?>> e) {
		e.getRegistry().register(new ContainerType<>(FletchingContainer::new).setRegistryName("fletching"));
	}

	@SubscribeEvent
	public void effects(Register<Effect> e) {
		e.getRegistry().register(new BleedingEffect().setRegistryName("bleeding"));
	}

	public void starting(AttachCapabilitiesEvent<World> e) {
		if (enableWandererSpawner && e.getObject() instanceof ServerWorld) {
			ServerWorld w = (ServerWorld) e.getObject();
			if (w.dimension.getType() == DimensionType.OVERWORLD) ReflectionHelper.setPrivateValue(ServerWorld.class, w, new WandererSpawnerExt(w), "field_217496_L", "wanderingTraderSpawner");
		}
	}
}
