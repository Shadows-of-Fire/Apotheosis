package shadows.apotheosis.core.attributeslib.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.core.attributeslib.api.AddAttributeTooltipsEvent;
import shadows.apotheosis.core.attributeslib.api.GatherSkippedAttributeTooltipsEvent;
import shadows.apotheosis.core.attributeslib.api.IFormattableAttribute;
import shadows.apotheosis.util.ItemAccess;

public class AttributesLibClient {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void tooltips(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		List<Component> list = e.getToolTip();
		int markIdx1 = -1, markIdx2 = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getContents() instanceof LiteralContents tc) {
				if (tc.text().equals("APOTH_REMOVE_MARKER")) {
					markIdx1 = i;
				}
				if (tc.text().equals("APOTH_REMOVE_MARKER_2")) {
					markIdx2 = i;
					break;
				}
			}
		}
		if (markIdx1 == -1 || markIdx2 == -1) return;
		var it = list.listIterator(markIdx1);
		for (int i = markIdx1; i < markIdx2 + 1; i++) {
			it.next();
			it.remove();
		}
		int flags = getHideFlags(stack);
		if (shouldShowInTooltip(flags, TooltipPart.MODIFIERS)) {
			applyModifierTooltips(e.getEntity(), stack, it::add, e.getFlags());
		}
		MinecraftForge.EVENT_BUS.post(new AddAttributeTooltipsEvent(stack, e.getEntity(), list, it, e.getFlags()));
	}

	public static Multimap<Attribute, AttributeModifier> sortedMap() {
		return TreeMultimap.create((k1, k2) -> id(k1).compareTo(id(k2)), (v1, v2) -> {
			int compOp = Integer.compare(v1.getOperation().ordinal(), v2.getOperation().ordinal());
			int compValue = Double.compare(v2.getAmount(), v1.getAmount());
			return compOp == 0 ? compValue == 0 ? v1.getId().compareTo(v2.getId()) : compValue : compOp;
		});
	}

	private static ResourceLocation id(Attribute attr) {
		return ForgeRegistries.ATTRIBUTES.getKey(attr);
	}

	public static Multimap<Attribute, AttributeModifier> getSortedModifiers(ItemStack stack, EquipmentSlot slot) {
		var unsorted = stack.getAttributeModifiers(slot);
		Multimap<Attribute, AttributeModifier> map = sortedMap();
		for (Map.Entry<Attribute, AttributeModifier> ent : unsorted.entries()) {
			if (ent.getKey() != null && ent.getValue() != null) map.put(ent.getKey(), ent.getValue());
			else AdventureModule.LOGGER.debug("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", stack, ent.getKey(), ent.getValue());
		}
		return map;
	}

	private static boolean shouldShowInTooltip(int pHideFlags, ItemStack.TooltipPart pPart) {
		return (pHideFlags & pPart.getMask()) == 0;
	}

	private static int getHideFlags(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : stack.getItem().getDefaultTooltipHideFlags(stack);
	}

	private static void applyModifierTooltips(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, TooltipFlag flag) {
		Multimap<Attribute, AttributeModifier> mainhand = getSortedModifiers(stack, EquipmentSlot.MAINHAND);
		Multimap<Attribute, AttributeModifier> offhand = getSortedModifiers(stack, EquipmentSlot.OFFHAND);
		Multimap<Attribute, AttributeModifier> dualHand = sortedMap();
		for (Attribute atr : mainhand.keys()) {
			Collection<AttributeModifier> modifMh = mainhand.get(atr);
			Collection<AttributeModifier> modifOh = offhand.get(atr);
			modifMh.stream().filter(a1 -> modifOh.stream().anyMatch(a2 -> a1.getName().equals(a2.getName()))).forEach(modif -> dualHand.put(atr, modif));
		}

		dualHand.values().forEach(m -> {
			mainhand.values().remove(m);
			offhand.values().removeIf(m1 -> m1.getName().equals(m.getName()));
		});

		Set<UUID> skips = new HashSet<>();
		MinecraftForge.EVENT_BUS.post(new GatherSkippedAttributeTooltipsEvent(stack, player, skips, flag));

		applyTextFor(player, stack, tooltip, dualHand, "both_hands", skips, flag);
		applyTextFor(player, stack, tooltip, mainhand, EquipmentSlot.MAINHAND.getName(), skips, flag);
		applyTextFor(player, stack, tooltip, offhand, EquipmentSlot.OFFHAND.getName(), skips, flag);

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.ordinal() < 2) continue;
			Multimap<Attribute, AttributeModifier> modifiers = getSortedModifiers(stack, slot);
			applyTextFor(player, stack, tooltip, modifiers, slot.getName(), skips, flag);
		}
	}

	private static MutableComponent padded(String padding, Component comp) {
		return Component.literal(padding).append(comp);
	}

	private static MutableComponent list() {
		return Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);
	}

	private static void applyTextFor(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, Multimap<Attribute, AttributeModifier> modifierMap, String group, Set<UUID> skips, TooltipFlag flag) {
		if (!modifierMap.isEmpty()) {
			modifierMap.values().removeIf(m -> skips.contains(m.getId()));

			tooltip.accept(Component.empty());
			tooltip.accept(Component.translatable("item.modifiers." + group).withStyle(ChatFormatting.GRAY));

			if (modifierMap.isEmpty()) return;

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
					else amt *= 1 + modif.getAmount();
				}
				amt += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
				MutableComponent text = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), Component.translatable(Attributes.ATTACK_DAMAGE.getDescriptionId()));
				tooltip.accept(padded(" ", text).withStyle(dmgModifs.isEmpty() ? ChatFormatting.DARK_GREEN : ChatFormatting.GOLD));
				if (Screen.hasShiftDown() && !dmgModifs.isEmpty()) {
					text = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rawBase), Component.translatable(Attributes.ATTACK_DAMAGE.getDescriptionId()));
					tooltip.accept(list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
					for (AttributeModifier modifier : dmgModifs) {
						tooltip.accept(list().append(IFormattableAttribute.toComponent(Attributes.ATTACK_DAMAGE, modifier, flag)));
					}
					float bonus = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
					if (bonus > 0) {
						tooltip.accept(list().append(Component.translatable("attribute.modifier.plus.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(bonus), Component.translatable(Attributes.ATTACK_DAMAGE.getDescriptionId())).withStyle(ChatFormatting.BLUE)));
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
					else amt *= 1 + modif.getAmount();
				}
				MutableComponent text = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), Component.translatable(Attributes.ATTACK_SPEED.getDescriptionId()));
				tooltip.accept(Component.literal(" ").append(text).withStyle(spdModifs.isEmpty() ? ChatFormatting.DARK_GREEN : ChatFormatting.GOLD));
				if (Screen.hasShiftDown() && !spdModifs.isEmpty()) {
					text = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rawBase), Component.translatable(Attributes.ATTACK_SPEED.getDescriptionId()));
					tooltip.accept(list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
					for (AttributeModifier modifier : spdModifs) {
						tooltip.accept(list().append(IFormattableAttribute.toComponent(Attributes.ATTACK_SPEED, modifier, flag)));
					}
				}
			}

			for (Attribute attr : modifierMap.keySet()) {
				if ((baseAD != null && attr == Attributes.ATTACK_DAMAGE) || (baseAS != null && attr == Attributes.ATTACK_SPEED)) continue;
				Collection<AttributeModifier> modifs = modifierMap.get(attr);
				if (modifs.size() > 1) {
					double[] sums = new double[3];
					boolean[] merged = new boolean[3];
					Map<Operation, List<AttributeModifier>> shiftExpands = new HashMap<>();
					for (AttributeModifier modifier : modifs) {
						if (modifier.getAmount() == 0) continue;
						if (sums[modifier.getOperation().ordinal()] != 0) merged[modifier.getOperation().ordinal()] = true;
						sums[modifier.getOperation().ordinal()] += modifier.getAmount();
						shiftExpands.computeIfAbsent(modifier.getOperation(), k -> new LinkedList<>()).add(modifier);
					}
					for (int i = 0; i < 3; i++) {
						if (sums[i] == 0) continue;
						String key = "attribute.modifier." + (sums[i] < 0 ? "take." : "plus.") + i;
						if (i != 0) key = "attribute.modifier.apotheosis." + (sums[i] < 0 ? "take." : "plus.") + i;
						Style style;
						if (merged[i]) style = sums[i] < 0 ? Style.EMPTY.withColor(TextColor.fromRgb(0xF93131)) : Style.EMPTY.withColor(TextColor.fromRgb(0x7A7AF9));
						else style = sums[i] < 0 ? Style.EMPTY.withColor(ChatFormatting.RED) : Style.EMPTY.withColor(ChatFormatting.BLUE);
						if (sums[i] < 0) sums[i] *= -1;
						if (attr == Attributes.KNOCKBACK_RESISTANCE) sums[i] *= 10;
						tooltip.accept(Component.translatable(key, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(sums[i]), Component.translatable(attr.getDescriptionId())).withStyle(style));
						if (merged[i] && Screen.hasShiftDown()) {
							shiftExpands.get(Operation.fromValue(i)).forEach(modif -> tooltip.accept(list().append(IFormattableAttribute.toComponent(attr, modif, flag))));
						}
					}
				} else modifs.forEach(m -> {
					if (m.getAmount() != 0) tooltip.accept(IFormattableAttribute.toComponent(attr, m, flag));
				});
			}
		}
	}

}
