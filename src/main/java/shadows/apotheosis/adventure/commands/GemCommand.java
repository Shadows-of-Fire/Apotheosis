package shadows.apotheosis.adventure.commands;

import java.util.Arrays;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemManager;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;

public class GemCommand {

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Operation.values()).map(Operation::name), builder);
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIB = (ctx, builder) -> SharedSuggestionProvider.suggest(ForgeRegistries.ATTRIBUTES.getKeys().stream().map(ResourceLocation::toString), builder);
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_GEM = (ctx, builder) -> SharedSuggestionProvider.suggest(GemManager.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

	@SuppressWarnings("removal")
	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("gem").requires(c -> c.hasPermission(2)).then(Commands.literal("fromPreset").then(Commands.argument("gem", ResourceLocationArgument.id()).suggests(SUGGEST_GEM).executes(c -> {
			Gem gem = GemManager.INSTANCE.getValue(ResourceLocationArgument.getId(c, "gem"));
			Player p = c.getSource().getPlayerOrException();
			ItemStack stack = GemManager.createGemStack(gem, p.random, null, p.getLuck());
			p.addItem(stack);
			return 0;
		}))).then(Commands.literal("random").executes(c -> {
			Player p = c.getSource().getPlayerOrException();
			ItemStack gem = GemManager.createRandomGemStack(p.random, c.getSource().getLevel(), p.getLuck(), IDimensional.matches(p.level), IStaged.matches(p));
			p.addItem(gem);
			return 0;
		})));
	}

}