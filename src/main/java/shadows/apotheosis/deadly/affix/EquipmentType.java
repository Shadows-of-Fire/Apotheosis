package shadows.apotheosis.deadly.affix;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;

public enum EquipmentType {
	SWORD(s -> EquipmentSlotType.MAINHAND),
	RANGED(s -> EquipmentSlotType.MAINHAND),
	PICKAXE(s -> EquipmentSlotType.MAINHAND),
	SHOVEL(s -> EquipmentSlotType.MAINHAND),
	AXE(s -> EquipmentSlotType.MAINHAND),
	ARMOR(s -> ((ArmorItem) s.getItem()).getEquipmentSlot()),
	SHIELD(s -> EquipmentSlotType.OFFHAND);

	final Function<ItemStack, EquipmentSlotType> type;

	EquipmentType(Function<ItemStack, EquipmentSlotType> type) {
		this.type = type;
	}

	public EquipmentSlotType getSlot(ItemStack stack) {
		return this.type.apply(stack);
	}

	@Nullable
	public static EquipmentType getTypeFor(ItemStack stack) {
		Item i = stack.getItem();
		if (i instanceof SwordItem) return SWORD;
		if (i instanceof ShootableItem) return RANGED;
		if (i instanceof ArmorItem) return ARMOR;
		if (i instanceof ShieldItem) return SHIELD;
		if (i instanceof AxeItem) return AXE;
		if (i instanceof PickaxeItem) return PICKAXE;
		if (i instanceof ShovelItem) return SHOVEL;
		return null;
	}
}