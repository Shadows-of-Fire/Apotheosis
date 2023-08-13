package shadows.apotheosis.spawn;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;

import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.spawn.compat.SpawnerTOPPlugin;
import shadows.apotheosis.spawn.enchantment.CapturingEnchant;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.apotheosis.spawn.spawner.ApothSpawnerBlock;
import shadows.apotheosis.spawn.spawner.ApothSpawnerItem;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.RegistryEvent.Register;

public class SpawnerModule {

    public static final Logger LOG = LogManager.getLogger("Apotheosis : Spawner");
    public static int spawnerSilkLevel = 1;
    public static int spawnerSilkDamage = 100;
    public static Set<ResourceLocation> bannedMobs = new HashSet<>();

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        BlockEntityType.MOB_SPAWNER.factory = ApothSpawnerTile::new;
        BlockEntityType.MOB_SPAWNER.validBlocks = ImmutableSet.of(Blocks.SPAWNER);
        MinecraftForge.EVENT_BUS.addListener(this::dropsEvent);
        MinecraftForge.EVENT_BUS.addListener(this::handleUseItem);
        MinecraftForge.EVENT_BUS.addListener(this::reload);
        MinecraftForge.EVENT_BUS.addListener(this::handleTooltips);
        MinecraftForge.EVENT_BUS.addListener(this::tickDumbMobs);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::dumbMobsCantTeleport);
        this.reload(null);
        ObfuscationReflectionHelper.setPrivateValue(Item.class, Items.SPAWNER, CreativeModeTab.TAB_MISC, "f_41377_");
        if (ModList.get().isLoaded("theoneprobe")) SpawnerTOPPlugin.register();
    }

    @SubscribeEvent
    public void blocks(Register<Block> e) {
        ApothSpawnerBlock spawner = new ApothSpawnerBlock();
        PlaceboUtil.overrideStates(Blocks.SPAWNER, spawner);
        e.getRegistry().register(spawner, new ResourceLocation("minecraft", "spawner"));
    }

    @SubscribeEvent
    public void items(Register<Item> e) {
        e.getRegistry().register(new ApothSpawnerItem(), new ResourceLocation("minecraft", "spawner"));
    }

    @SubscribeEvent
    public void serializers(Register<RecipeSerializer<?>> e) {
        e.getRegistry().register(SpawnerModifier.SERIALIZER, "spawner_modifier");
    }

    @SubscribeEvent
    public void enchants(Register<Enchantment> e) {
        e.getRegistry().register(new CapturingEnchant(), "capturing");
    }

    public void dropsEvent(LivingDropsEvent e) {
        Apoth.Enchantments.CAPTURING.get().handleCapturing(e);
    }

    public void handleUseItem(RightClickBlock e) {
        if (e.getLevel().getBlockEntity(e.getPos()) instanceof ApothSpawnerTile) {
            ItemStack s = e.getItemStack();
            if (s.getItem() instanceof SpawnEggItem egg) {
                EntityType<?> type = egg.getType(s.getTag());
                if (bannedMobs.contains(EntityType.getKey(type))) e.setCanceled(true);
            }
        }
    }

    public void handleTooltips(ItemTooltipEvent e) {
        ItemStack s = e.getItemStack();
        if (s.getItem() instanceof SpawnEggItem egg) {
            EntityType<?> type = egg.getType(s.getTag());
            if (bannedMobs.contains(EntityType.getKey(type))) e.getToolTip().add(Component.translatable("misc.apotheosis.banned").withStyle(ChatFormatting.GRAY));
        }
    }

    public void tickDumbMobs(LivingTickEvent e) {
        if (e.getEntity() instanceof Mob mob) {
            if (!mob.level.isClientSide && mob.isNoAi() && mob.getPersistentData().getBoolean("apotheosis:movable")) {
                mob.setNoAi(false);
                mob.travel(new Vec3(mob.xxa, mob.zza, mob.yya));
                mob.setNoAi(true);
            }
        }
    }

    public void dumbMobsCantTeleport(EntityTeleportEvent e) {
        if (e.getEntity().getPersistentData().getBoolean("apotheosis:movable")) {
            e.setCanceled(true);
        }
    }

    public void reload(ApotheosisReloadEvent e) {
        Configuration config = new Configuration(new File(Apotheosis.configDir, "spawner.cfg"));
        config.setTitle("Apotheosis Spawner Module Configuration");
        spawnerSilkLevel = config.getInt("Spawner Silk Level", "general", 1, -1, 127,
            "The level of silk touch needed to harvest a spawner.  Set to -1 to disable, 0 to always drop.  The enchantment module can increase the max level of silk touch.\nFunctionally server-authoritative, but should match on client for information.");
        spawnerSilkDamage = config.getInt("Spawner Silk Damage", "general", 100, 0, 100000, "The durability damage dealt to an item that silk touches a spawner.\nServer-authoritative.");
        bannedMobs.clear();
        String[] bans = config.getStringList("Banned Mobs", "spawn_eggs", new String[0], "A list of entity registry names that cannot be applied to spawners via egg.\nShould match between client and server.");
        for (String s : bans)
            try {
                bannedMobs.add(new ResourceLocation(s));
            }
            catch (ResourceLocationException ex) {
                SpawnerModule.LOG.error("Invalid entry {} detected in the spawner banned mobs list.", s);
                ex.printStackTrace();
            }
        if (e == null && config.hasChanged()) config.save();
    }

}
