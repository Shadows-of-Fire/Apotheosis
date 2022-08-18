package shadows.apotheosis.adventure.affix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
	};

	@Override
	public void addModifiers(ItemStack stack, LootRarity rarity, float level, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) {
			AdventureModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getRegistryName(), stack.getHoverName().getString());
			return;
		}
		ModifierInst modif = this.modifiers.get(rarity);
		if (modif.attr.get() == null) {
			AdventureModule.LOGGER.debug("The affix {} has attempted to apply a null attribute modifier to {}!", this.getRegistryName(), stack.getHoverName().getString());
			return;
		}
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
		return (this.types == null || this.types.test(cat)) && (this.items == null || this.items.test(stack)) && this.modifiers.containsKey(rarity);
	};

	public record ModifierInst(Supplier<Attribute> attr, Operation op, Float2FloatFunction valueFactory, Map<EquipmentSlot, UUID> cache) {

		public AttributeModifier build(EquipmentSlot slot, ResourceLocation id, float level) {
			return new AttributeModifier(this.cache.computeIfAbsent(slot, k -> UUID.randomUUID()), "affix:" + id, this.valueFactory.get(level), this.op);
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
			this.modifiers.put(rarity, new ModifierInst(this.attr, this.op, valueFactory, new HashMap<>()));
			return this;
		}

		public Builder with(LootRarity rarity, float min, float max) {
			return this.with(rarity, level -> min + level * max);
		}

		public AttributeAffix build(String id) {
			return (AttributeAffix) new AttributeAffix(this.modifiers, this.types, this.items).setRegistryName(id);
		}

	}

}