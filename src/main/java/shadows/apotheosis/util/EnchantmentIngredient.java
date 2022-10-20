package shadows.apotheosis.util;

import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import shadows.placebo.util.PlaceboUtil;

public class EnchantmentIngredient extends Ingredient {

	protected final ItemLike item;
	protected final Enchantment enchantment;
	protected final int minLevel;

	public EnchantmentIngredient(ItemLike item, Enchantment enchantment, int minLevel) {
		super(Stream.of(new Ingredient.ItemValue(format(item, enchantment, minLevel))));
		this.item = item;
		this.enchantment = enchantment;
		this.minLevel = minLevel;
	}

	private static ItemStack format(ItemLike item, Enchantment enchantment, int minLevel) {
		ItemStack stack = new ItemStack(item);
		EnchantmentHelper.setEnchantments(ImmutableMap.of(enchantment, minLevel), stack);
		PlaceboUtil.addLore(stack, Component.translatable("ingredient.apotheosis.ench", ((MutableComponent) enchantment.getFullname(minLevel)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
		return stack;
	}

	@Override
	public boolean test(ItemStack stack) {
		return super.test(stack) && stack.getEnchantmentLevel(this.enchantment) >= this.minLevel;
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
	public JsonElement toJson() {
		return new JsonObject();
	}

	public static class Serializer implements IIngredientSerializer<EnchantmentIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public EnchantmentIngredient parse(FriendlyByteBuf buffer) {
			ItemStack stack = buffer.readItem();
			Enchantment ench = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getValue(buffer.readVarInt());
			int level = buffer.readShort();
			return new EnchantmentIngredient(stack.getItem(), ench, level);
		}

		@Override
		public EnchantmentIngredient parse(JsonObject json) {
			return null;
		}

		@Override
		public void write(FriendlyByteBuf buffer, EnchantmentIngredient ingredient) {
			buffer.writeItem(new ItemStack(ingredient.item));
			buffer.writeVarInt(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(ingredient.enchantment));
			buffer.writeShort(ingredient.minLevel);
		}
	}

}