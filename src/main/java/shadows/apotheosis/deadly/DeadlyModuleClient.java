package shadows.apotheosis.deadly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import shadows.apotheosis.util.ItemAccess;

public class DeadlyModuleClient {

	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, DeadlyModuleClient::tooltips);
	}

	public static Multimap<Attribute, AttributeModifier> sortedMap() {
		return TreeMultimap.create((k1, k2) -> k1.getRegistryName().compareTo(k2.getRegistryName()), (v1, v2) -> {
			int compOp = Integer.compare(v1.getOperation().ordinal(), v2.getOperation().ordinal());
			int compValue = Double.compare(v2.getAmount(), v1.getAmount());
			return compOp == 0 ? compValue == 0 ? v1.getId().compareTo(v2.getId()) : compValue : compOp;
		});
	}

	public static Multimap<Attribute, AttributeModifier> getSortedMofifiers(ItemStack stack, EquipmentSlot slot) {
		var unsorted = stack.getAttributeModifiers(slot);
		Multimap<Attribute, AttributeModifier> map = sortedMap();
		for (Map.Entry<Attribute, AttributeModifier> ent : unsorted.entries()) {
			if (ent.getKey() != null && ent.getValue() != null) map.put(ent.getKey(), ent.getValue());
			else DeadlyModule.LOGGER.debug("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", stack, ent.getKey(), ent.getValue());
		}
		return map;
	}

	public static void tooltips(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		List<Component> list = e.getToolTip();
		int rmvIdx = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof TextComponent tc) {
				if (tc.getText().equals("APOTH_REMOVE_MARKER")) {
					list.removeAll(list.subList(i, list.size()));
					rmvIdx = i;
					break;
				}
			}
		}
		if (rmvIdx == -1) return;
		int flags = getHideFlags(stack);
		if (!shouldShowInTooltip(flags, TooltipPart.MODIFIERS)) return;
		int stupidLambdaFinal = rmvIdx;
		int oldSize = list.size();
		applyModifierTooltips(e.getPlayer(), stack, c -> list.add(stupidLambdaFinal, c));
		Collections.reverse(list.subList(rmvIdx, rmvIdx + list.size() - oldSize));
	}

	private static boolean shouldShowInTooltip(int pHideFlags, ItemStack.TooltipPart pPart) {
		return (pHideFlags & pPart.getMask()) == 0;
	}

	private static int getHideFlags(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : 0;
	}

	private static void applyModifierTooltips(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip) {
		Multimap<Attribute, AttributeModifier> mainhand = getSortedMofifiers(stack, EquipmentSlot.MAINHAND);
		Multimap<Attribute, AttributeModifier> offhand = getSortedMofifiers(stack, EquipmentSlot.OFFHAND);
		Multimap<Attribute, AttributeModifier> dualHand = sortedMap();
		for (Attribute atr : mainhand.keys()) {
			Collection<AttributeModifier> modifMh = mainhand.get(atr);
			Collection<AttributeModifier> modifOh = offhand.get(atr);
			modifMh.stream().filter(a1 -> modifOh.stream().anyMatch(a2 -> a1.getName().equals(a2.getName()))).forEach(modif -> dualHand.put(atr, modif));
		}

		applyTextFor(player, stack, tooltip, dualHand, "both_hands");

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			Multimap<Attribute, AttributeModifier> modifiers = getSortedMofifiers(stack, slot);
			if (slot.ordinal() < 2) dualHand.entries().stream().forEach(e -> modifiers.remove(e.getKey(), e.getValue()));
			applyTextFor(player, stack, tooltip, modifiers, slot.getName());
		}
	}

	private static MutableComponent padded(String padding, Component comp) {
		return new TextComponent(padding).append(comp);
	}

	private static MutableComponent list() {
		return new TextComponent(" \u2507 ").withStyle(ChatFormatting.GRAY);
	}

	private static Component toComponent(Attribute attr, AttributeModifier modif, @Nullable Player player) {
		double amt = modif.getAmount();

		if (modif.getOperation() == Operation.ADDITION) {
			if (attr == Attributes.KNOCKBACK_RESISTANCE) amt *= 10.0D;
		} else {
			amt *= 100.0D;
		}

		int code = modif.getOperation().ordinal();

		if (amt > 0.0D) {
			return new TranslatableComponent("attribute.modifier.plus." + code, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), new TranslatableComponent(attr.getDescriptionId())).withStyle(ChatFormatting.BLUE);
		} else {
			amt *= -1.0D;
			return new TranslatableComponent("attribute.modifier.take." + code, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), new TranslatableComponent(attr.getDescriptionId())).withStyle(ChatFormatting.RED);
		}
	}

	private static void applyTextFor(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, Multimap<Attribute, AttributeModifier> modifierMap, String group) {
		if (!modifierMap.isEmpty()) {
			tooltip.accept(TextComponent.EMPTY);
			tooltip.accept((new TranslatableComponent("item.modifiers." + group)).withStyle(ChatFormatting.GRAY));

			AttributeModifier baseAD = null, baseAS = null;
			List<AttributeModifier> dmgModifs = new ArrayList<>(), spdModifs = new ArrayList<>();

			for (AttributeModifier modif : modifierMap.get(Attributes.ATTACK_DAMAGE)) {
				if (modif.getId() == ItemAccess.getBaseAD()) baseAD = modif;
				else dmgModifs.add(modif);
			}

			for (AttributeModifier modif : modifierMap.get(Attributes.ATTACK_SPEED)) {
				if (modif.getId() == ItemAccess.getBaseAS()) baseAS = modif;
				else spdModifs.add(modif);
			}

			if (baseAD != null) {
				double base = baseAD.getAmount() + (player == null ? 0 : player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
				double rawBase = base;
				double amt = base;
				for (AttributeModifier modif : dmgModifs) {
					if (modif.getOperation() == Operation.ADDITION) base = amt = amt + modif.getAmount();
					else if (modif.getOperation() == Operation.MULTIPLY_BASE) amt += modif.getAmount() * base;
					else amt *= (1 + modif.getAmount());
				}
				amt += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
				MutableComponent text = new TranslatableComponent("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), new TranslatableComponent(Attributes.ATTACK_DAMAGE.getDescriptionId()));
				tooltip.accept(padded(" ", text).withStyle(dmgModifs.isEmpty() ? ChatFormatting.DARK_GREEN : ChatFormatting.GOLD));
				if (Screen.hasShiftDown() && !dmgModifs.isEmpty()) {
					text = new TranslatableComponent("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rawBase), new TranslatableComponent(Attributes.ATTACK_DAMAGE.getDescriptionId()));
					tooltip.accept(list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
					for (AttributeModifier modifier : dmgModifs) {
						tooltip.accept(list().append(toComponent(Attributes.ATTACK_DAMAGE, modifier, player)));
					}
					float bonus = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
					if (bonus > 0) {
						tooltip.accept(list().append(new TranslatableComponent("attribute.modifier.plus.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(bonus), new TranslatableComponent(Attributes.ATTACK_DAMAGE.getDescriptionId())).withStyle(ChatFormatting.BLUE)));
					}
				}
			}

			if (baseAS != null) {
				double base = baseAS.getAmount() + (player == null ? 0 : player.getAttributeBaseValue(Attributes.ATTACK_SPEED));
				double rawBase = base;
				double amt = base;
				for (AttributeModifier modif : spdModifs) {
					if (modif.getOperation() == Operation.ADDITION) base = amt = amt + modif.getAmount();
					else if (modif.getOperation() == Operation.MULTIPLY_BASE) amt += modif.getAmount() * base;
					else amt *= (1 + modif.getAmount());
				}
				MutableComponent text = new TranslatableComponent("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), new TranslatableComponent(Attributes.ATTACK_SPEED.getDescriptionId()));
				tooltip.accept(new TextComponent(" ").append(text).withStyle(spdModifs.isEmpty() ? ChatFormatting.DARK_GREEN : ChatFormatting.GOLD));
				if (Screen.hasShiftDown() && !spdModifs.isEmpty()) {
					text = new TranslatableComponent("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rawBase), new TranslatableComponent(Attributes.ATTACK_SPEED.getDescriptionId()));
					tooltip.accept(list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
					for (AttributeModifier modifier : spdModifs) {
						tooltip.accept(list().append(toComponent(Attributes.ATTACK_SPEED, modifier, player)));
					}
				}
			}

			for (Attribute attr : modifierMap.keySet()) {
				if (baseAD != null && attr == Attributes.ATTACK_DAMAGE) continue;
				if (baseAS != null && attr == Attributes.ATTACK_SPEED) continue;
				Collection<AttributeModifier> modifs = modifierMap.get(attr);
				if (modifs.size() > 1) {
					double[] sums = new double[3];
					boolean[] merged = new boolean[3];
					Map<Operation, List<AttributeModifier>> shiftExpands = new HashMap<>();
					for (AttributeModifier modifier : modifs) {
						if (sums[modifier.getOperation().ordinal()] != 0) merged[modifier.getOperation().ordinal()] = true;
						sums[modifier.getOperation().ordinal()] += modifier.getAmount();
						shiftExpands.computeIfAbsent(modifier.getOperation(), k -> new LinkedList<>()).add(modifier);
					}
					for (int i = 0; i < 3; i++) {
						if (sums[i] == 0) continue;
						String key = "attribute.modifier." + (sums[i] < 0 ? "take." : "plus.") + i;
						Style style;
						if (merged[i]) style = sums[i] < 0 ? Style.EMPTY.withColor(TextColor.fromRgb(0xF93131)) : Style.EMPTY.withColor(TextColor.fromRgb(0x7A7AF9));
						else style = sums[i] < 0 ? Style.EMPTY.withColor(ChatFormatting.RED) : Style.EMPTY.withColor(ChatFormatting.BLUE);
						if (sums[i] < 0) sums[i] *= -1;
						tooltip.accept(new TranslatableComponent(key, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(sums[i]), new TranslatableComponent(attr.getDescriptionId())).withStyle(style));
						if (merged[i] && Screen.hasShiftDown()) {
							shiftExpands.get(Operation.fromValue(i)).forEach(modif -> tooltip.accept(list().append(toComponent(attr, modif, player))));
						}
					}
				} else modifs.forEach(m -> tooltip.accept(toComponent(attr, m, player)));
			}
		}
	}

}
