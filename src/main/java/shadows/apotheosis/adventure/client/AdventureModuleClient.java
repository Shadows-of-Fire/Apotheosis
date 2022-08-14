package shadows.apotheosis.adventure.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.mojang.datafixers.util.Either;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.socket.GemItem;
import shadows.apotheosis.adventure.affix.socket.SocketHelper;
import shadows.apotheosis.adventure.client.SocketTooltipRenderer.SocketComponent;
import shadows.apotheosis.util.ItemAccess;
import shadows.placebo.util.AttributeHelper;

public class AdventureModuleClient {

	public static void init() {
		MinecraftForge.EVENT_BUS.register(AdventureModuleClient.class);
		MinecraftForgeClient.registerTooltipComponentFactory(SocketComponent.class, SocketTooltipRenderer::new);
		ItemProperties.register(Apoth.Items.GEM, new ResourceLocation(Apotheosis.MODID, "gem_variant"), (stack, level, entity, seed) -> {
			return GemItem.getVariant(stack);
		});
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
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

	@SubscribeEvent
	public static void comps(RenderTooltipEvent.GatherComponents e) {
		Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(e.getItemStack());
		if (affixes.containsKey(Apoth.Affixes.SOCKET)) {
			int size = (int) affixes.get(Apoth.Affixes.SOCKET).level();
			e.getTooltipElements().add(Either.right(new SocketComponent(SocketHelper.getGems(e.getItemStack(), size))));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void affixTooltips(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.hasTag()) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
			List<Component> components = new ArrayList<>();
			affixes.values().forEach(inst -> inst.addInformation(components::add));
			e.getToolTip().addAll(1, components);
		}
	}

	public static Multimap<Attribute, AttributeModifier> sortedMap() {
		return TreeMultimap.create((k1, k2) -> k1.getRegistryName().compareTo(k2.getRegistryName()), (v1, v2) -> {
			int compOp = Integer.compare(v1.getOperation().ordinal(), v2.getOperation().ordinal());
			int compValue = Double.compare(v2.getAmount(), v1.getAmount());
			return compOp == 0 ? compValue == 0 ? v1.getId().compareTo(v2.getId()) : compValue : compOp;
		});
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
		return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : 0;
	}

	private static void applyModifierTooltips(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip) {
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

		int sockets = SocketHelper.getSockets(stack);
		Set<UUID> skips = new HashSet<>();
		if (sockets > 0) {
			for (ItemStack gem : SocketHelper.getGems(stack, sockets)) {
				var modif = GemItem.getStoredBonus(gem);
				if (modif != null) skips.add(modif.getValue().getId());
			}
		}

		applyTextFor(player, stack, tooltip, dualHand, "both_hands", skips);
		applyTextFor(player, stack, tooltip, mainhand, EquipmentSlot.MAINHAND.getName(), skips);
		applyTextFor(player, stack, tooltip, offhand, EquipmentSlot.OFFHAND.getName(), skips);

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.ordinal() < 2) continue;
			Multimap<Attribute, AttributeModifier> modifiers = getSortedModifiers(stack, slot);
			applyTextFor(player, stack, tooltip, modifiers, slot.getName(), skips);
		}
	}

	private static MutableComponent padded(String padding, Component comp) {
		return new TextComponent(padding).append(comp);
	}

	private static MutableComponent list() {
		return new TextComponent(" \u2507 ").withStyle(ChatFormatting.GRAY);
	}

	private static void applyTextFor(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, Multimap<Attribute, AttributeModifier> modifierMap, String group, Set<UUID> skips) {
		if (!modifierMap.isEmpty()) {
			modifierMap.values().removeIf(m -> skips.contains(m.getId()));
			if (modifierMap.isEmpty()) return;

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
						tooltip.accept(list().append(AttributeHelper.toComponent(Attributes.ATTACK_DAMAGE, modifier)));
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
						tooltip.accept(list().append(AttributeHelper.toComponent(Attributes.ATTACK_SPEED, modifier)));
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
						if (modifier.getAmount() == 0) continue;
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
						if (attr == Attributes.KNOCKBACK_RESISTANCE) sums[i] *= 10;
						tooltip.accept(new TranslatableComponent(key, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(sums[i]), new TranslatableComponent(attr.getDescriptionId())).withStyle(style));
						if (merged[i] && Screen.hasShiftDown()) {
							shiftExpands.get(Operation.fromValue(i)).forEach(modif -> tooltip.accept(list().append(AttributeHelper.toComponent(attr, modif))));
						}
					}
				} else modifs.forEach(m -> {
					if (m.getAmount() != 0) tooltip.accept(AttributeHelper.toComponent(attr, m));
				});
			}
		}
	}

}
