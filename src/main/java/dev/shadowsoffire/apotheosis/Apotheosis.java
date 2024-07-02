package dev.shadowsoffire.apotheosis;

import java.io.File;
import java.util.function.BooleanSupplier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.shadowsoffire.apotheosis.advancements.AdvancementTriggers;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.RerollResultMessage;
import dev.shadowsoffire.apotheosis.adventure.client.BossSpawnMessage;
import dev.shadowsoffire.apotheosis.adventure.net.RadialStateChangeMessage;
import dev.shadowsoffire.apotheosis.compat.PatchouliCompat;
import dev.shadowsoffire.apotheosis.ench.EnchModule;
import dev.shadowsoffire.apotheosis.ench.table.ClueMessage;
import dev.shadowsoffire.apotheosis.ench.table.StatsMessage;
import dev.shadowsoffire.apotheosis.garden.GardenModule;
import dev.shadowsoffire.apotheosis.potion.PotionModule;
import dev.shadowsoffire.apotheosis.spawn.SpawnerModule;
import dev.shadowsoffire.apotheosis.util.ModuleCondition;
import dev.shadowsoffire.apotheosis.util.ParticleMessage;
import dev.shadowsoffire.apotheosis.village.VillageModule;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.recipe.NBTIngredient;
import dev.shadowsoffire.placebo.recipe.RecipeHelper;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import dev.shadowsoffire.placebo.util.RunnableReloader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.eventbus.api.EventPriority;
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
import net.minecraftforge.registries.RegisterEvent;

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

    public static File configDir;
    public static Configuration config;
    public static boolean enableEnch = true;
    public static boolean enableAdventure = true;
    public static boolean enableSpawner = true;
    public static boolean enablePotion = true;
    public static boolean enableVillage = true;
    public static boolean enableGarden = true;
    public static boolean giveBook = true;

    private static float localAtkStrength = 1;

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

    public Apotheosis() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

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
        MessageHelper.registerMessage(CHANNEL, 0, new ParticleMessage.Provider());
        MessageHelper.registerMessage(CHANNEL, 1, new BossSpawnMessage.Provider());
        MessageHelper.registerMessage(CHANNEL, 2, new ClueMessage.Provider());
        MessageHelper.registerMessage(CHANNEL, 3, new StatsMessage.Provider());
        MessageHelper.registerMessage(CHANNEL, 4, new RadialStateChangeMessage.Provider());
        MessageHelper.registerMessage(CHANNEL, 5, new RerollResultMessage.Provider());
        e.enqueueWork(() -> {
            AdvancementTriggers.init();
            CraftingHelper.register(new ModuleCondition.Serializer());
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
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

    public static ResourceLocation loc(String s) {
        return new ResourceLocation(MODID, s);
    }

    /**
     * Gets the local attack strength of an entity.
     * <p>
     * For players, this is recorded in {@link AttackEntityEvent} and is valid for other damage events.
     * <p>
     * For non-players, this value is always 1.
     */
    public static float getLocalAtkStrength(Entity entity) {
        if (entity instanceof Player) return localAtkStrength;
        return 1;
    }

    public static MutableComponent sysMessageHeader() {
        return Component.translatable("[%s] ", Component.literal("Apoth").withStyle(ChatFormatting.GOLD));
    }

    /**
     * The apotheosis construction event is fired from {@link Apotheosis}'s constructor.
     */
    public static class ApotheosisConstruction extends Event implements IModBusEvent {}

    /**
     * The apotheosis reload event is fired from resource reload.
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

    public static class ModularDeferredHelper extends DeferredHelper {

        protected final BooleanSupplier flag;

        public static ModularDeferredHelper create(BooleanSupplier flag) {
            ModularDeferredHelper helper = new ModularDeferredHelper(flag);
            FMLJavaModLoadingContext.get().getModEventBus().register(helper);
            return helper;
        }

        protected ModularDeferredHelper(BooleanSupplier flag) {
            super(Apotheosis.MODID);
            this.flag = flag;
        }

        @Override
        @SubscribeEvent
        public void register(RegisterEvent e) {
            if (this.flag.getAsBoolean()) super.register(e);
        }

    }

}
