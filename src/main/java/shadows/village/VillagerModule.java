package shadows.village;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisSetup;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.ReflectionHelper;
import shadows.village.fletching.BlockFletchingTable;
import shadows.village.fletching.FletchingContainer;
import shadows.village.fletching.FletchingRecipe;

public class VillagerModule {

	public static final IRecipeType<FletchingRecipe> FLETCHING = IRecipeType.register(Apotheosis.MODID + ":fletching");
	public static final IRecipeSerializer<FletchingRecipe> FLETCHING_SERIALIZER = new FletchingRecipe.Serializer();

	@SubscribeEvent
	public void setup(ApotheosisSetup e) {
		MinecraftForge.EVENT_BUS.addListener(WandererReplacements::replaceWandererArrays);
		MinecraftForge.EVENT_BUS.addGenericListener(World.class, this::starting);
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
	public void containers(Register<ContainerType<?>> e) {
		e.getRegistry().register(new ContainerType<>(FletchingContainer::new).setRegistryName("fletching"));
	}

	public void starting(AttachCapabilitiesEvent<World> e) {
		if (e.getObject() instanceof ServerWorld) {
			ServerWorld w = (ServerWorld) e.getObject();
			if (w.dimension.getType() == DimensionType.OVERWORLD) ReflectionHelper.setPrivateValue(ServerWorld.class, w, new WandererSpawnerExt(w), "field_217496_L", "wanderingTraderSpawner");
		}
	}
}
