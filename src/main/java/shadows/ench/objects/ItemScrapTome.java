package shadows.ench.objects;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.Apotheosis;

public class ItemScrapTome extends ItemBook {

	static Random rand = new Random();

	public ItemScrapTome() {
		this.setRegistryName(Apotheosis.MODID, "scrap_tome");
		this.setTranslationKey(Apotheosis.MODID + "." + getRegistryName().getPath());
		this.setCreativeTab(CreativeTabs.MISC);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.isItemEnchanted()) return;
		tooltip.add(I18n.format("info.apotheosis.scrap_tome"));
		tooltip.add(I18n.format("info.apotheosis.scrap_tome2"));
	}

	@Override
	public IRarity getForgeRarity(ItemStack stack) {
		return !stack.isItemEnchanted() ? super.getForgeRarity(stack) : EnumRarity.UNCOMMON;
	}

	public static void updateAnvil(AnvilUpdateEvent ev) {
		ItemStack weapon = ev.getLeft();
		ItemStack book = ev.getRight();
		if (!(book.getItem() instanceof ItemScrapTome) || book.isItemEnchanted() || !weapon.isItemEnchanted()) return;

		Map<Enchantment, Integer> wepEnch = EnchantmentHelper.getEnchantments(weapon);
		int size = MathHelper.ceil(wepEnch.size() / 2D);
		List<Enchantment> keys = Lists.newArrayList(wepEnch.keySet());
		long seed = 1831;
		for (Enchantment e : keys) {
			seed ^= e.getRegistryName().hashCode();
		}
		rand.setSeed(seed);
		while (wepEnch.keySet().size() > size) {
			Enchantment lost = keys.get(rand.nextInt(keys.size()));
			wepEnch.remove(lost);
			keys.remove(lost);
		}
		ItemStack out = book.copy();
		EnchantmentHelper.setEnchantments(wepEnch, out);
		out.setCount(1);
		ev.setMaterialCost(1);
		ev.setCost(wepEnch.size() * 10);
		ev.setOutput(out);
	}
}
