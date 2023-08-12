package dev.shadowsoffire.apotheosis.adventure.commands;

import java.util.Collections;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LootifyCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = RarityCommand.SUGGEST_RARITY;

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("lootify").requires(c -> c.hasPermission(2)).then(Commands.argument("rarity", ResourceLocationArgument.id()).suggests(SUGGEST_RARITY).executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            LootRarity rarity = RarityRegistry.INSTANCE.getValue(ResourceLocationArgument.getId(c, "rarity"));
            ItemStack stack = p.getMainHandItem();
            AffixHelper.setAffixes(stack, Collections.emptyMap());
            LootController.createLootItem(stack, rarity, p.level().random);
            return 0;
        })));
    }

}
