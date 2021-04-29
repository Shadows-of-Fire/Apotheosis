package shadows.apotheosis.ench.objects;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import shadows.apotheosis.Apotheosis;

public class ScrappingTomeItem extends BookItem {

	static Random rand = new Random();

	public ScrappingTomeItem() {
		super(new Item.Properties().group(Apotheosis.APOTH_GROUP));
		this.setRegistryName(Apotheosis.MODID, "scrap_tome");
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.isEnchanted()) return;
		tooltip.add(new TranslationTextComponent("info.apotheosis.scrap_tome"));
		tooltip.add(new TranslationTextComponent("info.apotheosis.scrap_tome2"));
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return !stack.isEnchanted() ? super.getRarity(stack) : Rarity.UNCOMMON;
	}

	public static boolean updateAnvil(AnvilUpdateEvent ev) {
		ItemStack weapon = ev.getLeft();
		ItemStack book = ev.getRight();
		if (!(book.getItem() instanceof ScrappingTomeItem) || book.isEnchanted() || !weapon.isEnchanted()) return false;

		Map<Enchantment, Integer> wepEnch = EnchantmentHelper.getEnchantments(weapon);
		int size = MathHelper.ceil(wepEnch.size() / 2D);
		List<Enchantment> keys = Lists.newArrayList(wepEnch.keySet());
		long seed = 1831;
		for (Enchantment e : keys) {
			seed ^= e.getRegistryName().hashCode();
		}
		seed ^= ev.getPlayer().getXPSeed();
		rand.setSeed(seed);
		while (wepEnch.keySet().size() > size) {
			Enchantment lost = keys.get(rand.nextInt(keys.size()));
			wepEnch.remove(lost);
			keys.remove(lost);
		}
		ItemStack out = new ItemStack(Items.ENCHANTED_BOOK);
		EnchantmentHelper.setEnchantments(wepEnch, out);
		ev.setMaterialCost(1);
		ev.setCost(wepEnch.size() * 10);
		ev.setOutput(out);
		return true;
	}
}