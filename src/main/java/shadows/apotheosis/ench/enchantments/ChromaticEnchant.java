package shadows.apotheosis.ench.enchantments;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.Util;
import shadows.apotheosis.ench.EnchModule;

public class ChromaticEnchant extends Enchantment {

	private static final Map<DyeColor, IItemProvider> ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), map -> {
		map.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
		map.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
		map.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
		map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
		map.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
		map.put(DyeColor.LIME, Blocks.LIME_WOOL);
		map.put(DyeColor.PINK, Blocks.PINK_WOOL);
		map.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
		map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
		map.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
		map.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
		map.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
		map.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
		map.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
		map.put(DyeColor.RED, Blocks.RED_WOOL);
		map.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
	});

	public ChromaticEnchant() {
		super(Rarity.RARE, EnchModule.SHEARS, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND });
	}

	@Override
	public int getMinCost(int pLevel) {
		return 40;
	}

	public List<ItemStack> molestSheepItems(SheepEntity sheep, ItemStack shears, List<ItemStack> items) {
		if (EnchantmentHelper.getItemEnchantmentLevel(this, shears) > 0) {
			for (int i = 0; i < items.size(); i++) {
				if (ItemTags.WOOL.contains(items.get(i).getItem())) {
					items.set(i, new ItemStack(ITEM_BY_DYE.get(DyeColor.byId(sheep.random.nextInt(16)))));
				}
			}
		}
		return items;
	}

}
