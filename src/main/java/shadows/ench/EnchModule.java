package shadows.ench;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisRecipeEvent;
import shadows.placebo.util.PlaceboUtil;
import shadows.util.NBTIngredient;

public class EnchModule {

	@ObjectHolder("apotheosis:hellshelf")
	public static final BlockHellBookshelf HELLSHELF = null;

	@ObjectHolder("minecraft:web")
	public static final Item COBWEB = null;

	@ObjectHolder("apotheosis:hell_infusion")
	public static final EnchantmentHellInfused HELL_INFUSION = null;

	@ObjectHolder("apotheosis:mounted_strike")
	public static final EnchantmentMounted MOUNTED_STRIKE = null;

	@ObjectHolder("apotheosis:depth_miner")
	public static final EnchantmentDepths DEPTH_MINER = null;

	@ObjectHolder("apotheosis:stable_footing")
	public static final EnchantmentStableFooting STABLE_FOOTING = null;

	@ObjectHolder("apotheosis:scavenger")
	public static final EnchantmentScavenger SCAVENGER = null;

	public static float localAtkStrength = 1;

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		Configuration config = new Configuration(new File(Apotheosis.configDir, "enchantments.cfg"));
		setEnch(ToolMaterial.GOLD, 40);
		setEnch(ArmorMaterial.GOLD, 40);
		for (ArmorMaterial a : ArmorMaterial.values())
			setEnch(a, config.getInt(a.name(), "Enchantability - Armor", a.getEnchantability(), 0, Integer.MAX_VALUE, "The enchantability of this armor material."));
		for (ToolMaterial a : ToolMaterial.values())
			setEnch(a, config.getInt(a.name(), "Enchantability - Tools", a.getEnchantability(), 0, Integer.MAX_VALUE, "The enchantability of this tool material."));
		if (config.hasChanged()) config.save();
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Block b;
		e.getRegistry().register(b = new BlockHellBookshelf(new ResourceLocation(Apotheosis.MODID, "hellshelf")));
		ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		ForgeRegistries.ITEMS.register(new ItemShearsExt().setRegistryName(Items.SHEARS.getRegistryName()).setTranslationKey("shears"));
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new EnchantmentHellInfused().setRegistryName(Apotheosis.MODID, "hell_infusion"),
				new EnchantmentMounted().setRegistryName(Apotheosis.MODID, "mounted_strike"),
				new EnchantmentDepths().setRegistryName(Apotheosis.MODID, "depth_miner"),
				new EnchantmentStableFooting().setRegistryName(Apotheosis.MODID, "stable_footing"),
				new EnchantmentScavenger().setRegistryName(Apotheosis.MODID, "scavenger"));
		//Formatter::on
	}

	@SubscribeEvent
	public void models(ModelRegistryEvent e) {
		PlaceboUtil.sMRL(HELLSHELF, 0, "normal");
	}

	@SubscribeEvent
	public void recipes(ApotheosisRecipeEvent e) {
		Ingredient pot = new NBTIngredient(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.REGENERATION));
		e.helper.addShaped(HELLSHELF, 3, 3, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Items.BLAZE_ROD, Blocks.BOOKSHELF, pot, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK, Blocks.NETHER_BRICK);
	}

	@SubscribeEvent
	public void removeEnch(AnvilUpdateEvent e) {
		if (!EnchantmentHelper.getEnchantments(e.getLeft()).isEmpty() && e.getRight().getItem() == COBWEB) {
			ItemStack stack = e.getLeft().copy();
			EnchantmentHelper.setEnchantments(Collections.emptyMap(), stack);
			e.setCost(1);
			e.setMaterialCost(1);
			e.setOutput(stack);
		}
	}

	@SubscribeEvent
	public void trackCooldown(AttackEntityEvent e) {
		localAtkStrength = e.getEntityPlayer().getCooledAttackStrength(0.5F);
	}

	Method dropLoot;

	@SubscribeEvent
	public void scavenger(LivingDropsEvent e) throws Exception {
		Entity attacker = e.getSource().getTrueSource();
		if (attacker instanceof EntityPlayer) {
			EntityPlayer p = (EntityPlayer) attacker;
			int scavenger = EnchantmentHelper.getEnchantmentLevel(SCAVENGER, p.getHeldItemMainhand());
			if (scavenger > 0 && p.world.rand.nextInt(100) < scavenger * 1.5F) {
				if (dropLoot == null) {
					dropLoot = EntityLivingBase.class.getDeclaredMethod("dropLoot", boolean.class, int.class, DamageSource.class);
					dropLoot.setAccessible(true);
				}
				dropLoot.invoke(e.getEntityLiving(), true, e.getLootingLevel(), e.getSource());
			}
		}
	}

	@SubscribeEvent
	public void breakSpeed(PlayerEvent.BreakSpeed e) {
		EntityPlayer p = e.getEntityPlayer();
		ItemStack stack = p.getHeldItemMainhand();
		if (stack.isEmpty()) return;
		int depth = EnchantmentHelper.getEnchantmentLevel(DEPTH_MINER, stack);
		//Increase or decrease speed based on distance above sea level and ench level.
		if (depth > 0) {
			float effectiveness = (p.world.getSeaLevel() - (float) p.posY) / p.world.getSeaLevel();
			if (effectiveness < 0) effectiveness /= 3;
			float speedChange = 1 + depth * depth * effectiveness;
			e.setNewSpeed(e.getNewSpeed() + speedChange);
		}
		//Counteract the division by 5 for players not on the ground.
		if (!p.onGround && EnchantmentHelper.getEnchantmentLevel(STABLE_FOOTING, stack) > 0) {
			e.setNewSpeed(e.getNewSpeed() * 5F);
		}
		System.out.println(e.getNewSpeed());
	}

	public static void setEnch(ToolMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ToolMaterial.class, mat, ench, "enchantability", "field_78008_j");
	}

	public static void setEnch(ArmorMaterial mat, int ench) {
		ReflectionHelper.setPrivateValue(ArmorMaterial.class, mat, ench, "enchantability", "field_78055_h");
	}

}
