package shadows.apotheosis.ench.objects;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.EnchModule;

public class TomeItem extends BookItem {

	final ItemStack rep;
	final EnchantmentType type;

	public TomeItem(Item rep, EnchantmentType type) {
		super(new Item.Properties().group(Apotheosis.APOTH_GROUP));
		this.type = type;
		this.rep = new ItemStack(rep);
		this.setRegistryName(Apotheosis.MODID, (type == null ? "null" : type.name().toLowerCase(Locale.ROOT)) + "_book");
		EnchModule.TYPED_BOOKS.add(this);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return stack.getCount() == 1;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		if (this.type == null) return EnchModule.TYPED_BOOKS.stream().filter(b -> b != this).allMatch(b -> !enchantment.canApply(new ItemStack(b)));
		return enchantment.type == this.type || enchantment.canApplyAtEnchantingTable(this.rep);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("info.apotheosis." + this.getRegistryName().getPath()).mergeStyle(TextFormatting.GRAY));
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return !stack.isEnchanted() ? super.getRarity(stack) : Rarity.UNCOMMON;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.isEnchanted()) {
			ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
			EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack), book);
			return ActionResult.resultConsume(book);
		}
		return ActionResult.resultPass(stack);
	}

}