package dev.shadowsoffire.apotheosis.garden;

import java.io.File;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisReloadEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.util.PlaceboUtil;
import dev.shadowsoffire.placebo.util.RegistryEvent.Register;

public class GardenModule {

    public static int maxCactusHeight = 5;
    public static int maxReedHeight = 255;
    public static int maxBambooHeight = 32;

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        this.reload(null);
        Apotheosis.HELPER.registerProvider(factory -> {
            factory.addShapeless(Apoth.Items.ENDER_LEAD, Items.ENDER_PEARL, Items.LEAD, Items.GOLD_INGOT);
        });
        MinecraftForge.EVENT_BUS.addListener(this::reload);
    }

    @SubscribeEvent
    public void blocks(Register<Block> e) {
        PlaceboUtil.registerOverride(Blocks.CACTUS, new ApothCactusBlock(), Apotheosis.MODID);
        PlaceboUtil.registerOverride(Blocks.SUGAR_CANE, new ApothSugarcaneBlock(), Apotheosis.MODID);
        PlaceboUtil.registerOverride(Blocks.BAMBOO, new ApothBambooBlock(), Apotheosis.MODID);
    }

    @SubscribeEvent
    public void items(Register<Item> e) {
        e.getRegistry().register(new EnderLeadItem(), "ender_lead");
        ComposterBlock.COMPOSTABLES.put(Blocks.CACTUS.asItem(), 0.5F);
        ComposterBlock.COMPOSTABLES.put(Blocks.SUGAR_CANE.asItem(), 0.5F);
    }

    public void reload(ApotheosisReloadEvent e) {
        Configuration c = new Configuration(new File(Apotheosis.configDir, "garden.cfg"));
        c.setTitle("Apotheosis Garden Module Configuration");
        maxCactusHeight = c.getInt("Cactus Height", "general", maxCactusHeight, 1, 512, "The max height a stack of cacti may grow to.  Vanilla is 3.  Values greater than 32 are uncapped growth.\nServer-authoritative.");
        maxReedHeight = c.getInt("Reed Height", "general", maxReedHeight, 1, 512, "The max height a stack of reeds may grow to.  Vanilla is 3.  Values greater than 32 are uncapped growth.\nServer-authoritative.");
        maxBambooHeight = c.getInt("Bamboo Height", "general", maxBambooHeight, 1, 64, "The max height a stack of bamboo may grow to.  Vanilla is 16.\nServer-authoritative.");
        if (e == null && c.hasChanged()) c.save();
    }
}
