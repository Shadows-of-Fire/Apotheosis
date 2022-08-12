package shadows.apotheosis.adventure.affix.socket;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.placebo.util.AttributeHelper;

public class GemItem extends Item {

	public static final String MODIFIER = "modifier";

	public GemItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
		var bonus = getStoredBonus(pStack);
		if (bonus == null) {
			tooltip.add(new TextComponent("Errored gem with no bonus!"));
			return;
		}
		tooltip.add(TextComponent.EMPTY);
		tooltip.add((new TranslatableComponent("item.modifiers.socket")).withStyle(ChatFormatting.GOLD));
		tooltip.add(AttributeHelper.toComponent(bonus.getKey(), bonus.getValue()));
	}

	@Override
	public String getDescriptionId(ItemStack pStack) {
		int variant = getVariant(pStack);
		return super.getDescriptionId(pStack) + "." + variant;
	}

	@Nullable
	public static Pair<Attribute, AttributeModifier> getStoredBonus(ItemStack stack) {
		CompoundTag tag = stack.getTagElement(MODIFIER);
		if (tag == null) return null;
		Attribute attrib = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(tag.getString("attribute")));
		if (attrib == null) return null;
		AttributeModifier modif = AttributeModifier.load(tag);
		if (modif == null) return null;
		return Pair.of(attrib, modif);
	}

	@Nullable
	public static void setStoredBonus(ItemStack stack, Attribute attrib, AttributeModifier modif) {
		CompoundTag tag = modif.save();
		tag.putString("attribute", attrib.getRegistryName().toString());
		stack.getOrCreateTag().put(MODIFIER, tag);
	}

	public static int getVariant(ItemStack stack) {
		return stack.hasTag() ? stack.getTag().getInt("variant") : 0;
	}

}
