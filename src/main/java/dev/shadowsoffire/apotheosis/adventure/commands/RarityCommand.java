package dev.shadowsoffire.apotheosis.adventure.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RarityCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = (ctx, builder) -> SharedSuggestionProvider.suggest(RarityRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("loot_rarity").requires(c -> c.hasPermission(2)).then(Commands.argument("rarity", StringArgumentType.word()).suggests(SUGGEST_RARITY).executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            String type = c.getArgument("rarity", String.class);
            LootRarity rarity = RarityRegistry.byLegacyId(type).get();
            ItemStack stack = p.getMainHandItem();
            AffixHelper.setRarity(stack, rarity);
            return 0;
        })));
    }

}
