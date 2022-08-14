package shadows.apotheosis.adventure.affix.socket;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public final class SocketAffix extends Affix {

	public SocketAffix() {
		super(AffixType.SOCKET);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
	}

	@Override
	public void addModifiers(ItemStack stack, LootRarity rarity, float level, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) {
			AdventureModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getRegistryName(), stack.getHoverName());
			return;
		}

		List<ItemStack> gems = SocketHelper.getGems(stack, (int) level);
		Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
		for (ItemStack s : gems) {
			Pair<Attribute, AttributeModifier> bonus = GemItem.getStoredBonus(s);
			if (bonus != null) modifiers.put(bonus.getKey(), bonus.getValue());
		}

		for (EquipmentSlot s : cat.getSlots(stack)) {
			if (s == type) modifiers.forEach(map);
		}
	}
}
