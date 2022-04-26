package shadows.apotheosis.deadly;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.objects.AffixTomeItem;
import shadows.apotheosis.deadly.objects.RarityShardItem;
import shadows.apotheosis.util.ItemAccess;

public class DeadlyModuleClient {

	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(DeadlyModuleClient::tooltips);

		Minecraft.getInstance().getItemColors().register((stack, tint) -> {
			return ((RarityShardItem) stack.getItem()).getRarity().getColor().getValue();
		}, DeadlyModule.RARITY_SHARDS.values().toArray(new Item[6]));

		Minecraft.getInstance().getItemColors().register((stack, tint) -> {
			if (tint != 1) return -1;
			return ((AffixTomeItem) stack.getItem()).getRarity().getColor().getValue();
		}, DeadlyModule.RARITY_TOMES.values().toArray(new Item[6]));
	}

	public static void tooltips(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		List<Component> list = e.getToolTip();
		int rmvIdx = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof TextComponent tc) {
				if (tc.getText().equals("APOTH_REMOVE_MARKER")) {
					list.remove(i);
					rmvIdx = i;
					break;
				}
			}
		}
		if (rmvIdx == -1) return;
		int flags = getHideFlags(stack);
		if (!shouldShowInTooltip(flags, TooltipPart.MODIFIERS) && AffixHelper.getRarity(stack) != null) {
			int stupidLambdaFinal = rmvIdx;
			int oldSize = list.size();
			applyModifierTooltips(e.getPlayer(), stack, c -> list.add(stupidLambdaFinal, c));

			Collections.reverse(list.subList(rmvIdx, rmvIdx + list.size() - oldSize));
		}
	}

	private static boolean shouldShowInTooltip(int pHideFlags, ItemStack.TooltipPart pPart) {
		return (pHideFlags & pPart.getMask()) == 0;
	}

	private static int getHideFlags(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : 0;
	}

	private static void applyModifierTooltips(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip) {

		Multimap<Attribute, AttributeModifier> mainhand = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
		Multimap<Attribute, AttributeModifier> offhand = stack.getAttributeModifiers(EquipmentSlot.OFFHAND);
		Multimap<Attribute, AttributeModifier> dualHand = HashMultimap.create();
		for (Attribute atr : mainhand.keys()) {
			Collection<AttributeModifier> modifMh = mainhand.get(atr);
			Collection<AttributeModifier> modifOh = offhand.get(atr);
			modifMh.stream().filter(a1 -> modifOh.stream().anyMatch(a2 -> a1.getName().equals(a2.getName()))).forEach(modif -> dualHand.put(atr, modif));
		}

		applyTextFor(player, stack, tooltip, dualHand, "both_hands");

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			Multimap<Attribute, AttributeModifier> modifiers = stack.getAttributeModifiers(slot);
			if (slot.ordinal() < 2) dualHand.entries().stream().forEach(e -> modifiers.remove(e.getKey(), e.getValue()));
			applyTextFor(player, stack, tooltip, modifiers, slot.getName());
		}
	}

	private static void applyTextFor(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, Multimap<Attribute, AttributeModifier> modifiers, String group) {
		if (!modifiers.isEmpty()) {
			tooltip.accept(TextComponent.EMPTY);
			tooltip.accept((new TranslatableComponent("item.modifiers." + group)).withStyle(ChatFormatting.GRAY));

			for (Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
				AttributeModifier modifier = entry.getValue();
				double amt = modifier.getAmount();
				boolean isBase = false;
				if (player != null) {
					if (modifier.getId() == ItemAccess.getBaseAD()) {
						amt += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
						amt += (double) EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
						isBase = true;
					} else if (modifier.getId() == ItemAccess.getBaseAS()) {
						amt += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
						isBase = true;
					}
				}

				double displayAmt;
				if (modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
					if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
						displayAmt = amt * 10.0D;
					} else {
						displayAmt = amt;
					}
				} else {
					displayAmt = amt * 100.0D;
				}

				if (isBase) {
					tooltip.accept((new TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + modifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmt), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
				} else if (amt > 0.0D) {
					tooltip.accept((new TranslatableComponent("attribute.modifier.plus." + modifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmt), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
				} else if (amt < 0.0D) {
					displayAmt *= -1.0D;
					tooltip.accept((new TranslatableComponent("attribute.modifier.take." + modifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmt), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.RED));
				}
			}
		}
	}

}
