package shadows.potion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisPreInit;

public class PotionModule {

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Potion");

	@ObjectHolder("apotheosis:potion_infinity")
	public static final EnchantmentPotionInfinity POTION_INFINITY = null;

	static Configuration config;

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		//config = new Configuration(new File(Apotheosis.configDir, "potion.cfg"));
		//if (config.hasChanged()) config.save();
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new EnchantmentPotionInfinity().setRegistryName(Apotheosis.MODID, "potion_infinity"));
	}

	/**
	 * 
	 * @param a Arrow ItemStack
	 * @param b Bow ItemStack
	 * @param c EntityPlayer using the bow
	 * @return If this arrow should not be consumed.
	 */
	public static boolean isInfinite(Object a, Object b, Object c) {
		ItemStack stack = (ItemStack) a;
		ItemStack bow = (ItemStack) b;
		int enchant = net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.INFINITY, bow);
		if (enchant <= 0 ? false : stack.getItem().getClass() == ItemArrow.class) return true;
		return (POTION_INFINITY != null && EnchantmentHelper.getEnchantmentLevel(POTION_INFINITY, bow) > 0 && stack.getItem() instanceof ItemArrow);
	}

	public static boolean doesShowParticles(Object e) {
		PotionEffect ef = (PotionEffect) e;
		if (ef.getPotion() == MobEffects.INVISIBILITY) return false;
		return ef.showParticles;
	}

}
