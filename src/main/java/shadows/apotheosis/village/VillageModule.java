package shadows.apotheosis.village;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.village.fletching.ApothFletchingBlock;
import shadows.apotheosis.village.fletching.FletchingContainer;
import shadows.apotheosis.village.fletching.FletchingRecipe;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowEntity;
import shadows.apotheosis.village.fletching.arrows.BroadheadArrowItem;
import shadows.apotheosis.village.fletching.arrows.ExplosiveArrowEntity;
import shadows.apotheosis.village.fletching.arrows.ExplosiveArrowItem;
import shadows.apotheosis.village.fletching.arrows.IApothArrowItem;
import shadows.apotheosis.village.fletching.arrows.MiningArrowEntity;
import shadows.apotheosis.village.fletching.arrows.MiningArrowItem;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowEntity;
import shadows.apotheosis.village.fletching.arrows.ObsidianArrowItem;
import shadows.apotheosis.village.fletching.effects.BleedingEffect;
import shadows.apotheosis.village.wanderer.WandererReplacements;
import shadows.apotheosis.village.wanderer.WandererTradeManager;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;

public class VillageModule {

	public static final RecipeType<FletchingRecipe> FLETCHING = PlaceboUtil.makeRecipeType(Apotheosis.MODID + ":fletching");
	public static final RecipeSerializer<FletchingRecipe> FLETCHING_SERIALIZER = new FletchingRecipe.Serializer();
	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Village");

	public static Configuration config;

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent e) {
		MinecraftForge.EVENT_BUS.addListener(WandererReplacements::replaceWandererArrays);
		MinecraftForge.EVENT_BUS.addListener(this::reloads);
		Map<BlockState, PoiType> types = ObfuscationReflectionHelper.getPrivateValue(PoiType.class, null, "f_27323_");
		types.put(Blocks.FLETCHING_TABLE.defaultBlockState(), PoiType.FLETCHER);
		config = new Configuration(new File(Apotheosis.configDir, "village.cfg"));
		WandererReplacements.load(config);
		if (config.hasChanged()) config.save();

		e.enqueueWork(() -> {
			for (Item i : ForgeRegistries.ITEMS) {
				if (i instanceof IApothArrowItem) {
					DispenserBlock.registerBehavior(i, new AbstractProjectileDispenseBehavior() {
						@Override
						protected Projectile getProjectile(Level world, Position pos, ItemStack stack) {
							return ((IApothArrowItem) i).fromDispenser(world, pos.x(), pos.y(), pos.z());
						}
					});
				}
			}
		});
	}

	@SubscribeEvent
	public void setup(FMLClientSetupEvent e) {
		e.enqueueWork(VillageModuleClient::init);
	}

	@SubscribeEvent
	public void serializers(Register<RecipeSerializer<?>> e) {
		e.getRegistry().register(FLETCHING_SERIALIZER.setRegistryName(FletchingRecipe.Serializer.NAME));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		PlaceboUtil.registerOverride(new ApothFletchingBlock(), Apotheosis.MODID);
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
				.<ObsidianArrowEntity>of(ObsidianArrowEntity::new, MobCategory.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(4)
				.setUpdateInterval(20)
				.sized(0.5F, 0.5F)
				.setCustomClientFactory((se, w) -> new ObsidianArrowEntity(w))
				.build("obsidian_arrow")
				.setRegistryName("obsidian_arrow"));
		e.getRegistry().register(EntityType.Builder
				.<BroadheadArrowEntity>of(BroadheadArrowEntity::new, MobCategory.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(4)
				.setUpdateInterval(20)
				.sized(0.5F, 0.5F)
				.setCustomClientFactory((se, w) -> new BroadheadArrowEntity(w))
				.build("broadhead_arrow")
				.setRegistryName("broadhead_arrow"));
		e.getRegistry().register(EntityType.Builder
				.<ExplosiveArrowEntity>of(ExplosiveArrowEntity::new, MobCategory.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(4)
				.setUpdateInterval(20)
				.sized(0.5F, 0.5F)
				.setCustomClientFactory((se, w) -> new ExplosiveArrowEntity(w))
				.build("explosive_arrow")
				.setRegistryName("explosive_arrow"));
		e.getRegistry().register(EntityType.Builder
				.<MiningArrowEntity>of(MiningArrowEntity::new, MobCategory.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(4)
				.setUpdateInterval(20)
				.sized(0.5F, 0.5F)
				.setCustomClientFactory((se, w) -> new MiningArrowEntity(w))
				.build("mining_arrow")
				.setRegistryName("mining_arrow"));
		//Formatter::on
	}

	@SubscribeEvent
	public void containers(Register<MenuType<?>> e) {
		e.getRegistry().register(new MenuType<>(FletchingContainer::new).setRegistryName("fletching"));
	}

	@SubscribeEvent
	public void effects(Register<MobEffect> e) {
		e.getRegistry().register(new BleedingEffect().setRegistryName("bleeding"));
	}

	public void reloads(AddReloadListenerEvent e) {
		e.addListener(WandererTradeManager.INSTANCE);
	}
}