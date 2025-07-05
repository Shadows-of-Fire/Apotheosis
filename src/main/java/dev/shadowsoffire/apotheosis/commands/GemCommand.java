package dev.shadowsoffire.apotheosis.commands;

import java.util.Arrays;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GemCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Operation.values()).map(Operation::name), builder);
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIB = (ctx, builder) -> SharedSuggestionProvider.suggest(BuiltInRegistries.ATTRIBUTE.keySet().stream().map(ResourceLocation::toString), builder);
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_GEM = (ctx, builder) -> SharedSuggestionProvider.suggest(GemRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("gem").requires(c -> c.hasPermission(2)).then(Commands.literal("fromPreset").then(Commands.argument("gem", ResourceLocationArgument.id()).suggests(SUGGEST_GEM).executes(c -> {
            Gem gem = GemRegistry.INSTANCE.getValue(ResourceLocationArgument.getId(c, "gem"));
            Player p = c.getSource().getPlayerOrException();
            GenContext ctx = GenContext.forPlayer(p);
            ItemStack stack = gem.toStack(Purity.random(ctx));
            p.addItem(stack);
            return 0;
        }))).then(Commands.literal("random").executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            GenContext ctx = GenContext.forPlayer(p);
            ItemStack gem = GemRegistry.createRandomGemStack(ctx);
            p.addItem(gem);
            return 0;
        })));
    }

}
