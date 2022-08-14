package shadows.apotheosis.adventure.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootRarity;

public class RarityCommand {

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = (ctx, builder) -> {
		return SharedSuggestionProvider.suggest(LootRarity.ids().stream(), builder);
	};

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("loot_rarity").requires(c -> c.hasPermission(2)).then(Commands.argument("rarity", StringArgumentType.word()).suggests(SUGGEST_RARITY).executes(c -> {
			Player p = c.getSource().getPlayerOrException();
			String type = c.getArgument("rarity", String.class);
			LootRarity rarity = LootRarity.byId(type);
			ItemStack stack = p.getMainHandItem();
			AffixHelper.setRarity(stack, rarity);
			return 0;
		})));
	}

}
