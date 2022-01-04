package shadows.apotheosis.deadly.affix;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.common.ToolActions;
import shadows.apotheosis.deadly.config.DeadlyConfig;

public enum EquipmentType {
	SWORD(s -> EquipmentSlot.MAINHAND),
	RANGED(s -> EquipmentSlot.MAINHAND),
	PICKAXE(s -> EquipmentSlot.MAINHAND),
	SHOVEL(s -> EquipmentSlot.MAINHAND),
	AXE(s -> EquipmentSlot.MAINHAND),
	ARMOR(s -> ((ArmorItem) s.getItem()).getSlot()),
	SHIELD(s -> EquipmentSlot.OFFHAND);

	final Function<ItemStack, EquipmentSlot> type;

	EquipmentType(Function<ItemStack, EquipmentSlot> type) {
		this.type = type;
	}

	public EquipmentSlot getSlot(ItemStack stack) {
		return this.type.apply(stack);
	}

	@Nullable
	public static EquipmentType getTypeFor(ItemStack stack) {
		Item i = stack.getItem();
		if (DeadlyConfig.TYPE_OVERRIDES.containsKey(i.getRegistryName())) return DeadlyConfig.TYPE_OVERRIDES.get(i.getRegistryName());
		if (i instanceof ProjectileWeaponItem) return RANGED;
		if (i instanceof ArmorItem) return ARMOR;
		if (stack.canPerformAction(ToolActions.SHIELD_BLOCK)) return SHIELD;
		if (ToolActions.DEFAULT_PICKAXE_ACTIONS.stream().anyMatch(a -> stack.canPerformAction(a))) return PICKAXE;
		if (ToolActions.DEFAULT_AXE_ACTIONS.stream().anyMatch(a -> stack.canPerformAction(a))) return AXE;
		if (ToolActions.DEFAULT_SHOVEL_ACTIONS.stream().anyMatch(a -> stack.canPerformAction(a))) return SHOVEL;
		if (i.getAttributeModifiers(EquipmentSlot.MAINHAND, stack).get(Attributes.ATTACK_DAMAGE).stream().anyMatch(m -> m.getAmount() > 0)) return SWORD;
		return null;
	}
}