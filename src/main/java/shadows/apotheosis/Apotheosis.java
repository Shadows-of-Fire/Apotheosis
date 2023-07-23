package shadows.apotheosis;

import java.io.File;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.client.BossSpawnMessage;
import shadows.apotheosis.compat.PatchouliCompat;
import shadows.apotheosis.core.attributeslib.AttributesLib;
import shadows.apotheosis.core.mobfx.MobFxLib;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.table.ClueMessage;
import shadows.apotheosis.garden.GardenModule;
import shadows.apotheosis.potion.PotionModule;
import shadows.apotheosis.spawn.SpawnerModule;
import shadows.apotheosis.util.ModuleCondition;
import shadows.apotheosis.util.ParticleMessage;
import shadows.apotheosis.util.RarityIngredient;
import shadows.apotheosis.village.VillageModule;
import shadows.placebo.config.Configuration;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.recipe.NBTIngredient;
import shadows.placebo.recipe.RecipeHelper;
import shadows.placebo.util.RunnableReloader;

@Mod(Apotheosis.MODID)
public class Apotheosis {

    public static final String MODID = "apotheosis";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(MODID, MODID))
        .clientAcceptedVersions(s -> true)
        .serverAcceptedVersions(s -> true)
        .networkProtocolVersion(() -> "1.0.0")
        .simpleChannel();

    public static final RecipeHelper HELPER = new RecipeHelper(Apotheosis.MODID);

    public static final CreativeModeTab APOTH_GROUP = new CreativeModeTab(MODID){

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.ENCHANTING_TABLE);
        }
    };

    public static File configDir;
    public static Configuration config;
    public static boolean enableEnch = true;
    public static boolean enableAdventure = true;
    public static boolean enableSpawner = true;
    public static boolean enablePotion = true;
    public static boolean enableVillage = true;
    public static boolean enableGarden = true;
    public static boolean giveBook = true;

    public static float localAtkStrength = 1;

    static {
        configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), MODID);
        config = new Configuration(new File(configDir, MODID + ".cfg"));
        enableEnch = config.getBoolean("Enable Enchantment Module", "general", true, "If the enchantment module is enabled.");
        enableAdventure = config.getBoolean("Enable Adventure Module", "general", true, "If the adventure module is loaded.");
        enableSpawner = config.getBoolean("Enable Spawner Module", "general", true, "If the spawner module is enabled.");
        enablePotion = config.getBoolean("Enable Potion Module", "general", true, "If the potion module is loaded.");
        enableVillage = config.getBoolean("Enable Village Module", "general", true, "If the village module is loaded.");
        enableGarden = config.getBoolean("Enable Garden Module", "general", true, "If the garden module is loaded.");
        giveBook = config.getBoolean("Give Book on First Join", "general", true, "If the Chronicle of Shadows is given to new players.");
        config.setTitle("Apotheosis Module Control");
        config.setComment("This file allows individual modules of Apotheosis to be enabled or disabled.\nChanges will have no effect until the next game restart.\nThis file must match on client and server.");
        if (config.hasChanged()) config.save();
    }

    public static final DamageSource CORRUPTED = new DamageSource("apoth_corrupted").bypassArmor().bypassMagic();

    public Apotheosis() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        // Library modules - Mandatory
        bus.register(new AttributesLib());
        bus.register(new MobFxLib());

        // Real modules
        if (enableEnch) bus.register(new EnchModule());
        if (enableSpawner) bus.register(new SpawnerModule());
        if (enableGarden) bus.register(new GardenModule());
        if (enableAdventure) bus.register(new AdventureModule());
        if (enablePotion) bus.register(new PotionModule());
        if (enableVillage) bus.register(new VillageModule());

        if (config.hasChanged()) config.save();
        bus.post(new ApotheosisConstruction());
        bus.addListener(this::init);
        MinecraftForge.EVENT_BUS.addListener(this::reloads);
        MinecraftForge.EVENT_BUS.addListener(this::trackCooldown);
        MinecraftForge.EVENT_BUS.addListener(this::cmds);
        if (ModList.get().isLoaded("patchouli")) PatchouliCompat.register();
        Apoth.RecipeTypes.FLETCHING.getClass(); // Static init wew
    }

    @SubscribeEvent
    public void init(FMLCommonSetupEvent e) {
        MessageHelper.registerMessage(CHANNEL, 0, new ParticleMessage());
        MessageHelper.registerMessage(CHANNEL, 1, new BossSpawnMessage(null, 0));
        MessageHelper.registerMessage(CHANNEL, 2, new ClueMessage(0, null, false));
        e.enqueueWork(() -> {
            AdvancementTriggers.init();
            CraftingHelper.register(new ModuleCondition.Serializer());
            CraftingHelper.register(new ResourceLocation(MODID, "rarity"), RarityIngredient.Serializer.INSTANCE);
        });
    }

    @SubscribeEvent
    public void reloads(AddReloadListenerEvent e) {
        e.addListener(RunnableReloader.of(() -> MinecraftForge.EVENT_BUS.post(new ApotheosisReloadEvent())));
    }

    @SubscribeEvent
    public void trackCooldown(AttackEntityEvent e) {
        Player p = e.getEntity();
        localAtkStrength = p.getAttackStrengthScale(0.5F);
    }

    @SubscribeEvent
    public void cmds(RegisterCommandsEvent e) {
        var builder = Commands.literal("apoth");
        MinecraftForge.EVENT_BUS.post(new ApotheosisCommandEvent(builder));
        e.getDispatcher().register(builder);
    }

    public static Ingredient potionIngredient(Potion type) {
        return new NBTIngredient(PotionUtils.setPotion(new ItemStack(Items.POTION), type));
    }

    /**
     * The apotheosis construction event is fired from {@link Apotheosis}'s constructor.
     */
    public static class ApotheosisConstruction extends Event implements IModBusEvent {}

    /**
     * The apotheosis reload event is fired from resource reload.
     * It may be fired off the main thread.
     */
    public static class ApotheosisReloadEvent extends Event {}

    /**
     * The apotheosis command event is fired when commands are to be registered.
     * Register subcommands at this time.
     */
    public static class ApotheosisCommandEvent extends Event {

        private final LiteralArgumentBuilder<CommandSourceStack> root;

        public ApotheosisCommandEvent(LiteralArgumentBuilder<CommandSourceStack> root) {
            this.root = root;
        }

        public LiteralArgumentBuilder<CommandSourceStack> getRoot() {
            return this.root;
        }
    }

    public static ResourceLocation loc(String s) {
        return new ResourceLocation(MODID, s);
    }

}
