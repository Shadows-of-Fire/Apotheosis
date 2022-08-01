package shadows.apotheosis.adventure.affix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

/**
 * Helper class for affixes that modify attributes, as the apply method is the same for most of those.
 */
public class AttributeAffix extends Affix {

	protected final Map<LootRarity, ModifierInst> modifiers;
	protected final @Nullable Predicate<LootCategory> types;
	protected final @Nullable Predicate<ItemStack> items;

	public AttributeAffix(Map<LootRarity, ModifierInst> modifiers, @Nullable Predicate<LootCategory> types, @Nullable Predicate<ItemStack> items) {
		super(AffixType.STAT);
		this.modifiers = modifiers;
		this.types = types;
		this.items = items;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, java.util.function.Consumer<Component> list) {
	};

	@Override
	public void addModifiers(ItemStack stack, LootRarity rarity, float level, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) {
			AdventureModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getRegistryName(), stack.getHoverName());
			return;
		}
		ModifierInst modif = modifiers.get(rarity);
		for (EquipmentSlot slot : cat.getSlots(stack)) {
			if (slot == type) {
				map.accept(modif.attr.get(), modif.build(slot, this.getRegistryName(), level));
			}
		}
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) return false;
		return (types == null || types.test(cat)) && (items == null || items.test(stack)) && modifiers.containsKey(rarity);
	};

	public record ModifierInst(Supplier<Attribute> attr, Operation op, Float2FloatFunction valueFactory, Map<EquipmentSlot, UUID> cache) {

		public AttributeModifier build(EquipmentSlot slot, ResourceLocation id, float level) {
			return new AttributeModifier(cache.computeIfAbsent(slot, k -> UUID.randomUUID()), "affix:" + id, valueFactory.get(level), op);
		}
	}

	public static class Builder {

		private final Supplier<Attribute> attr;
		private final Operation op;
		private final Map<LootRarity, ModifierInst> modifiers = new HashMap<>();

		private Predicate<LootCategory> types;
		private Predicate<ItemStack> items;

		public Builder(Supplier<Attribute> attr, Operation op) {
			this.attr = attr;
			this.op = op;
		}

		public Builder types(Predicate<LootCategory> types) {
			this.types = types;
			return this;
		}

		/**
		 * Limits the items this affix can apply to.
		 * Importantly, these are checked after types, so if types are filtered
		 * then it is guaranteed that any checked item is of a valid type.
		 */
		public Builder items(Predicate<ItemStack> items) {
			this.items = items;
			return this;
		}

		public Builder with(LootRarity rarity, Float2FloatFunction valueFactory) {
			this.modifiers.put(rarity, new ModifierInst(attr, op, valueFactory, new HashMap<>()));
			return this;
		}

		public Builder with(LootRarity rarity, float min, float max) {
			return with(rarity, level -> min + level * max);
		}

		public AttributeAffix build(String id) {
			return (AttributeAffix) new AttributeAffix(modifiers, types, items).setRegistryName(id);
		}

	}

}