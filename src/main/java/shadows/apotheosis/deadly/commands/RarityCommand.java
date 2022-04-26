package shadows.apotheosis.deadly.commands;

import java.util.Arrays;
import java.util.Locale;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.deadly.affix.AffixHelper;

public class RarityCommand {

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = (ctx, builder) -> {
		return SharedSuggestionProvider.suggest(Arrays.stream(LootRarity.values()).map(LootRarity::name), builder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
		pDispatcher.register(Commands.literal("loot_rarity").requires(c -> c.hasPermission(2)).then(Commands.argument("rarity", StringArgumentType.word()).suggests(SUGGEST_RARITY).executes(c -> {
			Player p = c.getSource().getPlayerOrException();
			String type = c.getArgument("rarity", String.class);
			LootRarity rarity = LootRarity.valueOf(type.toUpperCase(Locale.ROOT));
			ItemStack stack = p.getMainHandItem();
			AffixHelper.setRarity(stack, rarity);
			return 0;
		})));
	}

}
