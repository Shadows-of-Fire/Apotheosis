package shadows.apotheosis.adventure.affix.socket;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
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
			tooltip.add(new TextComponent("Errored gem with no bonus!").withStyle(ChatFormatting.GRAY));
			return;
		}
		tooltip.add(TextComponent.EMPTY);
		tooltip.add(new TranslatableComponent("item.modifiers.socket").withStyle(ChatFormatting.GOLD));
		tooltip.add(toComponent(bonus.getKey(), bonus.getValue()));
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

	public static void setStoredBonus(ItemStack stack, Attribute attrib, AttributeModifier modif) {
		CompoundTag tag = modif.save();
		tag.putString("attribute", attrib.getRegistryName().toString());
		stack.getOrCreateTag().put(MODIFIER, tag);
	}

	public static void setVariant(ItemStack stack, int variant) {
		stack.getOrCreateTag().putInt("variant", variant);
	}

	public static int getVariant(ItemStack stack) {
		return stack.hasTag() ? stack.getTag().getInt("variant") : 0;
	}

	public static ItemStack fromGem(Gem gem, Random rand) {
		ItemStack stack = new ItemStack(Apoth.Items.GEM);
		setVariant(stack, gem.getVariant());
		setStoredBonus(stack, gem.attribute, new AttributeModifier("GemBonus_" + gem.getId(), gem.value.get(rand.nextFloat()), gem.operation));
		return stack;
	}

	@Override
	public boolean canBeHurtBy(DamageSource src) {
		return super.canBeHurtBy(src) && src != DamageSource.ANVIL;
	}

	/**
	 * Copy of {@link AttributeHelper#toComponent(Attribute, AttributeModifier)}
	 * Uses Apoth-specific translation keys that differentiate between +%Base and +%Total
	 */
	public static Component toComponent(Attribute attr, AttributeModifier modif) {
		double amt = modif.getAmount();

		if (modif.getOperation() == Operation.ADDITION) {
			if (attr == Attributes.KNOCKBACK_RESISTANCE) amt *= 10.0D;
		} else {
			amt *= 100.0D;
		}

		int code = modif.getOperation().ordinal();
		String key = code == 0 ? "attribute.modifier." : "attribute.modifier.apotheosis.";
		if (amt > 0.0D) {
			return new TranslatableComponent(key + "plus." + code, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), new TranslatableComponent(attr.getDescriptionId())).withStyle(ChatFormatting.BLUE);
		} else {
			amt *= -1.0D;
			return new TranslatableComponent(key + "take." + code, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), new TranslatableComponent(attr.getDescriptionId())).withStyle(ChatFormatting.RED);
		}
	}

}
