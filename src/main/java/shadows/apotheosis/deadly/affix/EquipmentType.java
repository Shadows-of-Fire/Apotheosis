package shadows.apotheosis.deadly.affix;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.common.ToolType;
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
		if (i.isShield(stack, null)) return SHIELD;
		if (i.getToolTypes(stack).contains(ToolType.PICKAXE)) return PICKAXE;
		if (i.getToolTypes(stack).contains(ToolType.AXE)) return AXE;
		if (i.getToolTypes(stack).contains(ToolType.SHOVEL)) return SHOVEL;
		if (i.getAttributeModifiers(EquipmentSlot.MAINHAND, stack).get(Attributes.ATTACK_DAMAGE).stream().anyMatch(m -> m.getAmount() > 0)) return SWORD;
		return null;
	}
}