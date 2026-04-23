package dev.shadowsoffire.apotheosis.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RarityCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = (ctx, builder) -> SharedSuggestionProvider.suggest(RarityRegistry.INSTANCE.getKeys().stream().map(Identifier::toString), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("set_rarity").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.argument("rarity", IdentifierArgument.id()).suggests(SUGGEST_RARITY).executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            LootRarity rarity = RarityRegistry.INSTANCE.getValue(IdentifierArgument.getId(c, "rarity"));
            ItemStack stack = p.getMainHandItem();
            AffixHelper.setRarity(stack, rarity);
            return 0;
        })));
    }

}
