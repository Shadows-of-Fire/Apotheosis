package dev.shadowsoffire.apotheosis.commands;

import java.util.Arrays;
import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class WorldTierCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_WORLD_TIER = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(WorldTier.values()).map(WorldTier::getSerializedName), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("set_world_tier").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.argument("target_player", EntityArgument.player())
                .then(Commands.argument("tier", StringArgumentType.word()).suggests(SUGGEST_WORLD_TIER)
                    .executes(c -> {
                        Player p = EntityArgument.getPlayer(c, "target_player");
                        WorldTier tier = WorldTier.valueOf(StringArgumentType.getString(c, "tier").toUpperCase(Locale.ROOT));
                        WorldTier.setTier(p, tier);
                        c.getSource().sendSuccess(() -> Component.translatable("Set %s's world tier to %s", p.getName(), tier.getSerializedName()), true);
                        return 0;
                    }))));
    }
}
