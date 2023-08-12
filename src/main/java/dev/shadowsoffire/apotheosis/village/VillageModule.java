package dev.shadowsoffire.apotheosis.village;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.village.fletching.ApothFletchingBlock;
import dev.shadowsoffire.apotheosis.village.fletching.FletchingContainer;
import dev.shadowsoffire.apotheosis.village.fletching.FletchingRecipe;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.BroadheadArrowEntity;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.BroadheadArrowItem;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.ExplosiveArrowEntity;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.ExplosiveArrowItem;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.IApothArrowItem;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.MiningArrowEntity;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.MiningArrowItem;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.ObsidianArrowEntity;
import dev.shadowsoffire.apotheosis.village.fletching.arrows.ObsidianArrowItem;
import dev.shadowsoffire.apotheosis.village.wanderer.WandererReplacements;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.registry.RegistryEvent.Register;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import dev.shadowsoffire.placebo.util.PlaceboUtil;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class VillageModule {

    public static final RecipeSerializer<FletchingRecipe> FLETCHING_SERIALIZER = new FletchingRecipe.Serializer();
    public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Village");

    public static Configuration config;

    public static ExplosionInteraction expArrowMode = ExplosionInteraction.BLOCK;

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        // Map<BlockState, PoiType> types = ObfuscationReflectionHelper.getPrivateValue(PoiType.class, null, "f_27323_");
        // types.put(Blocks.FLETCHING_TABLE.defaultBlockState(), PoiTypes.FLETCHER); Should no longer be neccessary due to state cannibalization
        config = new Configuration(new File(Apotheosis.configDir, "village.cfg"));
        config.setTitle("Apotheosis Village Module Configuration");
        WandererReplacements.load(config);

        boolean blockDmg = config.getBoolean("Explosive Arrow Block Damage", "arrows", true, "If explosive arrows can break blocks.\nServer-authoritative.");
        expArrowMode = blockDmg ? ExplosionInteraction.BLOCK : ExplosionInteraction.NONE;
        if (config.hasChanged()) config.save();

        e.enqueueWork(() -> {
            for (Item i : ForgeRegistries.ITEMS) {
                if (i instanceof IApothArrowItem) {
                    DispenserBlock.registerBehavior(i, new AbstractProjectileDispenseBehavior(){
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
        e.getRegistry().register(FLETCHING_SERIALIZER, FletchingRecipe.Serializer.NAME);
    }

    @SubscribeEvent
    public void blocks(Register<Block> e) {
        PlaceboUtil.registerOverride(Blocks.FLETCHING_TABLE, new ApothFletchingBlock(), Apotheosis.MODID);
    }

    @SubscribeEvent
    public void items(Register<Item> e) {

        e.getRegistry().registerAll(
            new ObsidianArrowItem(new Item.Properties()), "obsidian_arrow",
            new BroadheadArrowItem(new Item.Properties()), "broadhead_arrow",
            new ExplosiveArrowItem(new Item.Properties()), "explosive_arrow",
            new MiningArrowItem(() -> Items.IRON_PICKAXE, MiningArrowEntity.Type.IRON), "iron_mining_arrow",
            new MiningArrowItem(() -> Items.DIAMOND_PICKAXE, MiningArrowEntity.Type.DIAMOND), "diamond_mining_arrow");

        TabFillingRegistry.register(CreativeModeTabs.COMBAT, Apoth.Items.OBSIDIAN_ARROW, Apoth.Items.BROADHEAD_ARROW, Apoth.Items.EXPLOSIVE_ARROW, Apoth.Items.IRON_MINING_ARROW, Apoth.Items.DIAMOND_MINING_ARROW);
    }

    @SubscribeEvent
    public void entities(Register<EntityType<?>> e) {

        e.getRegistry().register(EntityType.Builder
            .<ObsidianArrowEntity>of(ObsidianArrowEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(4)
            .setUpdateInterval(20)
            .sized(0.5F, 0.5F)
            .setCustomClientFactory((se, w) -> new ObsidianArrowEntity(w))
            .build("obsidian_arrow"), "obsidian_arrow");
        e.getRegistry().register(EntityType.Builder
            .<BroadheadArrowEntity>of(BroadheadArrowEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(4)
            .setUpdateInterval(20)
            .sized(0.5F, 0.5F)
            .setCustomClientFactory((se, w) -> new BroadheadArrowEntity(w))
            .build("broadhead_arrow"), "broadhead_arrow");
        e.getRegistry().register(EntityType.Builder
            .<ExplosiveArrowEntity>of(ExplosiveArrowEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(4)
            .setUpdateInterval(20)
            .sized(0.5F, 0.5F)
            .setCustomClientFactory((se, w) -> new ExplosiveArrowEntity(w))
            .build("explosive_arrow"), "explosive_arrow");
        e.getRegistry().register(EntityType.Builder
            .<MiningArrowEntity>of(MiningArrowEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(4)
            .setUpdateInterval(20)
            .sized(0.5F, 0.5F)
            .setCustomClientFactory((se, w) -> new MiningArrowEntity(w))
            .build("mining_arrow"), "mining_arrow");

    }

    @SubscribeEvent
    public void containers(Register<MenuType<?>> e) {
        e.getRegistry().register(MenuUtil.type(FletchingContainer::new), "fletching");
    }
}
