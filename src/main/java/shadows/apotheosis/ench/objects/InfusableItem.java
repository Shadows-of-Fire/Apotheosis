package shadows.apotheosis.ench.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.table.IEnchantableItem;

public class InfusableItem extends BlockItem implements IEnchantableItem {

	protected final Supplier<ItemStack> target;
	protected final Supplier<EnchantmentInstance> enchantment;
	protected final int threshold;

	public InfusableItem(Block block, Supplier<ItemStack> target, Supplier<EnchantmentInstance> enchantment, int threshold) {
		super(block, new Item.Properties().tab(Apotheosis.APOTH_GROUP));
		this.target = target;
		this.enchantment = enchantment;
		this.threshold = threshold;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return stack.getCount() == 1;
	}

	@Override
	public ItemStack onEnchantment(ItemStack stack, List<EnchantmentInstance> enchantments) {
		return target.get();
	}

	@Override
	public List<EnchantmentInstance> selectEnchantments(List<EnchantmentInstance> builtList, Random rand, ItemStack stack, int level, float quanta, float arcana, boolean treasure) {
		List<EnchantmentInstance> ench = new ArrayList<>();
		if (level >= threshold) ench.add(enchantment.get());
		return ench;
	}

}