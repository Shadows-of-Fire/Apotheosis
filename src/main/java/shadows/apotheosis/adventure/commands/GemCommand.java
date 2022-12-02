package shadows.apotheosis.adventure.commands;

import java.util.Arrays;
import java.util.Locale;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.affix.socket.gem.GemManager;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;

public class GemCommand {

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Operation.values()).map(Operation::name), builder);

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIB = (ctx, builder) -> SharedSuggestionProvider.suggest(ForgeRegistries.ATTRIBUTES.getKeys().stream().map(ResourceLocation::toString), builder);

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_GEM = (ctx, builder) -> SharedSuggestionProvider.suggest(GemManager.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("gem").requires(c -> c.hasPermission(2)).then(Commands.literal("fromPreset").then(Commands.argument("gem", ResourceLocationArgument.id()).suggests(SUGGEST_GEM).executes(c -> {
			Gem gem = GemManager.INSTANCE.getValue(ResourceLocationArgument.getId(c, "gem"));
			Player p = c.getSource().getPlayerOrException();
			ItemStack stack = GemItem.fromGem(gem, p.random);
			p.addItem(stack);
			return 0;
		}))).then(Commands.literal("custom").then(Commands.argument("attribute", ResourceLocationArgument.id()).suggests(SUGGEST_ATTRIB).then(Commands.argument("op", StringArgumentType.word()).suggests(SUGGEST_OP).then(Commands.argument("value", FloatArgumentType.floatArg()).then(Commands.argument("variant", IntegerArgumentType.integer(0, 11)).executes(c -> {
			Player p = c.getSource().getPlayerOrException();
			ItemStack gem = new ItemStack(Apoth.Items.GEM.get());
			CompoundTag tag = gem.getOrCreateTag();
			tag.putFloat("variant", c.getArgument("variant", Integer.class));
			Attribute attrib = ForgeRegistries.ATTRIBUTES.getValue(c.getArgument("attribute", ResourceLocation.class));
			Operation op = Operation.valueOf(c.getArgument("op", String.class).toUpperCase(Locale.ROOT));
			float value = c.getArgument("value", Float.class);
			var modif = new AttributeModifier("cmd-generated-modif", value, op);
			GemItem.setStoredBonus(gem, attrib, modif);
			p.addItem(gem);
			return 0;
		})))))).then(Commands.literal("random").executes(c -> {
			Player p = c.getSource().getPlayerOrException();
			ItemStack gem = GemManager.getRandomGemStack(p.random, p.getLuck(), IDimensional.matches(p.level));
			p.addItem(gem);
			return 0;
		})));
	}

}