package shadows.apotheosis.deadly.affix.impl.melee;

import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.util.FloatValueRange;

import javax.annotation.Nullable;

/**
 * Slain monsters have a chance to explode into a loot pinata.
 */
public class LootPinataAffix extends RangedAffix {

	public LootPinataAffix(LootRarity rarity, float min, float max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory == LootCategory.SWORD;
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		return super.generateLevel(stack, rand, modifier);
	}

	@Override
	public boolean isPrefix() {
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", fmt(level * 100)));
	}

	@Override
	public Component getDisplayName(float level) {
		return new TranslatableComponent("affix." + this.getRegistryName() + ".name", fmt(level * 100)).withStyle(ChatFormatting.GRAY);
	}
}