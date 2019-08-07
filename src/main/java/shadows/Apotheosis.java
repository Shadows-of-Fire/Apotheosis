package shadows;

import java.io.File;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import shadows.deadly.DeadlyModule;
import shadows.ench.EnchModule;
import shadows.garden.GardenModule;
import shadows.placebo.config.Configuration;
import shadows.placebo.recipe.RecipeHelper;
import shadows.placebo.util.NetworkUtils;
import shadows.potion.PotionModule;
import shadows.spawn.SpawnerModule;
import shadows.util.NBTIngredient;
import shadows.util.ParticleMessage;

@Mod(Apotheosis.MODID)
public class Apotheosis {

	public static final String MODID = "apotheosis";
	//Formatter::off
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, "channel"))
            .clientAcceptedVersions(s->true)
            .serverAcceptedVersions(s->true)
            .networkProtocolVersion(() -> "1.0.0")
            .simpleChannel();
    //Formatter::on

	public static File configDir;
	public static Configuration config;
	public static boolean enableSpawner = true;
	public static boolean enableGarden = true;
	public static boolean enableDeadly = true;
	public static boolean enableEnch = true;
	public static boolean enablePotion = true;
	public static boolean enchTooltips = true;

	public Apotheosis() {
		configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), MODID);
		config = new Configuration(new File(configDir, MODID + ".cfg"));

		enableEnch = config.getBoolean("Enable Enchantment Module", "general", true, "If the enchantment module is enabled.");
		if (enableEnch) MinecraftForge.EVENT_BUS.register(new EnchModule());

		enableSpawner = config.getBoolean("Enable Spawner Module", "general", true, "If the spawner module is enabled.");
		if (enableSpawner) MinecraftForge.EVENT_BUS.register(new SpawnerModule());

		enableGarden = config.getBoolean("Enable Garden Module", "general", true, "If the garden module is loaded.");
		if (enableGarden) MinecraftForge.EVENT_BUS.register(new GardenModule());

		enableDeadly = config.getBoolean("Enable Deadly Module", "general", true, "If the deadly module is loaded.");
		if (enableDeadly) MinecraftForge.EVENT_BUS.register(new DeadlyModule());

		enablePotion = config.getBoolean("Enable Potion Module", "general", true, "If the potion module is loaded.");
		if (enablePotion) MinecraftForge.EVENT_BUS.register(new PotionModule());

		enchTooltips = config.getBoolean("Enchantment Tooltips", "client", true, "If apotheosis enchantments have tooltips on books.");

		if (config.hasChanged()) config.save();
		FMLJavaModLoadingContext.get().getModEventBus().post(new ApotheosisConstruction());
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void init(FMLCommonSetupEvent e) throws IOException {
		MinecraftForge.EVENT_BUS.post(new ApotheosisSetup(e));
		NetworkUtils.registerMessage(CHANNEL, 0, new ParticleMessage());
		RecipeHelper helper = new RecipeHelper(Apotheosis.MODID);
		MinecraftForge.EVENT_BUS.post(new ApotheosisRecipeEvent(helper));
	}

	public static void registerOverrideBlock(IForgeRegistry<Block> reg, Block b, String modid) {
		reg.register(b);
		ForgeRegistries.ITEMS.register(new BlockItem(b, new Item.Properties().group(b.asItem().getGroup())) {
			@Override
			public String getCreatorModId(ItemStack itemStack) {
				return modid;
			}
		}.setRegistryName(b.getRegistryName()));
	}

	public static Ingredient potionIngredient(Potion type) {
		return new NBTIngredient(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), type));
	}

	public static class ApotheosisConstruction extends Event {
		private ApotheosisConstruction() {
		}
	}

	public static class ApotheosisSetup extends Event {
		public FMLCommonSetupEvent ev;

		private ApotheosisSetup(FMLCommonSetupEvent ev) {
			this.ev = ev;
		}
	}

	public static class ApotheosisRecipeEvent extends Event {
		public RecipeHelper helper;

		private ApotheosisRecipeEvent(RecipeHelper helper) {
			this.helper = helper;
		}
	}

}
