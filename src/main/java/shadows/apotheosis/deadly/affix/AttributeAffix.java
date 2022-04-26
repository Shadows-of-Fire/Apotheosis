package shadows.apotheosis.deadly.affix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

/**
 * Helper class for affixes that modify attributes, as the apply method is the same for most of those.
 */
public class AttributeAffix extends Affix {

	protected final @Nullable Predicate<LootCategory> types;
	protected final List<ModifierInst> modifiers;
	protected final boolean _isPrefix;

	public AttributeAffix(LootRarity rarity, int weight, boolean isPrefix, @Nullable Predicate<LootCategory> types, ModifierInst... modifiers) {
		super(rarity, weight);
		this.types = types;
		this._isPrefix = isPrefix;
		this.modifiers = Arrays.asList(modifiers);
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
	}

	@Override
	public Component getDisplayName(float level) {
		return new TranslatableComponent("affix." + this.getRegistryName() + ".name", fmt(level * 100)).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public boolean isPrefix() {
		return _isPrefix;
	}

//	@Override
//	public Component chainName(Component name, boolean prefix) {
//		if (prefix) return new TranslatableComponent("%s %s", new TranslatableComponent("affix." + this.name + ".prefix"), name);
//		return new TranslatableComponent("%s %s", name, new TranslatableComponent("affix." + this.name));
//	}

	@Override
	public void addModifiers(ItemStack stack, float level, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) {
			DeadlyModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getRegistryName(), stack.getHoverName());
			return;
		}
		for (EquipmentSlot slot : cat.getSlots(stack)) {
			if (slot == type) {
				this.modifiers.forEach(ins -> map.accept(ins.attr.get(), ins.build(slot, this.getRegistryName(), level)));
			}
		}
	}

	@Override
	public boolean canApply(LootCategory type) {
		return this.types == null || this.types.test(type);
	}

	public record ModifierInst(Supplier<Attribute> attr, Operation op, Function<Float, Float> valueFactory, Map<EquipmentSlot, UUID> cache) {

		public AttributeModifier build(EquipmentSlot slot, ResourceLocation id, float level) {
			return new AttributeModifier(cache.computeIfAbsent(slot, k -> UUID.randomUUID()), "affix:" + id, valueFactory.apply(level), op);
		}
	}

	public static class Builder {

		private final LootRarity rarity;
		private final List<ModifierInst> modifiers = new ArrayList<>();
		private Predicate<LootCategory> types;
		private int weight = 10;
		private boolean isPrefix;

		public Builder(LootRarity rarity) {
			this.rarity = rarity;
		}

		public Builder types(Predicate<LootCategory> types) {
			this.types = types;
			return this;
		}

		public Builder with(Supplier<Attribute> attr, Operation op, Function<Float, Float> valueFactory) {
			this.modifiers.add(new ModifierInst(attr, op, valueFactory, new HashMap<>()));
			return this;
		}

		public Builder with(Attribute attr, Operation op, Function<Float, Float> valueFactory) {
			return with(() -> attr, op, valueFactory);
		}

		public Builder with(Supplier<Attribute> attr, Operation op, float min, float max) {
			return with(attr, op, level -> min + level * (max - min));
		}

		public Builder with(Attribute attr, Operation op, float min, float max) {
			return with(attr, op, level -> min + level * (max - min));
		}

		public Builder weighted(int weight){
			this.weight = weight;
			return this;
		}

		public Builder setPrefix(boolean isPrefix) {
			this.isPrefix = isPrefix;
			return this;
		}

		public AttributeAffix build(String id) {
			return (AttributeAffix) new AttributeAffix(rarity, weight, isPrefix, types, modifiers.toArray(new ModifierInst[0])).setRegistryName(id);
		}

	}

}