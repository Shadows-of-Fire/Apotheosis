package shadows.apotheosis.spawn;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
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
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;

public class SpawnerModule {

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Spawner");
	public static int spawnerSilkLevel = 1;
	public static int spawnerSilkDamage = 100;
	public static Set<ResourceLocation> bannedMobs = new HashSet<>();

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent e) {
		BlockEntityType.MOB_SPAWNER.factory = ApothSpawnerTile::new;
		BlockEntityType.MOB_SPAWNER.validBlocks = ImmutableSet.of(Blocks.SPAWNER);
		MinecraftForge.EVENT_BUS.addListener(this::handleCapturing);
		MinecraftForge.EVENT_BUS.addListener(this::handleUseItem);
		MinecraftForge.EVENT_BUS.addListener(this::reload);
		MinecraftForge.EVENT_BUS.addListener(this::handleTooltips);
		MinecraftForge.EVENT_BUS.addListener(this::tickDumbMobs);
		this.reload(null);
		ObfuscationReflectionHelper.setPrivateValue(Item.class, Items.SPAWNER, CreativeModeTab.TAB_MISC, "f_41377_");
		if (ModList.get().isLoaded("theoneprobe")) SpawnerTOPPlugin.register();
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		PlaceboUtil.registerOverride(new ApothSpawnerBlock(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void serializers(Register<RecipeSerializer<?>> e) {
		e.getRegistry().register(SpawnerModifier.SERIALIZER.setRegistryName("spawner_modifier"));
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new CapturingEnchant().setRegistryName(Apotheosis.MODID, "capturing"));
	}

	public void handleCapturing(LivingDropsEvent e) {
		Entity killer = e.getSource().getEntity();
		if (killer instanceof LivingEntity) {
			int level = EnchantmentHelper.getItemEnchantmentLevel(Apoth.Enchantments.CAPTURING, ((LivingEntity) killer).getMainHandItem());
			LivingEntity killed = e.getEntityLiving();
			if (bannedMobs.contains(killed.getType().getRegistryName())) return;
			if (killed.level.random.nextFloat() < level / 250F) {
				ItemStack egg = new ItemStack(SpawnEggItem.BY_ID.get(killed.getType()));
				e.getDrops().add(new ItemEntity(killed.level, killed.getX(), killed.getY(), killed.getZ(), egg));
			}
		}
	}

	public void handleUseItem(RightClickBlock e) {
		if (e.getWorld().getBlockEntity(e.getPos()) instanceof ApothSpawnerTile) {
			ItemStack s = e.getItemStack();
			if (s.getItem() instanceof SpawnEggItem egg) {
				EntityType<?> type = egg.getType(s.getTag());
				if (bannedMobs.contains(type.getRegistryName())) e.setCanceled(true);
			}
		}
	}

	public void handleTooltips(ItemTooltipEvent e) {
		ItemStack s = e.getItemStack();
		if (s.getItem() instanceof SpawnEggItem egg) {
			EntityType<?> type = egg.getType(s.getTag());
			if (bannedMobs.contains(type.getRegistryName())) e.getToolTip().add(new TranslatableComponent("misc.apotheosis.banned").withStyle(ChatFormatting.GRAY));
		}
	}

	public void tickDumbMobs(LivingUpdateEvent e) {
		if (e.getEntityLiving() instanceof Mob mob) {
			if (!mob.level.isClientSide && mob.isNoAi() && mob.getPersistentData().getBoolean("apotheosis:movable")) {
				mob.setNoAi(false);
				mob.travel(new Vec3(mob.xxa, mob.zza, mob.yya));
				mob.setNoAi(true);
			}
		}
	}

	public void reload(ApotheosisReloadEvent e) {
		Configuration config = new Configuration(new File(Apotheosis.configDir, "spawner.cfg"));
		spawnerSilkLevel = config.getInt("Spawner Silk Level", "general", 1, -1, 127, "The level of silk touch needed to harvest a spawner.  Set to -1 to disable, 0 to always drop.  The enchantment module can increase the max level of silk touch.");
		spawnerSilkDamage = config.getInt("Spawner Silk Damage", "general", 100, 0, 100000, "The durability damage dealt to an item that silk touches a spawner.");
		bannedMobs.clear();
		String[] bans = config.getStringList("Banned Mobs", "spawn_eggs", new String[0], "A list of entity registry names that cannot be applied to spawners via egg.");
		for (String s : bans)
			bannedMobs.add(new ResourceLocation(s));
		if (e == null && config.hasChanged()) config.save();
	}

}