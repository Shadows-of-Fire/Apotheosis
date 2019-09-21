package shadows.apotheosis.spawn;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisSetup;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.ReflectionHelper;

public class SpawnerModule {

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Spawner");
	public static Configuration config;
	public static int spawnerSilkLevel = 1;

	@SubscribeEvent
	public void setup(ApotheosisSetup e) {
		MinecraftForge.EVENT_BUS.register(this);
		config = new Configuration(new File(Apotheosis.configDir, "spawner.cfg"));
		if (Apotheosis.enableSpawner) {
			TileEntityType.MOB_SPAWNER.factory = TileSpawnerExt::new;
			TileEntityType.MOB_SPAWNER.validBlocks = ImmutableSet.of(Blocks.SPAWNER);
		}
		spawnerSilkLevel = config.getInt("Spawner Silk Level", "general", 1, -1, 127, "The level of silk touch needed to harvest a spawner.  Set to -1 to disable, 0 to always drop.  The enchantment module can increase the max level of silk touch.");
		SpawnerModifiers.init();
		if (config.hasChanged()) config.save();
		ReflectionHelper.setPrivateValue(Item.class, Items.SPAWNER, ItemGroup.MISC, "field_77701_a", "group");
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		PlaceboUtil.registerOverrideBlock(new BlockSpawnerExt(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new EnchantmentCapturing().setRegistryName(Apotheosis.MODID, "capturing"));
	}

	@SubscribeEvent
	public void handleCapturing(LivingDropsEvent e) {
		Entity killer = e.getSource().getTrueSource();
		if (killer instanceof LivingEntity) {
			int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.CAPTURING, ((LivingEntity) killer).getHeldItemMainhand());
			if (e.getEntityLiving().world.rand.nextFloat() < level / 250F) {
				LivingEntity killed = e.getEntityLiving();
				ItemStack egg = new ItemStack(SpawnEggItem.EGGS.get(killed.getType()));
				e.getDrops().add(new ItemEntity(killed.world, killed.posX, killed.posY, killed.posZ, egg));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void drops(BlockEvent.HarvestDropsEvent e) {
		if (e.getState().getBlock() == Blocks.SPAWNER && e.getHarvester() != null) {
			if (SpawnerModule.spawnerSilkLevel != -1 && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, e.getHarvester().getHeldItemMainhand()) >= SpawnerModule.spawnerSilkLevel) {
				e.getDrops().clear();
			}
		}
	}

	@SubscribeEvent
	public void handleUseItem(RightClickBlock e) {
		TileEntity te;
		if ((te = e.getWorld().getTileEntity(e.getPos())) instanceof TileSpawnerExt) {
			ItemStack s = e.getItemStack();
			boolean inverse = SpawnerModifiers.inverseItem.test(e.getPlayer().getHeldItem(e.getHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
			for (SpawnerModifier sm : SpawnerModifiers.MODIFIERS)
				if (sm.canModify((TileSpawnerExt) te, s, inverse)) e.setUseBlock(Result.ALLOW);
		}
	}

}
