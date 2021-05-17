package shadows.apotheosis.deadly.affix;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraftforge.common.ToolType;
import shadows.apotheosis.deadly.config.DeadlyConfig;

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
		if (DeadlyConfig.TYPE_OVERRIDES.containsKey(i.getRegistryName())) return DeadlyConfig.TYPE_OVERRIDES.get(i.getRegistryName());
		if (i instanceof ShootableItem) return RANGED;
		if (i instanceof ArmorItem) return ARMOR;
		if (i.isShield(stack, null)) return SHIELD;
		if (i.getToolTypes(stack).contains(ToolType.PICKAXE)) return PICKAXE;
		if (i.getToolTypes(stack).contains(ToolType.AXE)) return AXE;
		if (i.getToolTypes(stack).contains(ToolType.SHOVEL)) return SHOVEL;
		if (i.getAttributeModifiers(EquipmentSlotType.MAINHAND, stack).get(Attributes.ATTACK_DAMAGE).stream().anyMatch(m -> m.getAmount() > 0)) return SWORD;
		return null;
	}
}