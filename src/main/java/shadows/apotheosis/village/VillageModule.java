package shadows.apotheosis.village;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.village.fletching.BlockFletchingTable;
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
import shadows.apotheosis.village.fletching.effects.BleedingEffect;
import shadows.apotheosis.village.wanderer.WandererReplacements;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;

public class VillageModule {

	public static final IRecipeType<FletchingRecipe> FLETCHING = IRecipeType.register(Apotheosis.MODID + ":fletching");
	public static final IRecipeSerializer<FletchingRecipe> FLETCHING_SERIALIZER = new FletchingRecipe.Serializer();

	public static Configuration config;

	public static boolean enableNewTrades = true;

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent e) {
		MinecraftForge.EVENT_BUS.addListener(WandererReplacements::replaceWandererArrays);
		MinecraftForge.EVENT_BUS.addListener(ApotheosisObjects.OBSIDIAN_ARROW::handleArrowJoin);
		config = new Configuration(new File(Apotheosis.configDir, "village.cfg"));
		enableNewTrades = config.getBoolean("Enable New Trades", "Wanderer", true, "If new trades are added to the wandering merchant.");
		if (config.hasChanged()) config.save();
	}

	@SubscribeEvent
	public void setup(FMLClientSetupEvent e) {
		e.enqueueWork(VillageModuleClient::init);
	}

	@SubscribeEvent
	public void poi(Register<PointOfInterestType> e) {
		PointOfInterestType f = e.getRegistry().getValue(new ResourceLocation("fletcher"));
		Field blockStates = ObfuscationReflectionHelper.findField(PointOfInterestType.class, "field_221075_w");
		Set<BlockState> states = new HashSet<>();
		states.add(Blocks.FLETCHING_TABLE.getDefaultState());
		try {
			blockStates.set(f, ImmutableSet.copyOf(states));
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}

	@SubscribeEvent
	public void serializers(Register<IRecipeSerializer<?>> e) {
		e.getRegistry().register(FLETCHING_SERIALIZER.setRegistryName(FletchingRecipe.Serializer.NAME));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		PlaceboUtil.registerOverrideBlock(new BlockFletchingTable(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new ObsidianArrowItem().setRegistryName("obsidian_arrow"), 
				new BroadheadArrowItem().setRegistryName("broadhead_arrow"),
				new ExplosiveArrowItem().setRegistryName("explosive_arrow"),
				new MiningArrowItem(() -> Items.IRON_PICKAXE, MiningArrowEntity.Type.IRON).setRegistryName("iron_mining_arrow"),
				new MiningArrowItem(() -> Items.DIAMOND_PICKAXE, MiningArrowEntity.Type.DIAMOND).setRegistryName("diamond_mining_arrow")
		);
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
		e.getRegistry().register(EntityType.Builder
				.<ExplosiveArrowEntity>create(ExplosiveArrowEntity::new, EntityClassification.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(4)
				.setUpdateInterval(20)
				.size(0.5F, 0.5F)
				.setCustomClientFactory((se, w) -> new ExplosiveArrowEntity(w))
				.build("ex_arrow")
				.setRegistryName("ex_arrow_entity"));
		e.getRegistry().register(EntityType.Builder
				.<MiningArrowEntity>create(MiningArrowEntity::new, EntityClassification.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(4)
				.setUpdateInterval(20)
				.size(0.5F, 0.5F)
				.setCustomClientFactory((se, w) -> new MiningArrowEntity(w))
				.build("mn_arrow")
				.setRegistryName("mn_arrow_entity"));
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
}