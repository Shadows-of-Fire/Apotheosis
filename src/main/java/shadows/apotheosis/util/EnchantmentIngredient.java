package shadows.apotheosis.util;

import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import shadows.apotheosis.deadly.affix.AffixHelper;

public class EnchantmentIngredient extends Ingredient {

	protected final IItemProvider item;
	protected final Enchantment enchantment;
	protected final int minLevel;

	public EnchantmentIngredient(IItemProvider item, Enchantment enchantment, int minLevel) {
		super(Stream.of(new Ingredient.SingleItemList(format(item, enchantment, minLevel))));
		this.item = item;
		this.enchantment = enchantment;
		this.minLevel = minLevel;
	}

	private static ItemStack format(IItemProvider item, Enchantment enchantment, int minLevel) {
		ItemStack stack = new ItemStack(item);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(enchantment, minLevel), stack);
		AffixHelper.addLore(stack, new TranslationTextComponent("ingredient.apotheosis.ench", ((IFormattableTextComponent) enchantment.getDisplayName(minLevel)).mergeStyle(TextFormatting.DARK_PURPLE, TextFormatting.ITALIC)));
		return stack;
	}

	@Override
	public boolean test(ItemStack stack) {
		return super.test(stack) && EnchantmentHelper.getEnchantmentLevel(this.enchantment, stack) >= this.minLevel;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public JsonElement serialize() {
		return new JsonObject();
	}

	public static class Serializer implements IIngredientSerializer<EnchantmentIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public EnchantmentIngredient parse(PacketBuffer buffer) {
			ItemStack stack = buffer.readItemStack();
			Enchantment ench = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getValue(buffer.readVarInt());
			int level = buffer.readShort();
			return new EnchantmentIngredient(stack.getItem(), ench, level);
		}

		@Override
		public EnchantmentIngredient parse(JsonObject json) {
			return null;
		}

		@Override
		public void write(PacketBuffer buffer, EnchantmentIngredient ingredient) {
			buffer.writeItemStack(new ItemStack(ingredient.item));
			buffer.writeVarInt(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(ingredient.enchantment));
			buffer.writeShort(ingredient.minLevel);
		}
	}

}