package shadows.apotheosis;

import java.io.File;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.garden.GardenModule;
import shadows.apotheosis.potion.PotionModule;
import shadows.apotheosis.spawn.SpawnerModule;
import shadows.apotheosis.util.EnchantmentIngredient;
import shadows.apotheosis.util.ModuleCondition;
import shadows.apotheosis.util.ParticleMessage;
import shadows.apotheosis.village.VillageModule;
import shadows.placebo.config.Configuration;
import shadows.placebo.recipe.NBTIngredient;
import shadows.placebo.recipe.RecipeHelper;
import shadows.placebo.util.NetworkUtils;

@SuppressWarnings("deprecation")
@Mod(Apotheosis.MODID)
public class Apotheosis {

	public static final String MODID = "apotheosis";
	//Formatter::off
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, MODID))
            .clientAcceptedVersions(s->true)
            .serverAcceptedVersions(s->true)
            .networkProtocolVersion(() -> "1.0.0")
            .simpleChannel();
    //Formatter::on

	public static final RecipeHelper HELPER = new RecipeHelper(Apotheosis.MODID);

	public static File configDir;
	public static Configuration config;
	public static boolean enableSpawner = true;
	public static boolean enableGarden = true;
	public static boolean enableDeadly = true;
	public static boolean enableEnch = true;
	public static boolean enablePotion = true;
	public static boolean enableVillager = true;

	public static float localAtkStrength = 1;

	public Apotheosis() {
		Affix.classload();
		configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), MODID);
		config = new Configuration(new File(configDir, MODID + ".cfg"));

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		enableEnch = config.getBoolean("Enable Enchantment Module", "general", true, "If the enchantment module is enabled.");
		if (enableEnch) bus.register(new EnchModule());

		enableSpawner = config.getBoolean("Enable Spawner Module", "general", true, "If the spawner module is enabled.");
		if (enableSpawner) bus.register(new SpawnerModule());

		enableGarden = config.getBoolean("Enable Garden Module", "general", true, "If the garden module is loaded.");
		if (enableGarden) bus.register(new GardenModule());

		enableDeadly = config.getBoolean("Enable Deadly Module", "general", true, "If the deadly module is loaded.");
		if (enableDeadly) bus.register(new DeadlyModule());

		enablePotion = config.getBoolean("Enable Potion Module", "general", true, "If the potion module is loaded.");
		if (enablePotion) bus.register(new PotionModule());

		enableVillager = config.getBoolean("Enable Village Module", "general", enableVillager, "If the village module is loaded.");
		if (enableVillager) bus.register(new VillageModule());

		if (config.hasChanged()) config.save();
		bus.post(new ApotheosisConstruction());
		bus.addListener(this::init);
		bus.addListener(this::initC);
		MinecraftForge.EVENT_BUS.addListener(this::trackCooldown);

	}

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		NetworkUtils.registerMessage(CHANNEL, 0, new ParticleMessage());
		FMLJavaModLoadingContext.get().getModEventBus().post(new ApotheosisSetup());
		DeferredWorkQueue.runLater(AdvancementTriggers::init);
		CraftingHelper.register(new ModuleCondition.Serializer());
		CraftingHelper.register(new ResourceLocation(MODID, "enchantment"), EnchantmentIngredient.Serializer.INSTANCE);
	}

	@SubscribeEvent
	public void initC(FMLClientSetupEvent e) {
		FMLJavaModLoadingContext.get().getModEventBus().post(new ApotheosisClientSetup());
	}

	public void trackCooldown(AttackEntityEvent e) {
		PlayerEntity p = e.getPlayer();
		localAtkStrength = p.getCooledAttackStrength(0.5F);
	}

	public static Ingredient potionIngredient(Potion type) {
		return new NBTIngredient(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), type));
	}

	public static class ApotheosisConstruction extends Event implements IModBusEvent {
		public ApotheosisConstruction() {
		}
	}

	public static class ApotheosisSetup extends Event implements IModBusEvent {
		public ApotheosisSetup() {
		}
	}

	public static class ApotheosisClientSetup extends Event implements IModBusEvent {
		public ApotheosisClientSetup() {
		}
	}

}
