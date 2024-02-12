package dev.shadowsoffire.apotheosis.village.wanderer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * The wandering merchant sucks. Trades are totally underwhelming and are borderline garbage 99% of the time.
 * The village-module-specific trades are only enabled if the module is enabled, but this data loader is always enabled.
 *
 * @author Shadows
 */
@EventBusSubscriber(modid = Apotheosis.MODID, bus = Bus.MOD)
public class WandererReplacements {

    public static boolean clearNormTrades = false;
    public static boolean clearRareTrades = false;
    public static boolean undergroundTrader = true;

    public static int wandererMaxChance = 101;
    public static int wandererRngCoeff = 2;
    public static int despawnDelay = 24000;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(BasicItemListing.class, BasicItemListingAdapter.INSTANCE).registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE)
        .registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE).create();

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent e) {
        WandererTradesRegistry.INSTANCE.registerToBus();
        MinecraftForge.EVENT_BUS.addListener(WandererReplacements::replaceWandererArrays);
    }

    public static void replaceWandererArrays(WandererTradesEvent e) {
        if (clearNormTrades) e.getGenericTrades().clear();
        if (clearRareTrades) e.getRareTrades().clear();
        e.getGenericTrades().addAll(WandererTradesRegistry.INSTANCE.getNormalTrades());
        e.getRareTrades().addAll(WandererTradesRegistry.INSTANCE.getRareTrades());
    }

    public static void load(Configuration cfg) {
        clearNormTrades = cfg.getBoolean("Clear Generic Trades", "wanderer", clearNormTrades, "If the generic trade list will be cleared before datapack loaded trades are added.\nServer-authoritative.");
        clearRareTrades = cfg.getBoolean("Clear Rare Trades", "wanderer", clearRareTrades, "If the rare trade list will be cleared before datapack loaded trades are added.\nServer-authoritative.");
        undergroundTrader = cfg.getBoolean("Underground Trader", "wanderer", undergroundTrader, "If the Wandering Trader can attempt to spawn underground.\nServer-authoritative.");
        // wandererMaxChance = cfg.getInt(null, null, wandererRngCoeff, wandererMaxChance, despawnDelay, null)
    }
}
