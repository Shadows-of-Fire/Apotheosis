package shadows.apotheosis.ench.objects;

import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.EnchModule;

public class TomeItem extends BookItem {

	final ItemStack rep;
	final EnchantmentCategory type;

	public TomeItem(Item rep, EnchantmentCategory type) {
		super(new Item.Properties().tab(Apotheosis.APOTH_GROUP));
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
		if (this.type == null) return EnchModule.TYPED_BOOKS.stream().filter(b -> b != this).allMatch(b -> !enchantment.canEnchant(new ItemStack(b)));
		return enchantment.category == this.type || enchantment.canApplyAtEnchantingTable(this.rep);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(new TranslatableComponent("info.apotheosis." + this.getRegistryName().getPath()).withStyle(ChatFormatting.GRAY));
		if (stack.isEnchanted()) {
			tooltip.add(new TranslatableComponent("info.apotheosis.tome_error").withStyle(ChatFormatting.RED));
		}
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return !stack.isEnchanted() ? super.getRarity(stack) : Rarity.UNCOMMON;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.isEnchanted()) {
			ItemStack book = new ItemStack(Items.ENCHANTED_BOOK, stack.getCount());
			EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack), book);
			return InteractionResultHolder.consume(book);
		}
		return InteractionResultHolder.pass(stack);
	}

}