package shadows.apotheosis.ench.enchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import shadows.apotheosis.Apoth;

public class EndlessQuiverEnchant extends Enchantment {

	public EndlessQuiverEnchant() {
		super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 60;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return 200;
	}

	@Override
	public Component getFullname(int level) {
		return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_GREEN);
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return super.checkCompatibility(ench) && ench != Enchantments.INFINITY_ARROWS;
	}

	public boolean isTrulyInfinite(ItemStack stack, ItemStack bow, Player player) {
		return EnchantmentHelper.getItemEnchantmentLevel(Apoth.Enchantments.ENDLESS_QUIVER, bow) > 0 && stack.getItem() instanceof ArrowItem;
	}
}