package shadows.spawn;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import shadows.Apotheosis;

@Mod(modid = SpawnerManagement.MODID, name = SpawnerManagement.MODNAME, version = SpawnerManagement.VERSION, dependencies = "required-after:apotheosis")
public class SpawnerManagement {

	public static final String MODID = "spawnermanagement";
	public static final String MODNAME = "Spawner Management";
	public static final String VERSION = "1.0.0";

	public static final Logger LOG = LogManager.getLogger(MODID);

	@ObjectHolder("spawnermanagement:capturing")
	public static final EnchantmentCapturing CAPTURING = null;

	static Configuration config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		config = new Configuration(new File(Apotheosis.configDir, MODID + ".cfg"));
		if (Apotheosis.enableSpawner) {
			TileEntity.register("mob_spawner", TileSpawnerExt.class);
			MinecraftForge.EVENT_BUS.register(this);
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		if (Apotheosis.enableSpawner) SpawnerModifiers.init(config);
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Block b;
		e.getRegistry().register(b = new BlockSpawnerExt());
		ForgeRegistries.ITEMS.register(new ItemBlock(b) {
			public String getCreatorModId(ItemStack stack) {
				return MODID;
			}
		}.setRegistryName(b.getRegistryName()));
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new EnchantmentCapturing().setRegistryName(MODID, "capturing"));
	}

	@SubscribeEvent
	public void handleCapturing(LivingDropsEvent e) {
		Entity killer = e.getSource().getTrueSource();
		if (killer instanceof EntityLivingBase) {
			int level = EnchantmentHelper.getEnchantmentLevel(CAPTURING, ((EntityLivingBase) killer).getHeldItemMainhand());
			if (e.getEntityLiving().world.rand.nextFloat() < level / 250F) {
				ItemStack egg = new ItemStack(Items.SPAWN_EGG);
				EntityLivingBase killed = e.getEntityLiving();
				ItemMonsterPlacer.applyEntityIdToItemStack(egg, EntityList.getKey(killed));
				e.getDrops().add(new EntityItem(killed.world, killed.posX, killed.posY, killed.posZ, egg));
			}
		}
	}

	@SubscribeEvent
	public void handleUseItem(RightClickBlock e) {
		if (e.getWorld().getBlockState(e.getPos()).getBlock() == Blocks.MOB_SPAWNER) {
			ItemStack s = e.getItemStack();
			for (SpawnerModifier sm : SpawnerModifiers.MODIFIERS)
				if (sm.matches(s)) {
					e.setUseBlock(Result.ALLOW);
				}
		}
	}

}
